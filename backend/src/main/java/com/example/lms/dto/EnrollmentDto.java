package com.example.lms.dto;

import java.time.Instant;

public class EnrollmentDto {
  private Long id;
  private Long courseId;
  private String courseTitle;
  private Long studentId;
  private String studentUsername;
  private String status;
  private Instant createdAt;

  public EnrollmentDto(
      Long id,
      Long courseId,
      String courseTitle,
      Long studentId,
      String studentUsername,
      String status,
      Instant createdAt) {
    this.id = id;
    this.courseId = courseId;
    this.courseTitle = courseTitle;
    this.studentId = studentId;
    this.studentUsername = studentUsername;
    this.status = status;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public String getCourseTitle() {
    return courseTitle;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentUsername() {
    return studentUsername;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
