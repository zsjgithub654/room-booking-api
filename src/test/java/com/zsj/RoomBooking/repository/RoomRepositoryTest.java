package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.RoomStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
/* PER_CLASS allow @BeforeAll be non-static */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoomRepositoryTest {
    @Autowired
    private RoomRepository roomRepository;

    @BeforeAll
    void Setup() {
        roomRepository.deleteAll();
        List<Room> rooms = List.of(
                new Room("101", 12, "Building A"),
                new Room("102", 4, "Building A"),
                new Room("101", 6, "Building B")
        );
        rooms.get(2).setStatus(RoomStatus.ROOM_STATUS_DELETED);
        roomRepository.saveAll(rooms);
    }

    @Test
    void FilterByNameContainsTest() {
        Specification<Room> spec = Specification.unrestricted();
        spec = spec.and(RoomSpecifications.nameContains("101"));
        List<Room> result = roomRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }

    @Test
    void FilterByMinCapacityTest() {
        Specification<Room> spec = Specification.unrestricted();
        spec = spec.and(RoomSpecifications.minCapacity(6));
        List<Room> result = roomRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }

    @Test
    void FilterByMaxCapacityTest() {
        Specification<Room> spec = Specification.unrestricted();
        spec = spec.and(RoomSpecifications.maxCapacity(6));
        List<Room> result = roomRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }

    @Test
    void FilterByInAreaTest() {
        Specification<Room> spec = Specification.unrestricted();
        spec = spec.and(RoomSpecifications.inArea("Building A"));
        List<Room> result = roomRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }

    @Test
    void FilterByHasStatusTest() {
        Specification<Room> spec = Specification.unrestricted();
        spec = spec.and(RoomSpecifications.hasStatus(RoomStatus.ROOM_STATUS_ACTIVE));
        List<Room> result = roomRepository.findAll(spec);
        assertThat(result).hasSize(2);
    }
}
