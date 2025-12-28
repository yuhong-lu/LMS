package com.example.lms.service;

import com.example.lms.dto.QuizAnswerDto;
import com.example.lms.dto.QuizAnswerGradeRequest;
import com.example.lms.dto.QuizAnswerRequest;
import com.example.lms.dto.QuizDto;
import com.example.lms.dto.QuizQuestionDto;
import com.example.lms.dto.QuizQuestionRequest;
import com.example.lms.dto.QuizRequest;
import com.example.lms.dto.QuizSubmissionDto;
import com.example.lms.dto.QuizSubmissionRequest;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.QuestionType;
import com.example.lms.entity.Quiz;
import com.example.lms.entity.QuizAnswer;
import com.example.lms.entity.QuizQuestion;
import com.example.lms.entity.QuizSubmission;
import com.example.lms.entity.User;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.QuizAnswerRepository;
import com.example.lms.repository.QuizQuestionRepository;
import com.example.lms.repository.QuizRepository;
import com.example.lms.repository.QuizSubmissionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
  private final QuizRepository quizRepository;
  private final QuizQuestionRepository questionRepository;
  private final QuizSubmissionRepository submissionRepository;
  private final QuizAnswerRepository answerRepository;
  private final EnrollmentRepository enrollmentRepository;

  public QuizService(
      QuizRepository quizRepository,
      QuizQuestionRepository questionRepository,
      QuizSubmissionRepository submissionRepository,
      QuizAnswerRepository answerRepository,
      EnrollmentRepository enrollmentRepository) {
    this.quizRepository = quizRepository;
    this.questionRepository = questionRepository;
    this.submissionRepository = submissionRepository;
    this.answerRepository = answerRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  @Transactional
  public QuizDto createQuiz(Course course, QuizRequest request, User actor, boolean isAdmin) {
    ensureTeacherOwner(course, actor, isAdmin);
    Quiz quiz = new Quiz();
    quiz.setCourse(course);
    quiz.setTitle(request.getTitle());
    quiz.setDescription(request.getDescription());
    quiz.setStartAt(request.getStartAt());
    quiz.setEndAt(request.getEndAt());
    return toDto(quizRepository.save(quiz));
  }

  @Transactional
  public QuizDto updateQuiz(Long quizId, QuizRequest request, User actor, boolean isAdmin) {
    Quiz quiz = getQuiz(quizId);
    ensureTeacherOwner(quiz.getCourse(), actor, isAdmin);
    quiz.setTitle(request.getTitle());
    quiz.setDescription(request.getDescription());
    quiz.setStartAt(request.getStartAt());
    quiz.setEndAt(request.getEndAt());
    return toDto(quizRepository.save(quiz));
  }

  @Transactional
  public void deleteQuiz(Long quizId, User actor, boolean isAdmin) {
    Quiz quiz = getQuiz(quizId);
    ensureTeacherOwner(quiz.getCourse(), actor, isAdmin);
    List<QuizSubmission> submissions = submissionRepository.findAllByQuiz_Id(quizId);
    for (QuizSubmission submission : submissions) {
      answerRepository.deleteAllBySubmission_Id(submission.getId());
    }
    submissionRepository.deleteAllByQuiz_Id(quizId);
    questionRepository.deleteAllByQuiz_Id(quizId);
    quizRepository.delete(quiz);
  }

  public Quiz getQuiz(Long quizId) {
    return quizRepository
        .findById(quizId)
        .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
  }

  public List<QuizDto> listQuizzes(Course course, User actor, boolean isAdmin, boolean isTeacher) {
    if (!isAdmin && !(isTeacher && course.getTeacher().getId().equals(actor.getId()))) {
      ensureEnrolled(course.getId(), actor.getId());
    }
    return quizRepository.findAllByCourse_Id(course.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public QuizQuestionDto addQuestion(
      Quiz quiz, QuizQuestionRequest request, User actor, boolean isAdmin) {
    ensureTeacherOwner(quiz.getCourse(), actor, isAdmin);
    QuizQuestion question = new QuizQuestion();
    question.setQuiz(quiz);
    question.setType(parseType(request.getType()));
    question.setContent(request.getContent());
    question.setOptions(request.getOptions());
    question.setCorrectAnswer(request.getCorrectAnswer());
    return toDto(questionRepository.save(question), true);
  }

  @Transactional
  public QuizQuestionDto updateQuestion(
      Long questionId, QuizQuestionRequest request, User actor, boolean isAdmin) {
    QuizQuestion question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));
    ensureTeacherOwner(question.getQuiz().getCourse(), actor, isAdmin);
    question.setType(parseType(request.getType()));
    question.setContent(request.getContent());
    question.setOptions(request.getOptions());
    question.setCorrectAnswer(request.getCorrectAnswer());
    return toDto(questionRepository.save(question), true);
  }

  @Transactional
  public void deleteQuestion(Long questionId, User actor, boolean isAdmin) {
    QuizQuestion question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));
    ensureTeacherOwner(question.getQuiz().getCourse(), actor, isAdmin);
    answerRepository.deleteAllByQuestion_Id(questionId);
    questionRepository.delete(question);
  }

  public List<QuizQuestionDto> listQuestions(
      Quiz quiz, User actor, boolean isAdmin, boolean isTeacher) {
    boolean includeAnswer = isAdmin || (isTeacher && quiz.getCourse().getTeacher().getId().equals(actor.getId()));
    return questionRepository.findAllByQuiz_Id(quiz.getId()).stream()
        .map(question -> toDto(question, includeAnswer))
        .collect(Collectors.toList());
  }

  @Transactional
  public QuizSubmissionDto submitQuiz(QuizSubmissionRequest request, User student) {
    Quiz quiz = getQuiz(request.getQuizId());
    ensureEnrolled(quiz.getCourse().getId(), student.getId());
    ensureWithinTime(quiz);

    if (submissionRepository.findByQuiz_IdAndStudent_Id(quiz.getId(), student.getId()).isPresent()) {
      throw new IllegalArgumentException("Quiz already submitted");
    }

    QuizSubmission submission = new QuizSubmission();
    submission.setQuiz(quiz);
    submission.setStudent(student);
    submission = submissionRepository.save(submission);

    List<QuizAnswerDto> answers = new ArrayList<>();
    int totalScore = 0;
    if (request.getAnswers() != null) {
      for (QuizAnswerRequest answerRequest : request.getAnswers()) {
        QuizQuestion question =
            questionRepository
                .findById(answerRequest.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!question.getQuiz().getId().equals(quiz.getId())) {
          throw new IllegalArgumentException("Question does not belong to quiz");
        }
        QuizAnswer answer = new QuizAnswer();
        answer.setSubmission(submission);
        answer.setQuestion(question);
        answer.setAnswer(answerRequest.getAnswer());
        if (question.getType() != QuestionType.TEXT && question.getCorrectAnswer() != null) {
          boolean correct = isCorrect(question, answerRequest.getAnswer());
          answer.setCorrect(correct);
          int score = correct ? 1 : 0;
          answer.setScore(score);
          totalScore += score;
        }
        answer = answerRepository.save(answer);
        answers.add(toDto(answer));
      }
    }

    submission.setScore(totalScore);
    submissionRepository.save(submission);

    return new QuizSubmissionDto(
        submission.getId(),
        submission.getQuiz().getId(),
        submission.getStudent().getId(),
        submission.getStudent().getUsername(),
        submission.getScore(),
        submission.getSubmittedAt(),
        answers);
  }

  @Transactional
  public QuizAnswerDto gradeAnswer(
      Long answerId, QuizAnswerGradeRequest request, User actor, boolean isAdmin) {
    QuizAnswer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
    ensureTeacherOwner(answer.getQuestion().getQuiz().getCourse(), actor, isAdmin);
    answer.setScore(request.getScore());
    answer.setCorrect(request.getCorrect());
    answer = answerRepository.save(answer);

    QuizSubmission submission = answer.getSubmission();
    Integer total = submission.getScore() == null ? 0 : submission.getScore();
    total = recalcSubmissionScore(submission.getId());
    submission.setScore(total);
    submissionRepository.save(submission);
    return toDto(answer);
  }

  public List<QuizSubmissionDto> listSubmissions(Long quizId, User actor, boolean isAdmin) {
    Quiz quiz = getQuiz(quizId);
    ensureTeacherOwner(quiz.getCourse(), actor, isAdmin);
    return submissionRepository.findAllByQuiz_Id(quizId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public List<QuizSubmissionDto> listMySubmissions(User student) {
    return submissionRepository.findAllByStudent_Id(student.getId()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  private int recalcSubmissionScore(Long submissionId) {
    return answerRepository.findAllBySubmission_Id(submissionId).stream()
        .map(QuizAnswer::getScore)
        .filter(score -> score != null)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private QuizDto toDto(Quiz quiz) {
    return new QuizDto(
        quiz.getId(),
        quiz.getCourse().getId(),
        quiz.getTitle(),
        quiz.getDescription(),
        quiz.getStartAt(),
        quiz.getEndAt(),
        quiz.getCreatedAt(),
        quiz.getUpdatedAt());
  }

  private QuizQuestionDto toDto(QuizQuestion question, boolean includeAnswer) {
    return new QuizQuestionDto(
        question.getId(),
        question.getQuiz().getId(),
        question.getType().name(),
        question.getContent(),
        question.getOptions(),
        includeAnswer ? question.getCorrectAnswer() : null,
        question.getCreatedAt());
  }

  public QuizSubmissionDto toDto(QuizSubmission submission) {
    List<QuizAnswerDto> answers =
        answerRepository.findAllBySubmission_Id(submission.getId()).stream()
            .sorted(Comparator.comparing(QuizAnswer::getId))
            .map(this::toDto)
            .collect(Collectors.toList());
    return new QuizSubmissionDto(
        submission.getId(),
        submission.getQuiz().getId(),
        submission.getStudent().getId(),
        submission.getStudent().getUsername(),
        submission.getScore(),
        submission.getSubmittedAt(),
        answers);
  }

  private QuizAnswerDto toDto(QuizAnswer answer) {
    return new QuizAnswerDto(
        answer.getId(),
        answer.getQuestion().getId(),
        answer.getAnswer(),
        answer.getCorrect(),
        answer.getScore());
  }

  private void ensureTeacherOwner(Course course, User actor, boolean isAdmin) {
    if (!isAdmin && !course.getTeacher().getId().equals(actor.getId())) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private void ensureEnrolled(Long courseId, Long studentId) {
    Optional<Enrollment> enrollment =
        enrollmentRepository.findByCourse_IdAndStudent_Id(courseId, studentId);
    if (enrollment.isEmpty()) {
      throw new IllegalArgumentException("No permission");
    }
  }

  private void ensureWithinTime(Quiz quiz) {
    Instant now = Instant.now();
    if (quiz.getStartAt() != null && now.isBefore(quiz.getStartAt())) {
      throw new IllegalArgumentException("Quiz not started");
    }
    if (quiz.getEndAt() != null && now.isAfter(quiz.getEndAt())) {
      throw new IllegalArgumentException("Quiz ended");
    }
  }

  private QuestionType parseType(String type) {
    try {
      return QuestionType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid question type");
    }
  }

  private boolean isCorrect(QuizQuestion question, String answer) {
    if (answer == null) {
      return false;
    }
    if (question.getType() == QuestionType.MULTI) {
      return normalizeMulti(answer).equals(normalizeMulti(question.getCorrectAnswer()));
    }
    return answer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
  }

  private String normalizeMulti(String value) {
    if (value == null) {
      return "";
    }
    return value
        .trim()
        .replace(" ", "")
        .toUpperCase()
        .chars()
        .filter(ch -> ch != ',')
        .sorted()
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
