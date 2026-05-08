package com.manhgg.rooms.room;

import java.time.Instant;
import java.util.List;

public record RoomResponse(
    String id,
    String address,
    String price,
    String code,
    String status,
    String note,
    List<MediaResponse> media,
    Instant createdAt,
    Instant updatedAt
) {}
