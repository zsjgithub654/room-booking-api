package com.zsj.roombooking.model.entity;

import com.zsj.roombooking.model.Role;
import com.zsj.roombooking.model.UserStatus;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.HashSet;
import java.util.Set;

@Entity
/* user is db reserved word */
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;

    @Column(unique = true)
    private String username;
    private String password;
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles ;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.status = UserStatus.USER_STATUS_ACTIVE;
        this.roles = new HashSet<>();
        this.roles.add(Role.ROLE_USER);
    }

    public void addAdminRole() {
        roles.add(Role.ROLE_ADMIN);
    }

    public void removeAdminRole() {
        roles.remove(Role.ROLE_ADMIN);
    }

    /* required by JPA */
    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == UserStatus.USER_STATUS_ACTIVE;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
