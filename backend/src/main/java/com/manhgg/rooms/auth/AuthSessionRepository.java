package com.manhgg.rooms.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {
  void deleteByUserId(String userId);
}
