package com.example.lms.controller;

import com.example.lms.entity.User;
import com.example.lms.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();
    response.put("username", authentication.getName());
    response.put("authorities", authentication.getAuthorities());
    return response;
  }

  @GetMapping("/students")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public List<User> listStudents() {
    return userService.findStudents();
  }

  @GetMapping("/teachers")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public List<User> listTeachers() {
    return userService.findTeachers();
  }
}
