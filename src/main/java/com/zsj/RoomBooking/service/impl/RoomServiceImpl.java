package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.RoomSchedule;
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
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public List<Room> searchRooms(String name, Integer minCapacity, Integer maxCapacity, String area) {
        Specification<Room> spec = Specification.unrestricted();
        /* name contains given string */
        if (name != null && !name.isBlank()) {
            spec = spec.and(RoomSpecifications.nameContains(name));
        }
        /* capacity in given range */
        if (minCapacity != null) {
            spec = spec.and(RoomSpecifications.minCapacity(minCapacity));
        }
        if (maxCapacity != null) {
            spec = spec.and(RoomSpecifications.minCapacity(maxCapacity));
        }
        /* area */
        if (area != null) {
            spec = spec.and(RoomSpecifications.inArea(area));
        }
        /* status */
        /* TODO: only show active for now, enable admin to see all later */
        spec = spec.and(RoomSpecifications.hasStatus(RoomStatus.ROOM_STATUS_ACTIVE));
        return roomRepository.findAll(spec);
    }

    @Override
    public List<RoomSchedule> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area, LocalDateTime startTime, LocalDateTime endTime) {
        /* filter rooms */
        List<Room> rooms = searchRooms(name, minCapacity, maxCapacity, area);
        /* calculate availability */
        List<RoomSchedule> availabilities = new ArrayList<>();
        for (Room room : rooms) {
            /* calculate available slots */
            List<Occupation> occupations = getOccupationsForRoom(room.getId(), startTime, endTime);
            /* skip rooms if no gap between occupations during the given range */
            if (!isAvailableDuringTime(room.getOpenTime(), room.getCloseTime(), startTime, endTime, occupations)) {
                continue;
            }
            availabilities.add(new RoomSchedule(room, occupations));
        }
        return availabilities;
    }

    private boolean isAvailableDuringTime(LocalTime openTime, LocalTime closeTime, LocalDateTime fromTime, LocalDateTime toTime, List<Occupation> occupations) {
        LocalDateTime availableSince = getNoEarlierThanOpenTime(fromTime, openTime);
        /* check if there is gap between occupations during the given range */
        for (Occupation occupation : occupations) {
            LocalDateTime availableUntil = getNoLaterThanCloseTime(occupation.getStartTime(), closeTime);
            if (availableSince.isBefore(availableUntil)) {
                return true;
            }
            availableSince = getNoEarlierThanOpenTime(occupation.getEndTime(), openTime);
        }
        LocalDateTime availableUntil = getNoLaterThanCloseTime(toTime, closeTime);
        return availableSince.isBefore(availableUntil);
    }

    private LocalDateTime getNoEarlierThanOpenTime(LocalDateTime time, LocalTime openTime) {
        return time.toLocalTime().isBefore(openTime)
                ? time.toLocalDate().atTime(openTime)
                : time;
    }

    private LocalDateTime getNoLaterThanCloseTime(LocalDateTime time, LocalTime closeTime) {
        return time.toLocalTime().isAfter(closeTime)
                ? time.toLocalDate().atTime(closeTime)
                : time;
    }

    private List<Occupation> getOccupationsForRoom(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Closure> closures = closureRepository.findByRoomIdAndOverlapping(roomId, startTime, endTime);
        List<Reservation> reservations = reservationRepository.findByRoomIdAndOverlappingAndActive(roomId, startTime, endTime);
        /* combine occupations and sort by startTime */
        return Stream.concat(
                        reservations.stream(),
                        closures.stream())
                .sorted(Comparator.comparing(Occupation::getStartTime))
                .toList();
    }


    /* TODO: rename */
    @Override
    public Room getRoom(Long id) {
        /* TODO: extract to constant */
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
    }

    @Override
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public List<Reservation> deleteRoom(Long id) {
        /* TODO: admin required */
        /* check if room exists */
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* already deleted */
        if (room.getStatus() == RoomStatus.ROOM_STATUS_DELETED) {
            return null;
        }
        room.setStatus(RoomStatus.ROOM_STATUS_DELETED);
        /* delete future closures */
        closureRepository.deleteByRoomIdAndAfterTime(id, LocalDateTime.now());
        /* close future reservations */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(id, LocalDateTime.now());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
        return reservations;
    }

    @Override
    public Room updateRoom(Long id, String name, Integer capacity, String area, LocalTime openTime, LocalTime closeTime) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        room.setName(name);
        room.setCapacity(capacity);
        room.setArea(area);
        /* TODO: not null */
        room.setOpenTime(openTime);
        room.setCloseTime(closeTime);
        return room;
    }
}