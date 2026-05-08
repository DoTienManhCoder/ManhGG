package com.manhgg.rooms.auth;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private static final String ADMIN_USERNAME = "manhdokhac";
  private static final String ADMIN_PASSWORD = "7355608";

  private final UserAccountRepository userAccountRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final Map<String, AuthUser> sessions = new ConcurrentHashMap<>();

  public AuthService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @PostConstruct
  public void ensureAdminAccount() {
    UserAccount admin = userAccountRepository.findByUsername(ADMIN_USERNAME).orElseGet(UserAccount::new);
    admin.setUsername(ADMIN_USERNAME);
    admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
    admin.setRole("ADMIN");
    userAccountRepository.save(admin);
  }

  public LoginResponse login(LoginRequest request) {
    String username = normalizeUsername(request.username());
    UserAccount account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));

    if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau");
    }

    String token = UUID.randomUUID().toString();
    AuthUser user = toAuthUser(account);
    sessions.put(token, user);
    return new LoginResponse(token, user);
  }

  public AuthUser currentUser(String authorization) {
    String token = tokenFromHeader(authorization);
    AuthUser user = sessions.get(token);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui long dang nhap");
    }
    return user;
  }

  public AuthUser requireAdmin(String authorization) {
    AuthUser user = currentUser(authorization);
    if (!"ADMIN".equals(user.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi admin moi co quyen thuc hien");
    }
    return user;
  }

  public void logout(String authorization) {
    String token = tokenFromHeader(authorization);
    sessions.remove(token);
  }

  public List<AuthUser> listUsers() {
    return userAccountRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toAuthUser)
        .toList();
  }

  public AuthUser createUser(CreateUserRequest request) {
    String username = normalizeUsername(request.username());
    if (!username.matches("[a-z0-9._-]{3,32}")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tai khoan chi gom chu thuong, so, dau cham, gach ngang");
    }
    if (ADMIN_USERNAME.equals(username) || userAccountRepository.existsByUsername(username)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Tai khoan da ton tai");
    }

    UserAccount account = new UserAccount();
    account.setUsername(username);
    account.setPasswordHash(passwordEncoder.encode(request.password()));
    account.setRole("USER");
    account.setCreatedAt(Instant.now());
    return toAuthUser(userAccountRepository.save(account));
  }

  public void deleteUser(String id) {
    UserAccount account = userAccountRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tai khoan"));

    if ("ADMIN".equals(account.getRole())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khong the xoa tai khoan admin");
    }

    userAccountRepository.delete(account);
  }

  private AuthUser toAuthUser(UserAccount account) {
    return new AuthUser(account.getId(), account.getUsername(), account.getRole());
  }

  private String normalizeUsername(String username) {
    return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
  }

  private String tokenFromHeader(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui long dang nhap");
    }
    return authorization.substring("Bearer ".length()).trim();
  }
}
