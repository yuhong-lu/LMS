package com.example.lms.dto;

import java.time.Instant;

public class DiscussionTopicDto {
  private Long id;
  private Long courseId;
  private Long authorId;
  private String authorUsername;
  private String title;
  private String content;
  private Instant createdAt;
  private Instant updatedAt;

  public DiscussionTopicDto(
      Long id,
      Long courseId,
      Long authorId,
      String authorUsername,
      String title,
      String content,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.courseId = courseId;
    this.authorId = authorId;
    this.authorUsername = authorUsername;
    this.title = title;
    this.content = content;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public String getAuthorUsername() {
    return authorUsername;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
