package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateCourseTeacherRequest {
  @NotNull
  private Long teacherId;

  public Long getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(Long teacherId) {
    this.teacherId = teacherId;
  }
}
