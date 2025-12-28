package com.example.lms.dto;

import java.time.Instant;

public class CourseDto {
  private Long id;
  private String title;
  private String description;
  private String syllabus;
  private Long teacherId;
  private String teacherUsername;
  private Instant createdAt;
  private Instant updatedAt;

  public CourseDto(
      Long id,
      String title,
      String description,
      String syllabus,
      Long teacherId,
      String teacherUsername,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.syllabus = syllabus;
    this.teacherId = teacherId;
    this.teacherUsername = teacherUsername;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getSyllabus() {
    return syllabus;
  }

  public Long getTeacherId() {
    return teacherId;
  }

  public String getTeacherUsername() {
    return teacherUsername;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
