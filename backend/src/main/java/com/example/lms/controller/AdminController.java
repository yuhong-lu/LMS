package com.example.lms.controller;

import com.example.lms.dto.UpdateUserStatusRequest;
import com.example.lms.entity.User;
import com.example.lms.service.AdminService;
import com.example.lms.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
  private final AdminService adminService;
  private final UserService userService;

  public AdminController(
      AdminService adminService,
      UserService userService) {
    this.adminService = adminService;
    this.userService = userService;
  }

  @GetMapping("/users")
  public List<User> listUsers() {
    return adminService.listUsers();
  }

  @PostMapping("/users/{userId}/roles/{roleName}")
  public ResponseEntity<Void> assignRole(
      @PathVariable Long userId, @PathVariable String roleName) {
    userService.assignRole(userId, roleName);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/users/{userId}/promote-admin")
  public ResponseEntity<Void> promoteToAdmin(@PathVariable Long userId) {
    userService.promoteToAdmin(userId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/users/{userId}/roles/{roleName}")
  public ResponseEntity<Void> removeRole(
      @PathVariable Long userId, @PathVariable String roleName) {
    userService.removeRole(userId, roleName);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/users/{userId}/status")
  public ResponseEntity<Void> updateUserStatus(
      @PathVariable Long userId, @RequestBody UpdateUserStatusRequest request) {
    userService.setEnabled(userId, request.isEnabled());
    return ResponseEntity.ok().build();
  }
}
