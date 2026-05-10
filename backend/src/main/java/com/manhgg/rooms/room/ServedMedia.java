package com.manhgg.rooms.room;

import org.springframework.core.io.Resource;

public record ServedMedia(
    Resource resource,
    String filename,
    String contentType,
    long contentLength
) {}
