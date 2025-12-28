package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentSubmissionRequest {
  @NotNull
  private Long assignmentId;

  private String content;
  private String attachmentUrl;

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }
}
