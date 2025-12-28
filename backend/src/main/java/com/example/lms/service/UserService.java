package com.example.lms.service;

import com.example.lms.entity.User;
import com.example.lms.entity.UserRole;
import com.example.lms.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  public List<User> findStudents() {
    return userRepository.findAllByRole(UserRole.ROLE_STUDENT);
  }

  public List<User> findTeachers() {
    return userRepository.findAllByRole(UserRole.ROLE_TEACHER);
  }

  public User getByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public User getById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @Transactional
  public void assignRole(Long userId, String roleName) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    UserRole role = parseRole(roleName);
    user.setRole(role);
    userRepository.save(user);
  }

  @Transactional
  public void removeRole(Long userId, String roleName) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getRole() == null || user.getRole().name().equals(roleName)) {
      user.setRole(UserRole.ROLE_STUDENT);
      userRepository.save(user);
    }
  }

  @Transactional
  public void promoteToAdmin(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setRole(UserRole.ROLE_ADMIN);
    userRepository.save(user);
  }

  @Transactional
  public void setEnabled(Long userId, boolean enabled) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setEnabled(enabled);
    userRepository.save(user);
  }

  private UserRole parseRole(String roleName) {
    if (roleName == null || roleName.isBlank()) {
      throw new IllegalArgumentException("Role is required");
    }
    try {
      return UserRole.valueOf(roleName);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Role not found");
    }
  }
}
