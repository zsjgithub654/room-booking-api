package com.zsj.RoomBooking;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username; // how to deal with length constriction in db
    private String password;
    private List<Role> roles ;
    private List<Reservation> reservations;

    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public boolean addRole(RoleType roleType) {
        return true;
    }

    public boolean removeRole(RoleType roleType) {
        return true;
    }

    public boolean changePassword(String password) {
        return true;
    }
}
