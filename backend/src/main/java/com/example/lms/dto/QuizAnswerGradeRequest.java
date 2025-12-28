package com.example.lms.dto;

public class QuizAnswerGradeRequest {
  private Integer score;
  private Boolean correct;

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Boolean getCorrect() {
    return correct;
  }

  public void setCorrect(Boolean correct) {
    this.correct = correct;
  }
}
