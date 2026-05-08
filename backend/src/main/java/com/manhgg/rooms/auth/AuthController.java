package com.manhgg.rooms.auth;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth/login")
  public LoginResponse login(@RequestBody @Valid LoginRequest request) {
    return authService.login(request);
  }

  @GetMapping("/auth/me")
  public AuthUser me(@RequestHeader(value = "Authorization", required = false) String authorization) {
    return authService.currentUser(authorization);
  }

  @PostMapping("/auth/logout")
  public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.logout(authorization);
  }

  @GetMapping("/users")
  public List<AuthUser> listUsers(@RequestHeader(value = "Authorization", required = false) String authorization) {
    authService.requireAdmin(authorization);
    return authService.listUsers();
  }

  @PostMapping("/users")
  public AuthUser createUser(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody @Valid CreateUserRequest request) {
    authService.requireAdmin(authorization);
    return authService.createUser(request);
  }

  @DeleteMapping("/users/{id}")
  public void deleteUser(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @PathVariable String id) {
    authService.requireAdmin(authorization);
    authService.deleteUser(id);
  }
}
