package com.example.lms.dto;

import java.time.Instant;

public class QuizQuestionDto {
  private Long id;
  private Long quizId;
  private String type;
  private String content;
  private String options;
  private String correctAnswer;
  private Instant createdAt;

  public QuizQuestionDto(
      Long id,
      Long quizId,
      String type,
      String content,
      String options,
      String correctAnswer,
      Instant createdAt) {
    this.id = id;
    this.quizId = quizId;
    this.type = type;
    this.content = content;
    this.options = options;
    this.correctAnswer = correctAnswer;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getQuizId() {
    return quizId;
  }

  public String getType() {
    return type;
  }

  public String getContent() {
    return content;
  }

  public String getOptions() {
    return options;
  }

  public String getCorrectAnswer() {
    return correctAnswer;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
