package com.example.lms.repository;

import com.example.lms.entity.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
  List<Assignment> findAllByCourse_Id(Long courseId);
  void deleteAllByCourse_Id(Long courseId);
}
