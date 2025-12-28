package com.example.lms.repository;

import com.example.lms.entity.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
  List<Quiz> findAllByCourse_Id(Long courseId);
  void deleteAllByCourse_Id(Long courseId);
}
