package com.example.lms.dto;

import java.time.Instant;
import java.util.List;

public class QuizSubmissionDto {
  private Long id;
  private Long quizId;
  private Long studentId;
  private String studentUsername;
  private Integer score;
  private Instant submittedAt;
  private List<QuizAnswerDto> answers;

  public QuizSubmissionDto(
      Long id,
      Long quizId,
      Long studentId,
      String studentUsername,
      Integer score,
      Instant submittedAt,
      List<QuizAnswerDto> answers) {
    this.id = id;
    this.quizId = quizId;
    this.studentId = studentId;
    this.studentUsername = studentUsername;
    this.score = score;
    this.submittedAt = submittedAt;
    this.answers = answers;
  }

  public Long getId() {
    return id;
  }

  public Long getQuizId() {
    return quizId;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentUsername() {
    return studentUsername;
  }

  public Integer getScore() {
    return score;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public List<QuizAnswerDto> getAnswers() {
    return answers;
  }
}
