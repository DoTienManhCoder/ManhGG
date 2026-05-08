package com.manhgg.rooms.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 32) String username,
    @NotBlank @Size(min = 4, max = 72) String password
) {}
