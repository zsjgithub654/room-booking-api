package com.zsj.roombooking.repository;

import com.zsj.roombooking.model.Role;
import com.zsj.roombooking.model.UserStatus;
import com.zsj.roombooking.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class UserSpecifications {
    public static Specification<User> usernameContains(String username) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("username")),
                        "%" + username.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<User> hasAdminRole(Boolean isAdmin) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(isAdmin)) {
                return cb.isMember(Role.ROLE_ADMIN, root.get("roles"));
            }
            return cb.isNotMember(Role.ROLE_ADMIN, root.get("roles"));
        };
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
