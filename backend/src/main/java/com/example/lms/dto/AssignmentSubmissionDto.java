package com.example.lms.dto;

import java.time.Instant;

public class AssignmentSubmissionDto {
  private Long id;
  private Long assignmentId;
  private Long studentId;
  private String studentUsername;
  private String content;
  private String attachmentUrl;
  private Integer score;
  private String feedback;
  private Instant submittedAt;
  private Instant gradedAt;

  public AssignmentSubmissionDto(
      Long id,
      Long assignmentId,
      Long studentId,
      String studentUsername,
      String content,
      String attachmentUrl,
      Integer score,
      String feedback,
      Instant submittedAt,
      Instant gradedAt) {
    this.id = id;
    this.assignmentId = assignmentId;
    this.studentId = studentId;
    this.studentUsername = studentUsername;
    this.content = content;
    this.attachmentUrl = attachmentUrl;
    this.score = score;
    this.feedback = feedback;
    this.submittedAt = submittedAt;
    this.gradedAt = gradedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentUsername() {
    return studentUsername;
  }

  public String getContent() {
    return content;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public Integer getScore() {
    return score;
  }

  public String getFeedback() {
    return feedback;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public Instant getGradedAt() {
    return gradedAt;
  }
}
