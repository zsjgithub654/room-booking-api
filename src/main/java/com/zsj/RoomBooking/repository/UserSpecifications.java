package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class UserSpecifications {
    public static Specification<User> usernameContains(String username) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("username")),
                        "%" + username.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) ->
                cb.isMember(role, root.get("roles"));
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
