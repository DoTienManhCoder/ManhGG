package com.manhgg.rooms.auth;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private static final String ADMIN_USERNAME = "manhdokhac";
  private static final String ADMIN_PASSWORD = "7355608";
  private static final Duration SESSION_TIMEOUT = Duration.ofDays(30);

  private final UserAccountRepository userAccountRepository;
  private final AuthSessionRepository authSessionRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(UserAccountRepository userAccountRepository, AuthSessionRepository authSessionRepository) {
    this.userAccountRepository = userAccountRepository;
    this.authSessionRepository = authSessionRepository;
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu"));

    if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu");
    }

    String token = UUID.randomUUID().toString();
    AuthSession session = new AuthSession();
    session.setToken(token);
    session.setUserId(account.getId());
    session.setExpiresAt(nextExpiry());
    authSessionRepository.save(session);

    return new LoginResponse(token, toAuthUser(account));
  }

  public AuthUser currentUser(String authorization) {
    String token = tokenFromHeader(authorization);
    AuthSession session = authSessionRepository.findById(token)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập"));

    Instant now = Instant.now();
    if (session.getExpiresAt() == null || !session.getExpiresAt().isAfter(now)) {
      authSessionRepository.deleteById(token);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn");
    }

    UserAccount account = userAccountRepository.findById(session.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập lại"));

    session.setExpiresAt(nextExpiry());
    authSessionRepository.save(session);
    return toAuthUser(account);
  }

  public AuthUser requireAdmin(String authorization) {
    AuthUser user = currentUser(authorization);
    if (!"ADMIN".equals(user.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ admin mới có quyền thực hiện");
    }
    return user;
  }

  public void logout(String authorization) {
    String token = tokenFromHeader(authorization);
    authSessionRepository.deleteById(token);
  }

  public List<AuthUser> listUsers() {
    return userAccountRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toAuthUser)
        .toList();
  }

  public AuthUser createUser(CreateUserRequest request) {
    String username = normalizeUsername(request.username());
    if (!username.matches("[a-z0-9._-]{3,32}")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản chỉ gồm chữ thường, số, dấu chấm, gạch ngang");
    }
    if (ADMIN_USERNAME.equals(username) || userAccountRepository.existsByUsername(username)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại");
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));

    if ("ADMIN".equals(account.getRole())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa tài khoản admin");
    }

    authSessionRepository.deleteByUserId(id);
    userAccountRepository.delete(account);
  }

  private AuthUser toAuthUser(UserAccount account) {
    return new AuthUser(account.getId(), account.getUsername(), account.getRole());
  }

  private Instant nextExpiry() {
    return Instant.now().plus(SESSION_TIMEOUT);
  }

  private String normalizeUsername(String username) {
    return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
  }

  private String tokenFromHeader(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
    }
    return authorization.substring("Bearer ".length()).trim();
  }
}
