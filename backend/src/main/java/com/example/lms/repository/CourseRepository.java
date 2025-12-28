package com.example.lms.repository;

import com.example.lms.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CourseRepository extends JpaRepository<Course, Long> {
  @EntityGraph(attributePaths = {"teacher"})
  Optional<Course> findById(Long id);

  @EntityGraph(attributePaths = {"teacher"})
  List<Course> findAll();

  @EntityGraph(attributePaths = {"teacher"})
  List<Course> findAllByTeacher_Id(Long teacherId);
}
