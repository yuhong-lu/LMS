package com.example.lms.repository;

import com.example.lms.entity.DiscussionReply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
  @EntityGraph(attributePaths = {"topic", "author"})
  List<DiscussionReply> findAllByTopic_Id(Long topicId);
  void deleteAllByTopic_Id(Long topicId);
}
