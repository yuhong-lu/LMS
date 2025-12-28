package com.example.lms.controller;

import com.example.lms.dto.AssignmentDto;
import com.example.lms.dto.AssignmentGradeRequest;
import com.example.lms.dto.AssignmentRequest;
import com.example.lms.dto.AssignmentSubmissionDto;
import com.example.lms.dto.AssignmentSubmissionRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.User;
import com.example.lms.service.AssignmentService;
import com.example.lms.service.CourseService;
import com.example.lms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
  private final AssignmentService assignmentService;
  private final CourseService courseService;
  private final UserService userService;

  public AssignmentController(
      AssignmentService assignmentService, CourseService courseService, UserService userService) {
    this.assignmentService = assignmentService;
    this.courseService = courseService;
    this.userService = userService;
  }

  @PostMapping("/courses/{courseId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<AssignmentDto> createAssignment(
      @PathVariable Long courseId,
      @Valid @RequestBody AssignmentRequest request,
      Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(assignmentService.createAssignment(course, request, actor, isAdmin));
  }

  @PutMapping("/{assignmentId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<AssignmentDto> updateAssignment(
      @PathVariable Long assignmentId,
      @Valid @RequestBody AssignmentRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, request, actor, isAdmin));
  }

  @DeleteMapping("/{assignmentId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteAssignment(
      @PathVariable Long assignmentId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    assignmentService.deleteAssignment(assignmentId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/courses/{courseId}")
  public ResponseEntity<List<AssignmentDto>> listAssignments(
      @PathVariable Long courseId, Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(assignmentService.listAssignments(course, actor, isAdmin, isTeacher));
  }

  @PostMapping("/submit")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<AssignmentSubmissionDto> submitAssignment(
      @Valid @RequestBody AssignmentSubmissionRequest request,
      Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(assignmentService.submitAssignment(request, student));
  }

  @GetMapping("/{assignmentId}/submissions")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<AssignmentSubmissionDto>> listSubmissions(
      @PathVariable Long assignmentId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(assignmentService.listSubmissions(assignmentId, actor, isAdmin));
  }

  @GetMapping("/courses/{courseId}/submissions")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<AssignmentSubmissionDto>> listCourseSubmissions(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(assignmentService.listCourseSubmissions(courseId, actor, isAdmin));
  }

  @GetMapping("/courses/{courseId}/submissions/export")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> exportCourseSubmissions(
      @PathVariable Long courseId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    List<AssignmentSubmissionDto> submissions =
        assignmentService.listCourseSubmissions(courseId, actor, isAdmin);
    String csv = buildCsv(submissions);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submissions.csv\"")
        .contentType(new MediaType("text", "csv"))
        .body(csv);
  }

  @GetMapping("/submissions/my")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<List<AssignmentSubmissionDto>> listMySubmissions(Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(assignmentService.listMySubmissions(student));
  }

  @PutMapping("/submissions/{submissionId}/grade")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<AssignmentSubmissionDto> gradeSubmission(
      @PathVariable Long submissionId,
      @RequestBody AssignmentGradeRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(assignmentService.gradeSubmission(submissionId, request, actor, isAdmin));
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }

  private String buildCsv(List<AssignmentSubmissionDto> submissions) {
    String header = "submissionId,assignmentId,studentId,studentUsername,score,submittedAt,gradedAt";
    return header
        + "\n"
        + submissions.stream()
            .map(
                submission ->
                    String.join(
                        ",",
                        String.valueOf(submission.getId()),
                        String.valueOf(submission.getAssignmentId()),
                        String.valueOf(submission.getStudentId()),
                        escapeCsv(submission.getStudentUsername()),
                        submission.getScore() == null ? "" : submission.getScore().toString(),
                        submission.getSubmittedAt() == null ? "" : submission.getSubmittedAt().toString(),
                        submission.getGradedAt() == null ? "" : submission.getGradedAt().toString()))
            .collect(Collectors.joining("\n"));
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }
}
