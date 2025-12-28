package com.example.lms.dto;

import java.util.List;

public class GradeOverviewDto {
  private Long courseId;
  private List<AssignmentSubmissionDto> assignments;
  private List<QuizSubmissionDto> quizzes;

  public GradeOverviewDto(
      Long courseId, List<AssignmentSubmissionDto> assignments, List<QuizSubmissionDto> quizzes) {
    this.courseId = courseId;
    this.assignments = assignments;
    this.quizzes = quizzes;
  }

  public Long getCourseId() {
    return courseId;
  }

  public List<AssignmentSubmissionDto> getAssignments() {
    return assignments;
  }

  public List<QuizSubmissionDto> getQuizzes() {
    return quizzes;
  }
}
