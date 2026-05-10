package com.manhgg.rooms.room;

import java.time.Instant;
import java.util.List;

public record RoomResponse(
    String id,
    String address,
    String realAddress,
    String price,
    String code,
    String status,
    String note,
    String area,
    String layout,
    String furniture,
    String amenities,
    String sellingPoints,
    String contact,
    List<MediaResponse> media,
    Instant createdAt,
    Instant updatedAt
) {}
