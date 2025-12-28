package com.example.lms.dto;

public class DashboardSummaryDto {
  private long totalUsers;
  private long totalStudents;
  private long totalTeachers;
  private long totalCourses;
  private long totalEnrollments;
  private long assignmentSubmissions;
  private long quizSubmissions;

  public DashboardSummaryDto(
      long totalUsers,
      long totalStudents,
      long totalTeachers,
      long totalCourses,
      long totalEnrollments,
      long assignmentSubmissions,
      long quizSubmissions) {
    this.totalUsers = totalUsers;
    this.totalStudents = totalStudents;
    this.totalTeachers = totalTeachers;
    this.totalCourses = totalCourses;
    this.totalEnrollments = totalEnrollments;
    this.assignmentSubmissions = assignmentSubmissions;
    this.quizSubmissions = quizSubmissions;
  }

  public long getTotalUsers() {
    return totalUsers;
  }

  public long getTotalStudents() {
    return totalStudents;
  }

  public long getTotalTeachers() {
    return totalTeachers;
  }

  public long getTotalCourses() {
    return totalCourses;
  }

  public long getTotalEnrollments() {
    return totalEnrollments;
  }

  public long getAssignmentSubmissions() {
    return assignmentSubmissions;
  }

  public long getQuizSubmissions() {
    return quizSubmissions;
  }
}
