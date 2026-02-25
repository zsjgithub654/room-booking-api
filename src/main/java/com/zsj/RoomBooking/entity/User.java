package com.zsj.RoomBooking.entity;

import com.zsj.RoomBooking.model.RoleType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName; // how to deal with length constriction in db
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<RoleType> roles ;

    @OneToMany(mappedBy = "user")
    private Set<Reservation> reservations;

    public User(String userName, String password, Set<RoleType> roles) {
        this.userName = userName;
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

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Set<RoleType> getRoles() {
        return roles;
    }
}