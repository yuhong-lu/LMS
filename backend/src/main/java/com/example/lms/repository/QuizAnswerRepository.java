package com.example.lms.repository;

import com.example.lms.entity.QuizAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
  List<QuizAnswer> findAllBySubmission_Id(Long submissionId);
  List<QuizAnswer> findAllByQuestion_Id(Long questionId);
  void deleteAllBySubmission_Id(Long submissionId);
  void deleteAllByQuestion_Id(Long questionId);
}
