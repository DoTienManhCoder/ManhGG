package com.manhgg.rooms.room;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomRepository extends MongoRepository<Room, String> {
  List<Room> findAllByOrderByUpdatedAtDesc();
}
