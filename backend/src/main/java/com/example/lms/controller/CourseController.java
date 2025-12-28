package com.example.lms.controller;

import com.example.lms.dto.CourseDto;
import com.example.lms.dto.CourseRequest;
import com.example.lms.dto.CourseResourceDto;
import com.example.lms.dto.CourseResourceRequest;
import com.example.lms.dto.UpdateCourseTeacherRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.User;
import com.example.lms.service.CourseResourceService;
import com.example.lms.service.CourseService;
import com.example.lms.service.EnrollmentService;
import com.example.lms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  private final CourseService courseService;
  private final CourseResourceService courseResourceService;
  private final EnrollmentService enrollmentService;
  private final UserService userService;

  public CourseController(
      CourseService courseService,
      CourseResourceService courseResourceService,
      EnrollmentService enrollmentService,
      UserService userService) {
    this.courseService = courseService;
    this.courseResourceService = courseResourceService;
    this.enrollmentService = enrollmentService;
    this.userService = userService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseDto> createCourse(
      @Valid @RequestBody CourseRequest request, Authentication authentication) {
    if (request.getTeacherId() == null) {
      throw new IllegalArgumentException("Teacher is required for course creation");
    }
    User teacher = userService.getById(request.getTeacherId());
    return ResponseEntity.ok(courseService.createCourse(request, teacher));
  }

  @PutMapping("/{courseId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseDto> updateCourse(
      @PathVariable Long courseId,
      @Valid @RequestBody CourseRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(courseService.updateCourse(courseId, request, actor, isAdmin));
  }

  @DeleteMapping("/{courseId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteCourse(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    courseService.deleteCourse(courseId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{courseId}/teacher")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseDto> updateTeacher(
      @PathVariable Long courseId,
      @Valid @RequestBody UpdateCourseTeacherRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    User newTeacher = userService.getById(request.getTeacherId());
    return ResponseEntity.ok(courseService.updateTeacher(courseId, newTeacher, actor, true));
  }

  @GetMapping
  public ResponseEntity<List<CourseDto>> listCourses(Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    if (hasRole(authentication, "ROLE_ADMIN")) {
      return ResponseEntity.ok(courseService.listAllCourses());
    }
    if (hasRole(authentication, "ROLE_TEACHER")) {
      return ResponseEntity.ok(courseService.listTeacherCourses(actor));
    }
    return ResponseEntity.ok(
        enrollmentService.listMyEnrollments(actor).stream()
            .map(enrollment -> courseService.getCourse(enrollment.getCourseId()))
            .map(courseService::toDto)
            .toList());
  }

  @GetMapping("/{courseId}")
  public ResponseEntity<CourseDto> getCourse(
      @PathVariable Long courseId, Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    if (hasRole(authentication, "ROLE_ADMIN")) {
      return ResponseEntity.ok(courseService.toDto(course));
    }
    User actor = userService.getByUsername(authentication.getName());
    if (hasRole(authentication, "ROLE_TEACHER") && course.getTeacher().getId().equals(actor.getId())) {
      return ResponseEntity.ok(courseService.toDto(course));
    }
    boolean enrolled =
        enrollmentService.listMyEnrollments(actor).stream()
            .anyMatch(enrollment -> enrollment.getCourseId().equals(courseId));
    if (!enrolled) {
      throw new IllegalArgumentException("No permission to view course");
    }
    return ResponseEntity.ok(courseService.toDto(course));
  }

  @GetMapping("/{courseId}/resources")
  public ResponseEntity<List<CourseResourceDto>> listResources(
      @PathVariable Long courseId, Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(
        courseResourceService.listResources(course, actor, isAdmin, isTeacher));
  }

  @PostMapping("/{courseId}/resources")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseResourceDto> addResource(
      @PathVariable Long courseId,
      @Valid @RequestBody CourseResourceRequest request,
      Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(courseResourceService.addResource(course, request, actor, isAdmin));
  }

  @PutMapping("/resources/{resourceId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<CourseResourceDto> updateResource(
      @PathVariable Long resourceId,
      @Valid @RequestBody CourseResourceRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(
        courseResourceService.updateResource(resourceId, request, actor, isAdmin));
  }

  @DeleteMapping("/resources/{resourceId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteResource(
      @PathVariable Long resourceId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    courseResourceService.deleteResource(resourceId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }
}
