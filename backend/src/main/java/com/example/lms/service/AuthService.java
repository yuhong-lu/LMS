package com.example.lms.service;

import com.example.lms.dto.AuthRequest;
import com.example.lms.dto.AuthResponse;
import com.example.lms.dto.RegisterRequest;
import com.example.lms.entity.User;
import com.example.lms.entity.UserRole;
import com.example.lms.repository.UserRepository;
import com.example.lms.security.JwtService;
import com.example.lms.security.UserPrincipal;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    String roleName = request.getRole();
    if (roleName == null || roleName.isBlank()) {
      roleName = "ROLE_STUDENT";
    }
    UserRole role = parseRole(roleName);

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setStudentNumber(request.getStudentNumber());
    user.setClassName(request.getClassName());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(role);

    userRepository.save(user);

    UserPrincipal principal = new UserPrincipal(user);
    String token = jwtService.generateToken(principal);

    List<String> roles = List.of(user.getRole().name());

    return new AuthResponse(token, user.getUsername(), roles);
  }

  public AuthResponse login(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    UserPrincipal principal = new UserPrincipal(user);
    String token = jwtService.generateToken(principal);

    List<String> roles = List.of(user.getRole().name());

    return new AuthResponse(token, user.getUsername(), roles);
  }

  private UserRole parseRole(String roleName) {
    try {
      return UserRole.valueOf(roleName);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Role not found");
    }
  }
}
