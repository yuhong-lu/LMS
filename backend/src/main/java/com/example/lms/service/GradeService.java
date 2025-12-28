package com.example.lms.service;

import com.example.lms.dto.AssignmentSubmissionDto;
import com.example.lms.dto.CourseGradeSummaryDto;
import com.example.lms.dto.GradeOverviewDto;
import com.example.lms.dto.QuizSubmissionDto;
import com.example.lms.dto.StudentGradeSummaryDto;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.User;
import com.example.lms.repository.AssignmentSubmissionRepository;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.QuizSubmissionRepository;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GradeService {
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final QuizSubmissionRepository quizSubmissionRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final AssignmentService assignmentService;
  private final QuizService quizService;

  public GradeService(
      AssignmentSubmissionRepository assignmentSubmissionRepository,
      QuizSubmissionRepository quizSubmissionRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      AssignmentService assignmentService,
      QuizService quizService) {
    this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    this.quizSubmissionRepository = quizSubmissionRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
    this.assignmentService = assignmentService;
    this.quizService = quizService;
  }

  public GradeOverviewDto getStudentOverview(Long courseId, User student) {
    ensureEnrolled(courseId, student.getId());
    List<AssignmentSubmissionDto> assignments =
        assignmentSubmissionRepository.findAllByStudent_IdAndAssignment_Course_Id(
                student.getId(), courseId)
            .stream()
            .map(assignmentService::toDto)
            .collect(Collectors.toList());
    List<QuizSubmissionDto> quizzes =
        quizSubmissionRepository.findAllByStudent_IdAndQuiz_Course_Id(student.getId(), courseId)
            .stream()
            .map(quizService::toDto)
            .collect(Collectors.toList());
    return new GradeOverviewDto(courseId, assignments, quizzes);
  }

  public CourseGradeSummaryDto getCourseSummary(Long courseId, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    ensureTeacherOwner(course, actor, isAdmin);

    List<Double> assignmentScores =
        assignmentSubmissionRepository.findAllByAssignment_Course_Id(courseId).stream()
            .map(submission -> submission.getScore())
            .filter(score -> score != null)
            .map(Integer::doubleValue)
            .collect(Collectors.toList());

    List<Double> quizScores =
        quizSubmissionRepository.findAllByQuiz_Course_Id(courseId).stream()
            .map(submission -> submission.getScore())
            .filter(score -> score != null)
            .map(Integer::doubleValue)
            .collect(Collectors.toList());

    Double assignmentAvg = average(assignmentScores);
    Double quizAvg = average(quizScores);

    return new CourseGradeSummaryDto(
        courseId, assignmentScores.size(), quizScores.size(), assignmentAvg, quizAvg);
  }

  public List<StudentGradeSummaryDto> listCourseStudentSummaries(
      Long courseId, User actor, boolean isAdmin) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    ensureTeacherOwner(course, actor, isAdmin);

    List<Enrollment> enrollments = enrollmentRepository.findAllByCourse_Id(courseId);
    Map<Long, List<Integer>> assignmentScoresByStudent =
        assignmentSubmissionRepository.findAllByAssignment_Course_Id(courseId).stream()
            .filter(submission -> submission.getScore() != null)
            .collect(
                Collectors.groupingBy(
                    submission -> submission.getStudent().getId(),
                    Collectors.mapping(
                        submission -> submission.getScore(), Collectors.toList())));

    Map<Long, List<Integer>> quizScoresByStudent =
        quizSubmissionRepository.findAllByQuiz_Course_Id(courseId).stream()
            .filter(submission -> submission.getScore() != null)
            .collect(
                Collectors.groupingBy(
                    submission -> submission.getStudent().getId(),
                    Collectors.mapping(
                        submission -> submission.getScore(), Collectors.toList())));

    Map<Long, User> studentMap =
        enrollments.stream()
            .map(Enrollment::getStudent)
            .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));

    return studentMap.values().stream()
        .map(
            student -> {
              List<Integer> assignmentScores = assignmentScoresByStudent.get(student.getId());
              List<Integer> quizScores = quizScoresByStudent.get(student.getId());
              Double assignmentAvg = averageInt(assignmentScores);
              Double quizAvg = averageInt(quizScores);
              int assignmentCount = assignmentScores == null ? 0 : assignmentScores.size();
              int quizCount = quizScores == null ? 0 : quizScores.size();
              return new StudentGradeSummaryDto(
                  student.getId(),
                  student.getUsername(),
                  assignmentAvg,
                  quizAvg,
                  assignmentCount,
                  quizCount);
            })
        .collect(Collectors.toList());
  }

  private void ensureEnrolled(Long courseId, Long studentId) {
    if (!enrollmentRepository.existsByCourse_IdAndStudent_Id(courseId, studentId)) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private void ensureTeacherOwner(Course course, User actor, boolean isAdmin) {
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private Double average(List<Double> values) {
    if (values.isEmpty()) {
      return null;
    }
    DoubleSummaryStatistics stats =
        values.stream().mapToDouble(Double::doubleValue).summaryStatistics();
    return stats.getAverage();
  }

  private Double averageInt(List<Integer> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    DoubleSummaryStatistics stats =
        values.stream().mapToDouble(Integer::doubleValue).summaryStatistics();
    return stats.getAverage();
  }
}
