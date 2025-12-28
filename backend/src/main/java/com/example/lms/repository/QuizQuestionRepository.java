package com.example.lms.repository;

import com.example.lms.entity.QuizQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
  List<QuizQuestion> findAllByQuiz_Id(Long quizId);
  void deleteAllByQuiz_Id(Long quizId);
}
