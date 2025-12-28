package com.example.lms.controller;

import com.example.lms.dto.QuizAnswerGradeRequest;
import com.example.lms.dto.QuizDto;
import com.example.lms.dto.QuizQuestionDto;
import com.example.lms.dto.QuizQuestionRequest;
import com.example.lms.dto.QuizRequest;
import com.example.lms.dto.QuizSubmissionDto;
import com.example.lms.dto.QuizSubmissionRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.Quiz;
import com.example.lms.entity.User;
import com.example.lms.service.CourseService;
import com.example.lms.service.QuizService;
import com.example.lms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
  private final QuizService quizService;
  private final CourseService courseService;
  private final UserService userService;

  public QuizController(QuizService quizService, CourseService courseService, UserService userService) {
    this.quizService = quizService;
    this.courseService = courseService;
    this.userService = userService;
  }

  @PostMapping("/courses/{courseId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<QuizDto> createQuiz(
      @PathVariable Long courseId,
      @Valid @RequestBody QuizRequest request,
      Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.createQuiz(course, request, actor, isAdmin));
  }

  @PutMapping("/{quizId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<QuizDto> updateQuiz(
      @PathVariable Long quizId,
      @Valid @RequestBody QuizRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.updateQuiz(quizId, request, actor, isAdmin));
  }

  @DeleteMapping("/{quizId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteQuiz(
      @PathVariable Long quizId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    quizService.deleteQuiz(quizId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/courses/{courseId}")
  public ResponseEntity<List<QuizDto>> listQuizzes(
      @PathVariable Long courseId, Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(quizService.listQuizzes(course, actor, isAdmin, isTeacher));
  }

  @PostMapping("/{quizId}/questions")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<QuizQuestionDto> addQuestion(
      @PathVariable Long quizId,
      @Valid @RequestBody QuizQuestionRequest request,
      Authentication authentication) {
    Quiz quiz = quizService.getQuiz(quizId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.addQuestion(quiz, request, actor, isAdmin));
  }

  @PutMapping("/questions/{questionId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<QuizQuestionDto> updateQuestion(
      @PathVariable Long questionId,
      @Valid @RequestBody QuizQuestionRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.updateQuestion(questionId, request, actor, isAdmin));
  }

  @DeleteMapping("/questions/{questionId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable Long questionId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    quizService.deleteQuestion(questionId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{quizId}/questions")
  public ResponseEntity<List<QuizQuestionDto>> listQuestions(
      @PathVariable Long quizId, Authentication authentication) {
    Quiz quiz = quizService.getQuiz(quizId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(quizService.listQuestions(quiz, actor, isAdmin, isTeacher));
  }

  @PostMapping("/submit")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<QuizSubmissionDto> submitQuiz(
      @Valid @RequestBody QuizSubmissionRequest request,
      Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(quizService.submitQuiz(request, student));
  }

  @GetMapping("/{quizId}/submissions")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<List<QuizSubmissionDto>> listSubmissions(
      @PathVariable Long quizId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.listSubmissions(quizId, actor, isAdmin));
  }

  @GetMapping("/submissions/my")
  @PreAuthorize("hasAuthority('ROLE_STUDENT')")
  public ResponseEntity<List<QuizSubmissionDto>> listMySubmissions(Authentication authentication) {
    User student = userService.getByUsername(authentication.getName());
    return ResponseEntity.ok(quizService.listMySubmissions(student));
  }

  @PutMapping("/answers/{answerId}/grade")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<?> gradeAnswer(
      @PathVariable Long answerId,
      @RequestBody QuizAnswerGradeRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(quizService.gradeAnswer(answerId, request, actor, isAdmin));
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }
}
