package com.zsj.roombooking.bootstrap;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

/* automatic validation is disabled on purpose */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{user.username.pattern}")
        String username,
        @Size(min = 8, max = 30)
        @Pattern(regexp = "^[A-Za-z0-9\\p{Punct}]+$", message = "{user.password.pattern}")
        String password) {
}
