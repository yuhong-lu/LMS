package com.example.lms.dto;

import java.time.Instant;

public class CourseResourceDto {
  private Long id;
  private Long courseId;
  private String type;
  private String title;
  private String url;
  private String content;
  private Instant createdAt;

  public CourseResourceDto(
      Long id,
      Long courseId,
      String type,
      String title,
      String url,
      String content,
      Instant createdAt) {
    this.id = id;
    this.courseId = courseId;
    this.type = type;
    this.title = title;
    this.url = url;
    this.content = content;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
