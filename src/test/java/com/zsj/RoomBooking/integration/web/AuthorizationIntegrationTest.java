package com.zsj.RoomBooking.integration.web;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for endpoints with special authorization requirements.
 * Mvc layer configuration is required.
 *
 */

@Testcontainers
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-trixie");

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        closureRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void ownerCanAccessOwnReservationTest() throws Exception {
        User owner = saveUser("owner", "password1", false);
        Room room = roomRepository.save(new Room("101", 12, "Building A", null, null));
        Reservation reservation = reservationRepository.save(
                new Reservation(owner, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );

        mockMvc.perform(get("/reservations/{id}", reservation.getId())
                        .with(httpBasic("owner", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.userId").value(owner.getId()))
                .andExpect(jsonPath("$.roomId").value(room.getId()));
    }

    @Test
    void otherUserCannotAccessThatReservationTest() throws Exception {
        User owner = saveUser("owner", "password1", false);
        User otherUser = saveUser("other", "password2", false);
        Room room = roomRepository.save(new Room("101", 12, "Building A", null, null));
        Reservation reservation = reservationRepository.save(
                new Reservation(owner, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );

        mockMvc.perform(get("/reservations/{id}", reservation.getId())
                        .with(httpBasic(otherUser.getUsername(), "password2")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAnyReservationTest() throws Exception {
        User owner = saveUser("owner", "password1", false);
        User admin = saveUser("admin", "password3", true);
        Room room = roomRepository.save(new Room("101", 12, "Building A", null, null));
        Reservation reservation = reservationRepository.save(
                new Reservation(owner, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );

        mockMvc.perform(get("/reservations/{id}", reservation.getId())
                        .with(httpBasic(admin.getUsername(), "password3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.userId").value(owner.getId()));
    }

    @Test
    void publicAvailabilityAccessTest() throws Exception {
        LocalDate date = LocalDate.of(2300, 3, 1);
        Room availableRoom = roomRepository.save(new Room("101", 12, "Building A", null, null));
        Room unavailableRoom = roomRepository.save(new Room("102", 8, "Building B", null, null));
        User user = saveUser("user1", "password1", false);
        reservationRepository.save(new Reservation(user, availableRoom,
                LocalDateTime.of(2300, 3, 1, 10, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0)));
        closureRepository.save(new Closure(unavailableRoom,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()));

        mockMvc.perform(get("/rooms/availabilities")
                        .param("fromDate", date.toString())
                        .param("toDate", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].room.id").value(availableRoom.getId()))
                .andExpect(jsonPath("$[0].room.name").value(availableRoom.getName()));
    }

    @Test
    void adminOnlyRoomEndpointRejectsAnonymousAndUserTest() throws Exception {
        saveUser("user1", "password1", false);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/rooms")
                        .with(httpBasic("user1", "password1")))
                .andExpect(status().isForbidden());
    }

    private User saveUser(String username, String rawPassword, boolean isAdmin) {
        User user = new User(username, passwordEncoder.encode(rawPassword));
        if (isAdmin) {
            user.addAdminRole();
        }
        return userRepository.save(user);
    }
}
