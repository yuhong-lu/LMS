package com.example.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "assignment_submissions",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"assignment_id", "student_id"})})
public class AssignmentSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignment_id", nullable = false)
  private Assignment assignment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(length = 500)
  private String attachmentUrl;

  private Integer score;

  @Column(columnDefinition = "TEXT")
  private String feedback;

  @Column(nullable = false, updatable = false)
  private Instant submittedAt;

  private Instant gradedAt;

  @PrePersist
  public void onCreate() {
    this.submittedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Assignment getAssignment() {
    return assignment;
  }

  public void setAssignment(Assignment assignment) {
    this.assignment = assignment;
  }

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
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

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public Instant getGradedAt() {
    return gradedAt;
  }

  public void setGradedAt(Instant gradedAt) {
    this.gradedAt = gradedAt;
  }
}
