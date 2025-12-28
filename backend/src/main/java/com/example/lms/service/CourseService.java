package com.example.lms.service;

import com.example.lms.dto.CourseDto;
import com.example.lms.dto.CourseRequest;
import com.example.lms.entity.Assignment;
import com.example.lms.entity.Course;
import com.example.lms.entity.DiscussionTopic;
import com.example.lms.entity.Quiz;
import com.example.lms.entity.QuizSubmission;
import com.example.lms.entity.User;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.AssignmentSubmissionRepository;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.CourseResourceRepository;
import com.example.lms.repository.DiscussionReplyRepository;
import com.example.lms.repository.DiscussionTopicRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.QuizAnswerRepository;
import com.example.lms.repository.QuizQuestionRepository;
import com.example.lms.repository.QuizRepository;
import com.example.lms.repository.QuizSubmissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {
  private final CourseRepository courseRepository;
  private final CourseResourceRepository courseResourceRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final QuizRepository quizRepository;
  private final QuizSubmissionRepository quizSubmissionRepository;
  private final QuizAnswerRepository quizAnswerRepository;
  private final QuizQuestionRepository quizQuestionRepository;
  private final DiscussionTopicRepository discussionTopicRepository;
  private final DiscussionReplyRepository discussionReplyRepository;

  public CourseService(
      CourseRepository courseRepository,
      CourseResourceRepository courseResourceRepository,
      EnrollmentRepository enrollmentRepository,
      AssignmentRepository assignmentRepository,
      AssignmentSubmissionRepository assignmentSubmissionRepository,
      QuizRepository quizRepository,
      QuizSubmissionRepository quizSubmissionRepository,
      QuizAnswerRepository quizAnswerRepository,
      QuizQuestionRepository quizQuestionRepository,
      DiscussionTopicRepository discussionTopicRepository,
      DiscussionReplyRepository discussionReplyRepository) {
    this.courseRepository = courseRepository;
    this.courseResourceRepository = courseResourceRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.assignmentRepository = assignmentRepository;
    this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    this.quizRepository = quizRepository;
    this.quizSubmissionRepository = quizSubmissionRepository;
    this.quizAnswerRepository = quizAnswerRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.discussionTopicRepository = discussionTopicRepository;
    this.discussionReplyRepository = discussionReplyRepository;
  }

  @Transactional
  public CourseDto createCourse(CourseRequest request, User teacher) {
    Course course = new Course();
    course.setTitle(request.getTitle());
    course.setDescription(request.getDescription());
    course.setSyllabus(request.getSyllabus());
    course.setTeacher(teacher);
    return toDto(courseRepository.save(course));
  }

  @Transactional
  public CourseDto updateCourse(Long courseId, CourseRequest request, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission to update course");
    }
    course.setTitle(request.getTitle());
    course.setDescription(request.getDescription());
    course.setSyllabus(request.getSyllabus());
    return toDto(courseRepository.save(course));
  }

  @Transactional
  public CourseDto updateTeacher(Long courseId, User newTeacher, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    if (!isAdmin) {
      throw new IllegalArgumentException("No permission to change teacher");
    }
    course.setTeacher(newTeacher);
    return toDto(courseRepository.save(course));
  }

  @Transactional
  public void deleteCourse(Long courseId, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission to delete course");
    }

    courseResourceRepository.deleteAllByCourse_Id(courseId);
    enrollmentRepository.deleteAllByCourse_Id(courseId);

    List<Assignment> assignments = assignmentRepository.findAllByCourse_Id(courseId);
    for (Assignment assignment : assignments) {
      assignmentSubmissionRepository.deleteAllByAssignment_Id(assignment.getId());
    }
    assignmentRepository.deleteAllByCourse_Id(courseId);

    List<Quiz> quizzes = quizRepository.findAllByCourse_Id(courseId);
    for (Quiz quiz : quizzes) {
      List<QuizSubmission> submissions = quizSubmissionRepository.findAllByQuiz_Id(quiz.getId());
      for (QuizSubmission submission : submissions) {
        quizAnswerRepository.deleteAllBySubmission_Id(submission.getId());
      }
      quizSubmissionRepository.deleteAllByQuiz_Id(quiz.getId());
      quizQuestionRepository.deleteAllByQuiz_Id(quiz.getId());
    }
    quizRepository.deleteAllByCourse_Id(courseId);

    List<DiscussionTopic> topics = discussionTopicRepository.findAllByCourse_Id(courseId);
    for (DiscussionTopic topic : topics) {
      discussionReplyRepository.deleteAllByTopic_Id(topic.getId());
    }
    discussionTopicRepository.deleteAll(topics);

    courseRepository.delete(course);
  }

  @Transactional(readOnly = true)
  public Course getCourse(Long courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new IllegalArgumentException("Course not found"));
  }

  @Transactional(readOnly = true)
  public List<CourseDto> listAllCourses() {
    return courseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<CourseDto> listTeacherCourses(User teacher) {
    return courseRepository.findAllByTeacher_Id(teacher.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public CourseDto toDto(Course course) {
    return new CourseDto(
        course.getId(),
        course.getTitle(),
        course.getDescription(),
        course.getSyllabus(),
        course.getTeacher().getId(),
        course.getTeacher().getUsername(),
        course.getCreatedAt(),
        course.getUpdatedAt());
  }
}
