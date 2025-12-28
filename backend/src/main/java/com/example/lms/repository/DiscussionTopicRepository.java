package com.example.lms.repository;

import com.example.lms.entity.DiscussionTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DiscussionTopicRepository extends JpaRepository<DiscussionTopic, Long> {
  @EntityGraph(attributePaths = {"course", "author"})
  List<DiscussionTopic> findAllByCourse_Id(Long courseId);
}
