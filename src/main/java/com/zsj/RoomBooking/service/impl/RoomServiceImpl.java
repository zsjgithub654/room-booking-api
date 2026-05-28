package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.TimeRange;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    public List<Availability> searchAvailabilities(String name, Integer minCapacity, Integer maxCapacity, String area, LocalDateTime startTime, LocalDateTime endTime) {
        /* filter rooms */
        List<Room> rooms = searchRooms(name, minCapacity, maxCapacity, area);
        /* calculate availability */
        List<Availability> availabilities = new ArrayList<>();
        for (Room room : rooms) {
            /* calculate available slots */
            List<TimeRange> availableSlots = getAvailabilitiesForRoom(room.getId(), startTime, endTime);
            /* skip unavailable rooms */
            if (availableSlots.isEmpty()) {
                continue;
            }
            availabilities.add(new Availability(room, availableSlots));
        }
        return availabilities;
    }

    private List<TimeRange> getAvailabilitiesForRoom(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TimeRange> closures = closureRepository.getTimeByRoomIdAndOverlapping(roomId, startTime, endTime);
        List<TimeRange> reservations = reservationRepository.getTimeByRoomIdAndOverlappingAndActive(roomId, startTime, endTime);
        List<TimeRange> occupations = new ArrayList<>();
        occupations.addAll(closures);
        occupations.addAll(reservations);
        /* sort occupied time ranges */
        occupations.sort(Comparator.comparing(TimeRange::getStartTime));
        /* add slots between occupied time ranges */
        List<TimeRange> availableSlots = new ArrayList<>();
        LocalDateTime slotStartTime = startTime;
        for (TimeRange occupation : occupations) {
            if (occupation.getStartTime().isAfter(slotStartTime)) {
                availableSlots.add(new TimeRange(slotStartTime, occupation.getStartTime()));
            }
            slotStartTime = occupation.getEndTime();
        }
        /* add slot after the last occupied time range */
        if (slotStartTime.isBefore(endTime)) {
            availableSlots.add(new TimeRange(slotStartTime, endTime));
        }
        return availableSlots;
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
    public Room updateRoom(Long id, String name, Integer capacity, String area) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        room.setName(name);
        room.setCapacity(capacity);
        room.setArea(area);
        return room;
    }
}