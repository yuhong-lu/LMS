package com.example.lms.repository;

import com.example.lms.entity.AssignmentSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
  Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
  List<AssignmentSubmission> findAllByAssignment_Id(Long assignmentId);
  void deleteAllByAssignment_Id(Long assignmentId);
  List<AssignmentSubmission> findAllByAssignment_Course_Id(Long courseId);
  List<AssignmentSubmission> findAllByStudent_Id(Long studentId);
  List<AssignmentSubmission> findAllByStudent_IdAndAssignment_Course_Id(
      Long studentId, Long courseId);
}
