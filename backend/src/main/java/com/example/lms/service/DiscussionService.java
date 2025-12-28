package com.example.lms.service;

import com.example.lms.dto.DiscussionReplyDto;
import com.example.lms.dto.DiscussionReplyRequest;
import com.example.lms.dto.DiscussionTopicDto;
import com.example.lms.dto.DiscussionTopicRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.DiscussionReply;
import com.example.lms.entity.DiscussionTopic;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.User;
import com.example.lms.repository.DiscussionReplyRepository;
import com.example.lms.repository.DiscussionTopicRepository;
import com.example.lms.repository.EnrollmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscussionService {
  private final DiscussionTopicRepository topicRepository;
  private final DiscussionReplyRepository replyRepository;
  private final EnrollmentRepository enrollmentRepository;

  public DiscussionService(
      DiscussionTopicRepository topicRepository,
      DiscussionReplyRepository replyRepository,
      EnrollmentRepository enrollmentRepository) {
    this.topicRepository = topicRepository;
    this.replyRepository = replyRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  @Transactional(readOnly = true)
  public List<DiscussionTopicDto> listTopics(Course course, User actor, boolean isAdmin, boolean isTeacher) {
    ensureParticipant(course, actor, isAdmin, isTeacher);
    return topicRepository.findAllByCourse_Id(course.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public DiscussionTopicDto createTopic(
      Course course, DiscussionTopicRequest request, User actor, boolean isAdmin, boolean isTeacher) {
    ensureParticipant(course, actor, isAdmin, isTeacher);
    DiscussionTopic topic = new DiscussionTopic();
    topic.setCourse(course);
    topic.setAuthor(actor);
    topic.setTitle(request.getTitle());
    topic.setContent(request.getContent());
    return toDto(topicRepository.save(topic));
  }

  @Transactional
  public DiscussionTopicDto updateTopic(
      Long topicId, DiscussionTopicRequest request, User actor, boolean isAdmin) {
    DiscussionTopic topic = getTopic(topicId);
    ensureTeacherOwner(topic.getCourse(), actor, isAdmin);
    topic.setTitle(request.getTitle());
    topic.setContent(request.getContent());
    return toDto(topicRepository.save(topic));
  }

  @Transactional
  public void deleteTopic(Long topicId, User actor, boolean isAdmin) {
    DiscussionTopic topic = getTopic(topicId);
    ensureTeacherOwner(topic.getCourse(), actor, isAdmin);
    topicRepository.delete(topic);
  }

  @Transactional(readOnly = true)
  public List<DiscussionReplyDto> listReplies(Long topicId, User actor, boolean isAdmin, boolean isTeacher) {
    DiscussionTopic topic = getTopic(topicId);
    ensureParticipant(topic.getCourse(), actor, isAdmin, isTeacher);
    return replyRepository.findAllByTopic_Id(topicId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public DiscussionReplyDto addReply(
      Long topicId, DiscussionReplyRequest request, User actor, boolean isAdmin, boolean isTeacher) {
    DiscussionTopic topic = getTopic(topicId);
    ensureParticipant(topic.getCourse(), actor, isAdmin, isTeacher);
    DiscussionReply reply = new DiscussionReply();
    reply.setTopic(topic);
    reply.setAuthor(actor);
    reply.setContent(request.getContent());
    return toDto(replyRepository.save(reply));
  }

  @Transactional
  public void deleteReply(Long replyId, User actor, boolean isAdmin) {
    DiscussionReply reply =
        replyRepository
            .findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("Reply not found"));
    if (!isAdmin && !reply.getTopic().getCourse().getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
    replyRepository.delete(reply);
  }

  private DiscussionTopic getTopic(Long topicId) {
    return topicRepository
        .findById(topicId)
        .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
  }

  private void ensureParticipant(Course course, User actor, boolean isAdmin, boolean isTeacher) {
    if (isAdmin || (isTeacher && course.getTeacher().getId().equals(actor.getId()))) {
      return;
    }
    Optional<Enrollment> enrollment =
        enrollmentRepository.findByCourse_IdAndStudent_Id(course.getId(), actor.getId());
    if (enrollment.isEmpty()) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private void ensureTeacherOwner(Course course, User actor, boolean isAdmin) {
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private DiscussionTopicDto toDto(DiscussionTopic topic) {
    return new DiscussionTopicDto(
        topic.getId(),
        topic.getCourse().getId(),
        topic.getAuthor().getId(),
        topic.getAuthor().getUsername(),
        topic.getTitle(),
        topic.getContent(),
        topic.getCreatedAt(),
        topic.getUpdatedAt());
  }

  private DiscussionReplyDto toDto(DiscussionReply reply) {
    return new DiscussionReplyDto(
        reply.getId(),
        reply.getTopic().getId(),
        reply.getAuthor().getId(),
        reply.getAuthor().getUsername(),
        reply.getContent(),
        reply.getCreatedAt());
  }
}
