package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.result.RoomSchedule;
import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.RoomSpecifications;
import com.zsj.RoomBooking.model.criteria.RoomSearchCriteria;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {
    private static final String ROOM_NOT_FOUND = "Room not found.";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public Page<Room> searchRooms(RoomSearchCriteria criteria, Pageable pageable) {
        Pageable queryPageable = DefaultSorts.addRoomDefaultSort(pageable);
        Specification<Room> spec = Specification.unrestricted();
        /* name contains given string */
        if (criteria.name() != null && !criteria.name().isBlank()) {
            spec = spec.and(RoomSpecifications.nameContains(criteria.name()));
        }
        /* capacity in given range */
        if (criteria.minCapacity() != null) {
            spec = spec.and(RoomSpecifications.minCapacity(criteria.minCapacity()));
        }
        if (criteria.maxCapacity() != null) {
            spec = spec.and(RoomSpecifications.maxCapacity(criteria.maxCapacity()));
        }
        /* area */
        if (criteria.area() != null) {
            spec = spec.and(RoomSpecifications.inArea(criteria.area()));
        }
        /* status */
        if (criteria.status() != null) {
            spec = spec.and(RoomSpecifications.hasStatus(criteria.status()));
        }
        return roomRepository.findAll(spec, queryPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSchedule> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area,
                                                   LocalDate fromDate, LocalDate toDate, Boolean includeUnavailable) {
        /* filter rooms */
        List<Room> rooms = searchRooms(
                new RoomSearchCriteria(name, minCapacity, maxCapacity, area, RoomStatus.ROOM_STATUS_ACTIVE),
                Pageable.unpaged()).getContent();
        boolean shouldIncludeUnavailable = Boolean.TRUE.equals(includeUnavailable);
        LocalDateTime fromTime = fromDate.atStartOfDay();
        LocalDateTime toTime = toDate.plusDays(1).atStartOfDay();
        /* calculate availability */
        List<RoomSchedule> availabilities = new ArrayList<>();
        for (Room room : rooms) {
            List<Occupation> occupations = getOccupationsForRoom(room.getId(), fromTime, toTime);
            /* skip rooms if no gap between occupations during the given range */
            if (!shouldIncludeUnavailable && !isRoomAvailableDuringDays(room, fromDate, toDate, occupations)) {
                continue;
            }
            availabilities.add(new RoomSchedule(room, occupations));
        }
        return availabilities;
    }

    private List<Occupation> getOccupationsForRoom(Long roomId, LocalDateTime fromTime, LocalDateTime toTime) {
        /* get closures and reservations */
        List<Closure> closures = closureRepository.findByRoomIdAndOverlapping(roomId, fromTime, toTime);
        List<Reservation> reservations = reservationRepository.findByRoomIdAndOverlappingAndScheduled(
                roomId, fromTime, toTime, DefaultSorts.occupationSort());
        /* combine occupations and sort by startTime */
        return Stream.concat(
                        reservations.stream(),
                        closures.stream())
                .sorted(DefaultSorts.occupationComparator())
                .toList();
    }

    private boolean isRoomAvailableDuringDays(Room room, LocalDate fromDate, LocalDate toDate, List<Occupation> occupations) {
        if (room.isOpenAllDay()) {
            return getNextOccupationAfterTimeRangeFilled(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay(), occupations, 0) == -1;
        }
        int occupationIndex = 0;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            occupationIndex = getNextOccupationAfterTimeRangeFilled(
                    date.atTime(room.getOpenTime()),
                    date.atTime(room.getCloseTime()),
                    occupations,
                    occupationIndex);
            if (occupationIndex == -1) {
                return true;
            }
        }
        return false;
    }

    /* return -1 if gap found, otherwise return next occupation index to continue from */
    private int getNextOccupationAfterTimeRangeFilled(LocalDateTime fromTime, LocalDateTime toTime, List<Occupation> occupations, int startIndex) {
        LocalDateTime releaseAt = fromTime;
        while (startIndex < occupations.size()) {
            Occupation occupation = occupations.get(startIndex);
            /* ends before earliest available time */
            if (!occupation.getEndTime().isAfter(releaseAt)) {
                startIndex++;
                continue;
            }
            /* starts after latest available time */
            if (!occupation.getStartTime().isBefore(toTime)) {
                break;
            }
            /* gap found */
            if (releaseAt.isBefore(occupation.getStartTime())) {
                return -1;
            }
            /* no gap, update releaseAt */
            if (occupation.getEndTime().isAfter(releaseAt)) {
                releaseAt = occupation.getEndTime();
            }
            /* window ends without gap */
            if (!releaseAt.isBefore(toTime)) {
                return startIndex;
            }
            startIndex++;
        }
        return releaseAt.isBefore(toTime) ? -1 : startIndex;
    }

    @Override
    public Room getRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
    }

    @Override
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public List<Reservation> deleteRoom(Long id) {
        /* verify and acquire lock on room */
        Room room = roomRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
        if (!room.isActive()) {
            return Collections.emptyList();
        }
        room.setStatus(RoomStatus.ROOM_STATUS_DELETED);
        /* delete future closures */
        closureRepository.deleteByRoomIdAndStartAfter(id, LocalDateTime.now());
        /* close future reservations */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndScheduled(
                id, LocalDateTime.now(), DefaultSorts.occupationSort());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
        return reservations;
    }

    @Override
    public Room updateRoom(Long id, String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime) {
        Room room = roomRepository.findById(id)
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
        room.setName(name);
        room.setCapacity(capacity);
        room.setArea(area);
        room.setOpenHours(openTime, closeTime);
        return room;
    }
}
