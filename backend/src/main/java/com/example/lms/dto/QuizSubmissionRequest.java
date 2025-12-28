package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuizSubmissionRequest {
  @NotNull
  private Long quizId;

  private List<QuizAnswerRequest> answers;

  public Long getQuizId() {
    return quizId;
  }

  public void setQuizId(Long quizId) {
    this.quizId = quizId;
  }

  public List<QuizAnswerRequest> getAnswers() {
    return answers;
  }

  public void setAnswers(List<QuizAnswerRequest> answers) {
    this.answers = answers;
  }
}
