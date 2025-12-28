package com.example.lms.service;

import com.example.lms.dto.CourseResourceDto;
import com.example.lms.dto.CourseResourceRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.CourseResource;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.ResourceType;
import com.example.lms.entity.User;
import com.example.lms.repository.CourseResourceRepository;
import com.example.lms.repository.EnrollmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseResourceService {
  private final CourseResourceRepository courseResourceRepository;
  private final EnrollmentRepository enrollmentRepository;

  public CourseResourceService(
      CourseResourceRepository courseResourceRepository,
      EnrollmentRepository enrollmentRepository) {
    this.courseResourceRepository = courseResourceRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  @Transactional
  public CourseResourceDto addResource(
      Course course, CourseResourceRequest request, User actor, boolean isAdmin) {
    ensureTeacherOwner(course, actor, isAdmin);
    CourseResource resource = new CourseResource();
    resource.setCourse(course);
    resource.setType(parseType(request.getType()));
    resource.setTitle(request.getTitle());
    resource.setUrl(request.getUrl());
    resource.setContent(request.getContent());
    return toDto(courseResourceRepository.save(resource));
  }

  @Transactional
  public CourseResourceDto updateResource(
      Long resourceId, CourseResourceRequest request, User actor, boolean isAdmin) {
    CourseResource resource =
        courseResourceRepository
            .findById(resourceId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    ensureTeacherOwner(resource.getCourse(), actor, isAdmin);
    resource.setType(parseType(request.getType()));
    resource.setTitle(request.getTitle());
    resource.setUrl(request.getUrl());
    resource.setContent(request.getContent());
    return toDto(courseResourceRepository.save(resource));
  }

  @Transactional
  public void deleteResource(Long resourceId, User actor, boolean isAdmin) {
    CourseResource resource =
        courseResourceRepository
            .findById(resourceId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    ensureTeacherOwner(resource.getCourse(), actor, isAdmin);
    courseResourceRepository.delete(resource);
  }

  public List<CourseResourceDto> listResources(
      Course course, User actor, boolean isAdmin, boolean isTeacher) {
    if (isAdmin || (isTeacher && course.getTeacher().getId().equals(actor.getId()))) {
      return toDtoList(course.getId());
    }
    Optional<Enrollment> enrollment =
        enrollmentRepository.findByCourse_IdAndStudent_Id(course.getId(), actor.getId());
    if (enrollment.isEmpty()) {
      throw new IllegalArgumentException("No permission to view resources");
    }
    return toDtoList(course.getId());
  }

  private List<CourseResourceDto> toDtoList(Long courseId) {
    return courseResourceRepository.findAllByCourse_Id(courseId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  private CourseResourceDto toDto(CourseResource resource) {
    return new CourseResourceDto(
        resource.getId(),
        resource.getCourse().getId(),
        resource.getType().name(),
        resource.getTitle(),
        resource.getUrl(),
        resource.getContent(),
        resource.getCreatedAt());
  }

  private void ensureTeacherOwner(Course course, User actor, boolean isAdmin) {
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission to manage resources");
    }
  }

  private ResourceType parseType(String type) {
    try {
      return ResourceType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid resource type");
    }
  }
}
