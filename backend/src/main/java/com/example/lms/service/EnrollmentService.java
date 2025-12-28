package com.example.lms.service;

import com.example.lms.dto.EnrollmentDto;
import com.example.lms.dto.EnrollmentRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.User;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  public EnrollmentService(
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      UserRepository userRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public EnrollmentDto enrollStudent(EnrollmentRequest request, User actor, boolean isAdmin) {
    if (enrollmentRepository.existsByCourse_IdAndStudent_Id(
        request.getCourseId(), request.getStudentId())) {
      throw new IllegalArgumentException("Student already enrolled");
    }
    Course course =
        courseRepository
            .findById(request.getCourseId())
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
    User student =
        userRepository
            .findById(request.getStudentId())
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

    Enrollment enrollment = new Enrollment();
    enrollment.setCourse(course);
    enrollment.setStudent(student);
    return toDto(enrollmentRepository.save(enrollment));
  }

  @Transactional(readOnly = true)
  public List<EnrollmentDto> listMyEnrollments(User student) {
    return enrollmentRepository.findAllByStudent_Id(student.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EnrollmentDto> listCourseEnrollments(Long courseId, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
    return enrollmentRepository.findAllByCourse_Id(courseId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void removeEnrollment(Long enrollmentId, User actor, boolean isAdmin) {
    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
    if (!isAdmin && !enrollment.getCourse().getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
    enrollmentRepository.delete(enrollment);
  }

  private EnrollmentDto toDto(Enrollment enrollment) {
    return new EnrollmentDto(
        enrollment.getId(),
        enrollment.getCourse().getId(),
        enrollment.getCourse().getTitle(),
        enrollment.getStudent().getId(),
        enrollment.getStudent().getUsername(),
        enrollment.getStatus().name(),
        enrollment.getCreatedAt());
  }
}
