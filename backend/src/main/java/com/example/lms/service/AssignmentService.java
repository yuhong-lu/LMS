package com.example.lms.service;

import com.example.lms.dto.AssignmentDto;
import com.example.lms.dto.AssignmentGradeRequest;
import com.example.lms.dto.AssignmentRequest;
import com.example.lms.dto.AssignmentSubmissionDto;
import com.example.lms.dto.AssignmentSubmissionRequest;
import com.example.lms.entity.Assignment;
import com.example.lms.entity.AssignmentSubmission;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.User;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.AssignmentSubmissionRepository;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository submissionRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;

  public AssignmentService(
      AssignmentRepository assignmentRepository,
      AssignmentSubmissionRepository submissionRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository) {
    this.assignmentRepository = assignmentRepository;
    this.submissionRepository = submissionRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
  }

  @Transactional
  public AssignmentDto createAssignment(Course course, AssignmentRequest request, User actor, boolean isAdmin) {
    ensureTeacherOwner(course, actor, isAdmin);
    Assignment assignment = new Assignment();
    assignment.setCourse(course);
    assignment.setTitle(request.getTitle());
    assignment.setDescription(request.getDescription());
    assignment.setDueAt(request.getDueAt());
    return toDto(assignmentRepository.save(assignment));
  }

  @Transactional
  public AssignmentDto updateAssignment(
      Long assignmentId, AssignmentRequest request, User actor, boolean isAdmin) {
    Assignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    ensureTeacherOwner(assignment.getCourse(), actor, isAdmin);
    assignment.setTitle(request.getTitle());
    assignment.setDescription(request.getDescription());
    assignment.setDueAt(request.getDueAt());
    return toDto(assignmentRepository.save(assignment));
  }

  @Transactional
  public void deleteAssignment(Long assignmentId, User actor, boolean isAdmin) {
    Assignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    ensureTeacherOwner(assignment.getCourse(), actor, isAdmin);
    submissionRepository.deleteAllByAssignment_Id(assignmentId);
    assignmentRepository.delete(assignment);
  }

  public List<AssignmentDto> listAssignments(Course course, User actor, boolean isAdmin, boolean isTeacher) {
    if (!isAdmin && !(isTeacher && course.getTeacher().getId().equals(actor.getId()))) {
      ensureEnrolled(course.getId(), actor.getId());
    }
    return assignmentRepository.findAllByCourse_Id(course.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public AssignmentSubmissionDto submitAssignment(AssignmentSubmissionRequest request, User student) {
    Assignment assignment =
        assignmentRepository
            .findById(request.getAssignmentId())
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    ensureEnrolled(assignment.getCourse().getId(), student.getId());

    Optional<AssignmentSubmission> existing =
        submissionRepository.findByAssignment_IdAndStudent_Id(
            assignment.getId(), student.getId());
    AssignmentSubmission submission =
        existing.orElseGet(
            () -> {
              AssignmentSubmission created = new AssignmentSubmission();
              created.setAssignment(assignment);
              created.setStudent(student);
              return created;
            });
    submission.setContent(request.getContent());
    submission.setAttachmentUrl(request.getAttachmentUrl());
    return toDto(submissionRepository.save(submission));
  }

  public List<AssignmentSubmissionDto> listSubmissions(Long assignmentId, User actor, boolean isAdmin) {
    Assignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    ensureTeacherOwner(assignment.getCourse(), actor, isAdmin);
    return submissionRepository.findAllByAssignment_Id(assignmentId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public List<AssignmentSubmissionDto> listCourseSubmissions(
      Long courseId, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    ensureTeacherOwner(course, actor, isAdmin);
    return submissionRepository.findAllByAssignment_Course_Id(courseId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public List<AssignmentSubmissionDto> listMySubmissions(User student) {
    return submissionRepository.findAllByStudent_Id(student.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public AssignmentSubmissionDto gradeSubmission(
      Long submissionId, AssignmentGradeRequest request, User actor, boolean isAdmin) {
    AssignmentSubmission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    ensureTeacherOwner(submission.getAssignment().getCourse(), actor, isAdmin);
    submission.setScore(request.getScore());
    submission.setFeedback(request.getFeedback());
    submission.setGradedAt(Instant.now());
    return toDto(submissionRepository.save(submission));
  }

  private void ensureTeacherOwner(Course course, User actor, boolean isAdmin) {
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private void ensureEnrolled(Long courseId, Long studentId) {
    Optional<Enrollment> enrollment =
        enrollmentRepository.findByCourse_IdAndStudent_Id(courseId, studentId);
    if (enrollment.isEmpty()) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private AssignmentDto toDto(Assignment assignment) {
    return new AssignmentDto(
        assignment.getId(),
        assignment.getCourse().getId(),
        assignment.getTitle(),
        assignment.getDescription(),
        assignment.getDueAt(),
        assignment.getCreatedAt(),
        assignment.getUpdatedAt());
  }

  public AssignmentSubmissionDto toDto(AssignmentSubmission submission) {
    return new AssignmentSubmissionDto(
        submission.getId(),
        submission.getAssignment().getId(),
        submission.getStudent().getId(),
        submission.getStudent().getUsername(),
        submission.getContent(),
        submission.getAttachmentUrl(),
        submission.getScore(),
        submission.getFeedback(),
        submission.getSubmittedAt(),
        submission.getGradedAt());
  }
}
