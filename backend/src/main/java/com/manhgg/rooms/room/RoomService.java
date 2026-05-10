package com.manhgg.rooms.room;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
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
  private static final String CLOUDINARY_STORAGE = "cloudinary";
  private static final String FILESYSTEM_STORAGE = "filesystem";

  private final RoomRepository roomRepository;
  private final GridFsTemplate gridFsTemplate;
  private final ListingTemplateService listingTemplateService;
  private final Path mediaStorageRoot;
  private final boolean deleteGridFsAfterMigration;
  private final Cloudinary cloudinary;
  private final String cloudinaryFolder;
  private final boolean deleteSourceAfterCloudinaryMigration;

  public RoomService(
      RoomRepository roomRepository,
      GridFsTemplate gridFsTemplate,
      ListingTemplateService listingTemplateService,
      @Value("${app.media.storage-dir}") String mediaStorageDir,
      @Value("${app.media.delete-gridfs-after-migration:false}") boolean deleteGridFsAfterMigration,
      @Value("${app.cloudinary.cloud-name}") String cloudName,
      @Value("${app.cloudinary.api-key}") String apiKey,
      @Value("${app.cloudinary.api-secret}") String apiSecret,
      @Value("${app.cloudinary.folder}") String cloudinaryFolder,
      @Value("${app.cloudinary.delete-source-after-migration:false}") boolean deleteSourceAfterCloudinaryMigration) {
    this.roomRepository = roomRepository;
    this.gridFsTemplate = gridFsTemplate;
    this.listingTemplateService = listingTemplateService;
    this.mediaStorageRoot = Path.of(mediaStorageDir).toAbsolutePath().normalize();
    this.deleteGridFsAfterMigration = deleteGridFsAfterMigration;
    this.cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret,
        "secure", true));
    this.cloudinaryFolder = cloudinaryFolder;
    this.deleteSourceAfterCloudinaryMigration = deleteSourceAfterCloudinaryMigration;
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
    room.getMedia().forEach(this::deleteMediaFile);
    roomRepository.delete(room);
  }

  public ServedMedia getMedia(String mediaId) {
    Room room = roomRepository.findByMedia_Id(mediaId).orElse(null);
    MediaRef media = room == null ? null : findMedia(room, mediaId);

    if (media != null && isCloudinaryMedia(media)) {
      return getCloudinaryMedia(media);
    }

    if (media != null && isFilesystemMedia(media)) {
      ServedMedia filesystemMedia = getFilesystemMedia(media);
      if (filesystemMedia != null) {
        return filesystemMedia;
      }
    }

    GridFsResource resource = getGridFsMedia(mediaId);
    return new ServedMedia(
        resource,
        resource.getFilename(),
        resource.getContentType(),
        contentLength(resource));
  }

  public void deleteMedia(String roomId, String mediaId) {
    Room room = getRoom(roomId);
    List<MediaRef> removed = new ArrayList<>();
    room.getMedia().removeIf(media -> {
      boolean shouldRemove = media.getId().equals(mediaId);
      if (shouldRemove) {
        removed.add(media);
      }
      return shouldRemove;
    });

    if (removed.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    }

    removed.forEach(this::deleteMediaFile);
    reorderMedia(room);
    room.setUpdatedAt(Instant.now());
    roomRepository.save(room);
  }

  public int migrateMediaToCloudinary() {
    int migratedCount = 0;

    for (Room room : roomRepository.findAll()) {
      boolean changed = false;
      for (MediaRef media : room.getMedia()) {
        if (isCloudinaryMedia(media)) {
          continue;
        }

        try (InputStream inputStream = openLegacyMediaStream(media)) {
          if (inputStream == null) {
            continue;
          }

          String oldStorage = media.getStorage();
          String oldPath = media.getPath();
          String oldId = media.getId();
          CloudinaryUpload upload = uploadToCloudinary(room.getId(), media.getName(), media.getType(), inputStream);
          media.setStorage(CLOUDINARY_STORAGE);
          media.setPath(upload.publicId());
          media.setUrl(upload.secureUrl());
          if (media.getType() == null || media.getType().isBlank()) {
            media.setType(upload.contentType());
          }
          if (upload.size() > 0) {
            media.setSize(upload.size());
          }
          changed = true;
          migratedCount++;

          if (deleteSourceAfterCloudinaryMigration) {
            deleteLegacyMediaFile(oldStorage, oldId, oldPath);
          }
        } catch (IOException exception) {
          // Keep the original metadata so old media remains readable if migration fails.
        }
      }

      if (changed) {
        room.setUpdatedAt(Instant.now());
        roomRepository.save(room);
      }
    }

    return migratedCount;
  }

  public int migrateGridFsMediaToFilesystem() {
    int migratedCount = 0;

    for (Room room : roomRepository.findAll()) {
      boolean changed = false;
      for (MediaRef media : room.getMedia()) {
        if (isFilesystemMedia(media)) {
          continue;
        }

        GridFSFile file = findGridFsFile(media.getId());
        if (file == null) {
          continue;
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        try (InputStream inputStream = resource.getInputStream()) {
          String path = writeMediaFile(room.getId(), media.getName(), inputStream);
          media.setStorage(FILESYSTEM_STORAGE);
          media.setPath(path);
          if (media.getType() == null || media.getType().isBlank()) {
            media.setType(resource.getContentType());
          }
          changed = true;
          migratedCount++;

          if (deleteGridFsAfterMigration) {
            deleteGridFile(media.getId());
          }
        } catch (IOException exception) {
          // Keep the original GridFS metadata so old media remains readable if migration fails.
        }
      }

      if (changed) {
        room.setUpdatedAt(Instant.now());
        roomRepository.save(room);
      }
    }

    return migratedCount;
  }

  public GeneratedListingResponse generateListingTemplate(String roomId) {
    return new GeneratedListingResponse(listingTemplateService.generate(getRoom(roomId)));
  }

  private void applyInput(Room room, RoomInput input, boolean allowStatusChange) {
    room.setAddress(input.address().trim());
    room.setRealAddress(clean(input.realAddress()));
    room.setPrice(input.price().trim());
    room.setCode(input.code().trim());
    room.setStatus(allowStatusChange && input.status() != null && !input.status().isBlank() ? input.status() : "open");
    room.setNote(input.note() == null ? "" : input.note().trim());
    room.setArea(clean(input.area()));
    room.setLayout(clean(input.layout()));
    room.setFurniture(clean(input.furniture()));
    room.setAmenities(clean(input.amenities()));
    room.setSellingPoints(clean(input.sellingPoints()));
    room.setContact(clean(input.contact()));
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
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

        String originalName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
            ? UUID.randomUUID().toString()
            : file.getOriginalFilename();
        CloudinaryUpload upload = uploadToCloudinary(room.getId(), originalName, contentType, file.getInputStream());
        room.getMedia().add(new MediaRef(
            UUID.randomUUID().toString(),
            originalName,
            upload.contentType(),
            upload.size() > 0 ? upload.size() : file.getSize(),
            sortOrder++,
            Instant.now(),
            CLOUDINARY_STORAGE,
            upload.publicId(),
            upload.secureUrl()));
      } catch (IOException exception) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot save media file", exception);
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

    removed.forEach(this::deleteMediaFile);
  }

  private void reorderMedia(Room room) {
    room.getMedia().sort(Comparator.comparingInt(MediaRef::getSortOrder));
    for (int index = 0; index < room.getMedia().size(); index++) {
      room.getMedia().get(index).setSortOrder(index);
    }
  }

  private MediaRef findMedia(Room room, String mediaId) {
    return room.getMedia().stream()
        .filter(media -> media.getId().equals(mediaId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
  }

  private ServedMedia getFilesystemMedia(MediaRef media) {
    Path path = resolveMediaPath(media.getPath());
    if (!Files.isRegularFile(path)) {
      return null;
    }

    try {
      return new ServedMedia(
          new FileSystemResource(path),
          media.getName(),
          media.getType(),
          Files.size(path));
    } catch (IOException exception) {
      return null;
    }
  }

  private ServedMedia getCloudinaryMedia(MediaRef media) {
    if (media.getUrl() == null || media.getUrl().isBlank()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media URL not found");
    }

    try {
      return new ServedMedia(
          new UrlResource(media.getUrl()),
          media.getName(),
          media.getType(),
          media.getSize());
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media URL not found", exception);
    }
  }

  private GridFsResource getGridFsMedia(String mediaId) {
    GridFSFile file = findGridFsFile(mediaId);
    if (file == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    }

    return gridFsTemplate.getResource(file);
  }

  private GridFSFile findGridFsFile(String mediaId) {
    if (!ObjectId.isValid(mediaId)) {
      return null;
    }
    return gridFsTemplate.findOne(queryByObjectId(mediaId));
  }

  private String writeMediaFile(String roomId, String originalName, InputStream inputStream) throws IOException {
    Files.createDirectories(mediaStorageRoot);
    Path roomDirectory = mediaStorageRoot.resolve(roomId).normalize();
    if (!roomDirectory.startsWith(mediaStorageRoot)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid room id");
    }
    Files.createDirectories(roomDirectory);

    String filename = UUID.randomUUID() + extensionOf(originalName);
    Path target = roomDirectory.resolve(filename).normalize();
    if (!target.startsWith(roomDirectory)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media filename");
    }

    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    return mediaStorageRoot.relativize(target).toString().replace('\\', '/');
  }

  private String extensionOf(String filename) {
    if (filename == null) {
      return "";
    }

    String cleanName = Path.of(filename).getFileName().toString();
    int dotIndex = cleanName.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == cleanName.length() - 1) {
      return "";
    }

    return cleanName.substring(dotIndex).replaceAll("[^A-Za-z0-9.]", "");
  }

  private boolean isFilesystemMedia(MediaRef media) {
    return FILESYSTEM_STORAGE.equals(media.getStorage());
  }

  private boolean isCloudinaryMedia(MediaRef media) {
    return CLOUDINARY_STORAGE.equals(media.getStorage());
  }

  private void deleteMediaFile(MediaRef media) {
    if (isCloudinaryMedia(media)) {
      deleteCloudinaryMedia(media);
      return;
    }

    if (isFilesystemMedia(media)) {
      try {
        Files.deleteIfExists(resolveMediaPath(media.getPath()));
      } catch (IOException exception) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot delete media file", exception);
      }
      deleteGridFile(media.getId());
      return;
    }

    deleteGridFile(media.getId());
  }

  private void deleteLegacyMediaFile(String oldStorage, String mediaId, String path) {
    if (FILESYSTEM_STORAGE.equals(oldStorage)) {
      try {
        Files.deleteIfExists(resolveMediaPath(path));
      } catch (IOException exception) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot delete media file", exception);
      }
      return;
    }

    deleteGridFile(mediaId);
  }

  private void deleteCloudinaryMedia(MediaRef media) {
    if (media.getPath() == null || media.getPath().isBlank()) {
      return;
    }

    try {
      cloudinary.uploader().destroy(media.getPath(), ObjectUtils.asMap("resource_type", resourceType(media.getType())));
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot delete media from Cloudinary", exception);
    }
  }

  private InputStream openLegacyMediaStream(MediaRef media) throws IOException {
    if (isFilesystemMedia(media)) {
      Path path = resolveMediaPath(media.getPath());
      return Files.isRegularFile(path) ? Files.newInputStream(path) : null;
    }

    GridFSFile file = findGridFsFile(media.getId());
    return file == null ? null : gridFsTemplate.getResource(file).getInputStream();
  }

  private CloudinaryUpload uploadToCloudinary(String roomId, String originalName, String contentType, InputStream inputStream) throws IOException {
    Path tempFile = Files.createTempFile("manhgg-media-", extensionOf(originalName));
    try {
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      String publicId = cloudinaryPublicId(roomId);
      @SuppressWarnings("unchecked")
      Map<String, Object> result = cloudinary.uploader().upload(tempFile.toFile(), ObjectUtils.asMap(
          "resource_type", "auto",
          "folder", cloudinaryFolder,
          "public_id", publicId,
          "overwrite", false));

      String uploadedResourceType = stringValue(result.get("resource_type"));
      String format = stringValue(result.get("format"));
      return new CloudinaryUpload(
          stringValue(result.get("public_id")),
          stringValue(result.get("secure_url")),
          cloudinaryContentType(uploadedResourceType, format, contentType),
          longValue(result.get("bytes")));
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  private String cloudinaryPublicId(String roomId) {
    String cleanRoomId = roomId == null ? "unknown-room" : roomId.replaceAll("[^A-Za-z0-9_-]", "-");
    return cleanRoomId + "/" + UUID.randomUUID();
  }

  private String resourceType(String contentType) {
    return contentType != null && contentType.startsWith("video/") ? "video" : "image";
  }

  private String cloudinaryContentType(String resourceType, String format, String fallback) {
    if (resourceType == null || resourceType.isBlank() || format == null || format.isBlank()) {
      return fallback == null || fallback.isBlank() ? "application/octet-stream" : fallback;
    }

    if ("video".equals(resourceType)) {
      return "video/" + format;
    }
    return "image/" + format;
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private long longValue(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    return 0;
  }

  private Path resolveMediaPath(String path) {
    if (path == null || path.isBlank()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found");
    }

    Path resolvedPath = mediaStorageRoot.resolve(path).normalize();
    if (!resolvedPath.startsWith(mediaStorageRoot)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media path");
    }

    return resolvedPath;
  }

  private long contentLength(GridFsResource resource) {
    try {
      return resource.contentLength();
    } catch (IOException exception) {
      return -1;
    }
  }

  private void deleteGridFile(String mediaId) {
    if (!ObjectId.isValid(mediaId)) {
      return;
    }
    gridFsTemplate.delete(queryByObjectId(mediaId));
  }

  private Query queryByObjectId(String id) {
    return Query.query(Criteria.where("_id").is(new ObjectId(id)));
  }

  private record CloudinaryUpload(String publicId, String secureUrl, String contentType, long size) {}
}
