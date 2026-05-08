package com.manhgg.rooms.room;

import java.time.Instant;

public record MediaResponse(
    String id,
    String name,
    String type,
    long size,
    int sortOrder,
    Instant createdAt,
    String url
) {}
