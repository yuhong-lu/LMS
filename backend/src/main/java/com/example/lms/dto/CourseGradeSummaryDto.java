package com.example.lms.dto;

public class CourseGradeSummaryDto {
  private Long courseId;
  private int assignmentSubmissions;
  private int quizSubmissions;
  private Double assignmentAverage;
  private Double quizAverage;

  public CourseGradeSummaryDto(
      Long courseId,
      int assignmentSubmissions,
      int quizSubmissions,
      Double assignmentAverage,
      Double quizAverage) {
    this.courseId = courseId;
    this.assignmentSubmissions = assignmentSubmissions;
    this.quizSubmissions = quizSubmissions;
    this.assignmentAverage = assignmentAverage;
    this.quizAverage = quizAverage;
  }

  public Long getCourseId() {
    return courseId;
  }

  public int getAssignmentSubmissions() {
    return assignmentSubmissions;
  }

  public int getQuizSubmissions() {
    return quizSubmissions;
  }

  public Double getAssignmentAverage() {
    return assignmentAverage;
  }

  public Double getQuizAverage() {
    return quizAverage;
  }
}
