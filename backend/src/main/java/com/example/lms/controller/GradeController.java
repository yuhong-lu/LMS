package com.example.lms.controller;

import com.example.lms.dto.CourseGradeSummaryDto;
import com.example.lms.dto.GradeOverviewDto;
import com.example.lms.dto.StudentGradeSummaryDto;
import com.example.lms.entity.User;
import com.example.lms.service.GradeService;
import com.example.lms.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grades")
public class GradeController {
  private final GradeService gradeService;
  private final UserService userService;

  public GradeController(GradeService gradeService, UserService userService) {
    this.gradeService = gradeService;
    this.userService = userService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<GradeOverviewDto> myGrades(
      @RequestParam Long courseId, Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(gradeService.getStudentOverview(courseId, student));
  }

  @GetMapping("/courses/{courseId}/summary")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseGradeSummaryDto> courseSummary(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(gradeService.getCourseSummary(courseId, actor, isAdmin));
  }

  @GetMapping("/courses/{courseId}/students")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<StudentGradeSummaryDto>> courseStudentSummaries(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(gradeService.listCourseStudentSummaries(courseId, actor, isAdmin));
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }
}
