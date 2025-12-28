package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;

public class QuizAnswerRequest {
  @NotNull
  private Long questionId;

  private String answer;

  public Long getQuestionId() {
    return questionId;
  }

  public void setQuestionId(Long questionId) {
    this.questionId = questionId;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }
}
