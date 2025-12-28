package com.example.lms.dto;

import java.time.Instant;

public class DiscussionReplyDto {
  private Long id;
  private Long topicId;
  private Long authorId;
  private String authorUsername;
  private String content;
  private Instant createdAt;

  public DiscussionReplyDto(
      Long id,
      Long topicId,
      Long authorId,
      String authorUsername,
      String content,
      Instant createdAt) {
    this.id = id;
    this.topicId = topicId;
    this.authorId = authorId;
    this.authorUsername = authorUsername;
    this.content = content;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getTopicId() {
    return topicId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public String getAuthorUsername() {
    return authorUsername;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
