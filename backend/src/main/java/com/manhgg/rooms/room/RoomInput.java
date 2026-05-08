package com.manhgg.rooms.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoomInput(
    @NotBlank String address,
    @NotBlank String price,
    @NotBlank String code,
    @Pattern(regexp = "open|lock") String status,
    String note
) {}
