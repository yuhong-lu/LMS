package com.example.lms.repository;

import com.example.lms.entity.QuizSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
  Optional<QuizSubmission> findByQuiz_IdAndStudent_Id(Long quizId, Long studentId);
  List<QuizSubmission> findAllByQuiz_Id(Long quizId);
  void deleteAllByQuiz_Id(Long quizId);
  List<QuizSubmission> findAllByQuiz_Course_Id(Long courseId);
  List<QuizSubmission> findAllByStudent_Id(Long studentId);
  List<QuizSubmission> findAllByStudent_IdAndQuiz_Course_Id(Long studentId, Long courseId);
}
