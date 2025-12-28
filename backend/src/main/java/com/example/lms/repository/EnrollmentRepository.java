package com.example.lms.repository;

import com.example.lms.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);

  @EntityGraph(attributePaths = {"course", "student"})
  Optional<Enrollment> findByCourse_IdAndStudent_Id(Long courseId, Long studentId);

  @EntityGraph(attributePaths = {"course", "student"})
  List<Enrollment> findAllByCourse_Id(Long courseId);

  @EntityGraph(attributePaths = {"course", "student"})
  List<Enrollment> findAllByStudent_Id(Long studentId);

  void deleteAllByCourse_Id(Long courseId);
}
