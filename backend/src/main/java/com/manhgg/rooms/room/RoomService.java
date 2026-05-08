package com.manhgg.rooms.room;

import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoomService {
  private final RoomRepository roomRepository;
  private final GridFsTemplate gridFsTemplate;

  public RoomService(RoomRepository roomRepository, GridFsTemplate gridFsTemplate) {
    this.roomRepository = roomRepository;
    this.gridFsTemplate = gridFsTemplate;
  }

  public List<Room> listRooms() {
    return roomRepository.findAllByOrderByUpdatedAtDesc();
  }

  public Room getRoom(String id) {
    return roomRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
  }

  public Room createRoom(RoomInput input, List<MultipartFile> files) {
    Room room = new Room();
    applyInput(room, input, false);
    room.setCreatedAt(Instant.now());
    room.setUpdatedAt(Instant.now());
    room = roomRepository.save(room);
    addFiles(room, files);
    return roomRepository.save(room);
  }

  public Room updateRoom(String id, RoomInput input, Set<String> keepMediaIds, List<MultipartFile> files) {
    Room room = getRoom(id);
    applyInput(room, input, true);
    room.setUpdatedAt(Instant.now());
    removeMissingMedia(room, keepMediaIds);
    addFiles(room, files);
    reorderMedia(room);
    return roomRepository.save(room);
  }

  public void deleteRoom(String id) {
    Room room = getRoom(id);
    room.getMedia().forEach(media -> deleteGridFile(media.getId()));
    roomRepository.delete(room);
  }

  public GridFsResource getMedia(String mediaId) {
    GridFSFile file = gridFsTemplate.findOne(queryByObjectId(mediaId));
    if (file == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    }

    return gridFsTemplate.getResource(file);
  }

  public void deleteMedia(String roomId, String mediaId) {
    Room room = getRoom(roomId);
    boolean removed = room.getMedia().removeIf(media -> media.getId().equals(mediaId));
    if (!removed) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    }

    deleteGridFile(mediaId);
    reorderMedia(room);
    room.setUpdatedAt(Instant.now());
    roomRepository.save(room);
  }

  private void applyInput(Room room, RoomInput input, boolean allowStatusChange) {
    room.setAddress(input.address().trim());
    room.setPrice(input.price().trim());
    room.setCode(input.code().trim());
    room.setStatus(allowStatusChange && input.status() != null && !input.status().isBlank() ? input.status() : "open");
    room.setNote(input.note() == null ? "" : input.note().trim());
  }

  private void addFiles(Room room, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return;
    }

    int sortOrder = room.getMedia().stream()
        .map(MediaRef::getSortOrder)
        .max(Integer::compareTo)
        .orElse(-1) + 1;

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        continue;
      }

      try {
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image and video files are allowed");
        }

        Document metadata = new Document("roomId", room.getId())
            .append("contentType", contentType)
            .append("originalName", file.getOriginalFilename());
        ObjectId objectId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), contentType, metadata);
        room.getMedia().add(new MediaRef(
            objectId.toHexString(),
            file.getOriginalFilename(),
            contentType,
            file.getSize(),
            sortOrder++,
            Instant.now()));
      } catch (IOException exception) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not save media file", exception);
      }
    }
  }

  private void removeMissingMedia(Room room, Set<String> keepMediaIds) {
    Set<String> keepIds = keepMediaIds == null ? new HashSet<>() : keepMediaIds;
    List<MediaRef> removed = new ArrayList<>();
    room.getMedia().removeIf(media -> {
      boolean shouldRemove = !keepIds.contains(media.getId());
      if (shouldRemove) {
        removed.add(media);
      }
      return shouldRemove;
    });

    removed.forEach(media -> deleteGridFile(media.getId()));
  }

  private void reorderMedia(Room room) {
    room.getMedia().sort(Comparator.comparingInt(MediaRef::getSortOrder));
    for (int index = 0; index < room.getMedia().size(); index++) {
      room.getMedia().get(index).setSortOrder(index);
    }
  }

  private void deleteGridFile(String mediaId) {
    gridFsTemplate.delete(queryByObjectId(mediaId));
  }

  private Query queryByObjectId(String id) {
    if (!ObjectId.isValid(id)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media id");
    }
    return Query.query(Criteria.where("_id").is(new ObjectId(id)));
  }
}
