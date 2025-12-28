package com.example.lms.dto;

import java.time.Instant;

public class QuizDto {
  private Long id;
  private Long courseId;
  private String title;
  private String description;
  private Instant startAt;
  private Instant endAt;
  private Instant createdAt;
  private Instant updatedAt;

  public QuizDto(
      Long id,
      Long courseId,
      String title,
      String description,
      Instant startAt,
      Instant endAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.courseId = courseId;
    this.title = title;
    this.description = description;
    this.startAt = startAt;
    this.endAt = endAt;
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

  public Instant getStartAt() {
    return startAt;
  }

  public Instant getEndAt() {
    return endAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
