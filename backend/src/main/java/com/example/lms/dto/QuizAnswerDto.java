package com.example.lms.dto;

public class QuizAnswerDto {
  private Long id;
  private Long questionId;
  private String answer;
  private Boolean correct;
  private Integer score;

  public QuizAnswerDto(Long id, Long questionId, String answer, Boolean correct, Integer score) {
    this.id = id;
    this.questionId = questionId;
    this.answer = answer;
    this.correct = correct;
    this.score = score;
  }

  public Long getId() {
    return id;
  }

  public Long getQuestionId() {
    return questionId;
  }

  public String getAnswer() {
    return answer;
  }

  public Boolean getCorrect() {
    return correct;
  }

  public Integer getScore() {
    return score;
  }
}
