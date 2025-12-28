package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CourseRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  private String description;
  private String syllabus;

  private Long teacherId;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSyllabus() {
    return syllabus;
  }

  public void setSyllabus(String syllabus) {
    this.syllabus = syllabus;
  }

  public Long getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(Long teacherId) {
    this.teacherId = teacherId;
  }
}
