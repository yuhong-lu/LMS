package com.example.lms.controller;

import com.example.lms.dto.EnrollmentDto;
import com.example.lms.dto.EnrollmentRequest;
import com.example.lms.entity.User;
import com.example.lms.service.EnrollmentService;
import com.example.lms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
  private final EnrollmentService enrollmentService;
  private final UserService userService;

  public EnrollmentController(EnrollmentService enrollmentService, UserService userService) {
    this.enrollmentService = enrollmentService;
    this.userService = userService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
  public ResponseEntity<EnrollmentDto> enrollStudent(
      @Valid @RequestBody EnrollmentRequest request, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(enrollmentService.enrollStudent(request, actor, isAdmin));
  }

  @GetMapping("/my")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<List<EnrollmentDto>> listMyEnrollments(Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(enrollmentService.listMyEnrollments(student));
  }

  @GetMapping("/courses/{courseId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<EnrollmentDto>> listCourseEnrollments(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(enrollmentService.listCourseEnrollments(courseId, actor, isAdmin));
  }

  @DeleteMapping("/{enrollmentId}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER')")
  public ResponseEntity<Void> removeEnrollment(
      @PathVariable Long enrollmentId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    enrollmentService.removeEnrollment(enrollmentId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }
}
