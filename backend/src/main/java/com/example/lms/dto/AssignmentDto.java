package com.example.lms.dto;

import java.time.Instant;

public class AssignmentDto {
  private Long id;
  private Long courseId;
  private String title;
  private String description;
  private Instant dueAt;
  private Instant createdAt;
  private Instant updatedAt;

  public AssignmentDto(
      Long id,
      Long courseId,
      String title,
      String description,
      Instant dueAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.courseId = courseId;
    this.title = title;
    this.description = description;
    this.dueAt = dueAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Instant getDueAt() {
    return dueAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
