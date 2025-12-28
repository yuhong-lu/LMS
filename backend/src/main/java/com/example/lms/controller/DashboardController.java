package com.example.lms.controller;

import com.example.lms.dto.DashboardSummaryDto;
import com.example.lms.repository.AssignmentSubmissionRepository;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.QuizSubmissionRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.entity.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final QuizSubmissionRepository quizSubmissionRepository;

  public DashboardController(
      UserRepository userRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      AssignmentSubmissionRepository assignmentSubmissionRepository,
      QuizSubmissionRepository quizSubmissionRepository) {
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    this.quizSubmissionRepository = quizSubmissionRepository;
  }

  @GetMapping("/summary")
  public DashboardSummaryDto summary() {
    return new DashboardSummaryDto(
        userRepository.count(),
        userRepository.countByRole(UserRole.ROLE_STUDENT),
        userRepository.countByRole(UserRole.ROLE_TEACHER),
        courseRepository.count(),
        enrollmentRepository.count(),
        assignmentSubmissionRepository.count(),
        quizSubmissionRepository.count());
  }
}
