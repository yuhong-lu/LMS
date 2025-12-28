package com.example.lms.controller;

import com.example.lms.dto.DiscussionReplyDto;
import com.example.lms.dto.DiscussionReplyRequest;
import com.example.lms.dto.DiscussionTopicDto;
import com.example.lms.dto.DiscussionTopicRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.User;
import com.example.lms.service.CourseService;
import com.example.lms.service.DiscussionService;
import com.example.lms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {
  private final DiscussionService discussionService;
  private final CourseService courseService;
  private final UserService userService;

  public DiscussionController(
      DiscussionService discussionService, CourseService courseService, UserService userService) {
    this.discussionService = discussionService;
    this.courseService = courseService;
    this.userService = userService;
  }

  @GetMapping("/courses/{courseId}/topics")
  public ResponseEntity<List<DiscussionTopicDto>> listTopics(
      @PathVariable Long courseId, Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(discussionService.listTopics(course, actor, isAdmin, isTeacher));
  }

  @PostMapping("/courses/{courseId}/topics")
  public ResponseEntity<DiscussionTopicDto> createTopic(
      @PathVariable Long courseId,
      @Valid @RequestBody DiscussionTopicRequest request,
      Authentication authentication) {
    Course course = courseService.getCourse(courseId);
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(
        discussionService.createTopic(course, request, actor, isAdmin, isTeacher));
  }

  @PutMapping("/topics/{topicId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<DiscussionTopicDto> updateTopic(
      @PathVariable Long topicId,
      @Valid @RequestBody DiscussionTopicRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    return ResponseEntity.ok(discussionService.updateTopic(topicId, request, actor, isAdmin));
  }

  @DeleteMapping("/topics/{topicId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteTopic(
      @PathVariable Long topicId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    discussionService.deleteTopic(topicId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/topics/{topicId}/replies")
  public ResponseEntity<List<DiscussionReplyDto>> listReplies(
      @PathVariable Long topicId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(discussionService.listReplies(topicId, actor, isAdmin, isTeacher));
  }

  @PostMapping("/topics/{topicId}/replies")
  public ResponseEntity<DiscussionReplyDto> addReply(
      @PathVariable Long topicId,
      @Valid @RequestBody DiscussionReplyRequest request,
      Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");
    return ResponseEntity.ok(discussionService.addReply(topicId, request, actor, isAdmin, isTeacher));
  }

  @DeleteMapping("/replies/{replyId}")
  @PreAuthorize("hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteReply(
      @PathVariable Long replyId, Authentication authentication) {
    User actor = userService.getByUsername(authentication.getName());
    boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
    discussionService.deleteReply(replyId, actor, isAdmin);
    return ResponseEntity.ok().build();
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .anyMatch(granted -> granted.getAuthority().equals(role));
  }
}
