package com.manhgg.rooms.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
  boolean existsByUsername(String username);

  Optional<UserAccount> findByUsername(String username);

  List<UserAccount> findAllByOrderByCreatedAtDesc();
}
