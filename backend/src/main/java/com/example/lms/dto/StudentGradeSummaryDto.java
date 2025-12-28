package com.example.lms.dto;

public class StudentGradeSummaryDto {
  private Long studentId;
  private String studentUsername;
  private Double assignmentAverage;
  private Double quizAverage;
  private int assignmentSubmissions;
  private int quizSubmissions;

  public StudentGradeSummaryDto(
      Long studentId,
      String studentUsername,
      Double assignmentAverage,
      Double quizAverage,
      int assignmentSubmissions,
      int quizSubmissions) {
    this.studentId = studentId;
    this.studentUsername = studentUsername;
    this.assignmentAverage = assignmentAverage;
    this.quizAverage = quizAverage;
    this.assignmentSubmissions = assignmentSubmissions;
    this.quizSubmissions = quizSubmissions;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentUsername() {
    return studentUsername;
  }

  public Double getAssignmentAverage() {
    return assignmentAverage;
  }

  public Double getQuizAverage() {
    return quizAverage;
  }

  public int getAssignmentSubmissions() {
    return assignmentSubmissions;
  }

  public int getQuizSubmissions() {
    return quizSubmissions;
  }
}
