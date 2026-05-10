package com.manhgg.rooms.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoomInput(
    @NotBlank String address,
    String realAddress,
    @NotBlank String price,
    @NotBlank String code,
    @Pattern(regexp = "open|lock") String status,
    String note,
    String area,
    String layout,
    String furniture,
    String amenities,
    String sellingPoints,
    String contact
) {}
