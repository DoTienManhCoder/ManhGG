package com.manhgg.rooms.room;

import com.manhgg.rooms.auth.AuthService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class RoomController {
  private final RoomService roomService;
  private final AuthService authService;

  public RoomController(RoomService roomService, AuthService authService) {
    this.roomService = roomService;
    this.authService = authService;
  }

  @GetMapping("/rooms")
  public List<RoomResponse> listRooms(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.currentUser(authorization);
    return roomService.listRooms().stream().map(this::toResponse).toList();
  }

  @GetMapping("/rooms/{id}")
  public RoomResponse getRoom(
      @PathVariable String id,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.currentUser(authorization);
    return toResponse(roomService.getRoom(id));
  }

  @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public RoomResponse createRoom(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @ModelAttribute @Valid RoomInput input,
      @RequestParam(value = "files", required = false) List<MultipartFile> files) {
    authService.requireAdmin(authorization);
    return toResponse(roomService.createRoom(input, files));
  }

  @PutMapping(value = "/rooms/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public RoomResponse updateRoom(
      @PathVariable String id,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @ModelAttribute @Valid RoomInput input,
      @RequestParam(value = "keepMediaIds", required = false, defaultValue = "") String keepMediaIds,
      @RequestParam(value = "files", required = false) List<MultipartFile> files) {
    authService.requireAdmin(authorization);
    return toResponse(roomService.updateRoom(id, input, parseIds(keepMediaIds), files));
  }

  @DeleteMapping("/rooms/{id}")
  public void deleteRoom(
      @PathVariable String id,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.requireAdmin(authorization);
    roomService.deleteRoom(id);
  }

  @DeleteMapping("/rooms/{roomId}/media/{mediaId}")
  public void deleteMedia(
      @PathVariable String roomId,
      @PathVariable String mediaId,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.requireAdmin(authorization);
    roomService.deleteMedia(roomId, mediaId);
  }

  @GetMapping("/media/{id}")
  public ResponseEntity<InputStreamResource> getMedia(@PathVariable String id) throws IOException {
    GridFsResource resource = roomService.getMedia(id);
    String contentType = resource.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : resource.getContentType();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .contentLength(resource.contentLength())
        .cacheControl(CacheControl.noCache())
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
        .body(new InputStreamResource(resource.getInputStream()));
  }

  private RoomResponse toResponse(Room room) {
    List<MediaResponse> media = room.getMedia().stream()
        .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
        .map(item -> new MediaResponse(
            item.getId(),
            item.getName(),
            item.getType(),
            item.getSize(),
            item.getSortOrder(),
            item.getCreatedAt(),
            "/api/media/" + item.getId()))
        .toList();

    return new RoomResponse(
        room.getId(),
        room.getAddress(),
        room.getPrice(),
        room.getCode(),
        room.getStatus(),
        room.getNote(),
        media,
        room.getCreatedAt(),
        room.getUpdatedAt());
  }

  private Set<String> parseIds(String value) {
    if (value == null || value.isBlank()) {
      return Set.of();
    }

    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(item -> !item.isBlank())
        .collect(Collectors.toSet());
  }
}
