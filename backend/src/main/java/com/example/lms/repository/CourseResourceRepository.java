package com.example.lms.repository;

import com.example.lms.entity.CourseResource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {
  List<CourseResource> findAllByCourse_Id(Long courseId);
  void deleteAllByCourse_Id(Long courseId);
}
