package org.mephi.learning.service;

import java.util.*;
import java.util.stream.Collectors;
import org.mephi.learning.api.AnalyticsViews.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final UserRepository users;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository quizSubmissions;

    public AnalyticsService(
            UserRepository users,
            EnrollmentRepository enrollments,
            SubmissionRepository submissions,
            QuizSubmissionRepository quizSubmissions) {
        this.users = users;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.quizSubmissions = quizSubmissions;
    }

    @Transactional(readOnly = true)
    public StudentPerformance student(Long studentId) {
        User student =
                users.findById(studentId)
                        .orElseThrow(() -> DomainException.missing("Student", studentId));
        List<CoursePerformance> courses =
                enrollments.findByStudentId(studentId).stream()
                        .map(e -> coursePerformance(e.getCourse(), studentId))
                        .toList();
        return new StudentPerformance(
                studentId,
                student.getName(),
                courses,
                weightedAverage(courses.stream().map(CoursePerformance::overallPercent).toList()));
    }

    @Transactional(readOnly = true)
    public List<StudentRanking> ranking() {
        return users.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .map(u -> studentScores(u))
                .sorted(
                        Comparator.comparingDouble(StudentRanking::averagePercent)
                                .reversed()
                                .thenComparing(StudentRanking::studentName))
                .map((r) -> r)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    List<StudentRanking> ranked = new ArrayList<>();
                                    for (int i = 0; i < list.size(); i++) {
                                        StudentRanking r = list.get(i);
                                        ranked.add(
                                                new StudentRanking(
                                                        r.studentId(),
                                                        r.studentName(),
                                                        r.courses(),
                                                        r.gradedItems(),
                                                        r.averagePercent(),
                                                        i + 1));
                                    }
                                    return ranked;
                                }));
    }

    @Transactional(readOnly = true)
    public QuizStatistics quizStatistics() {
        List<QuizSubmission> all = quizSubmissions.findAll();
        Set<Long> students =
                all.stream().map(q -> q.getStudent().getId()).collect(Collectors.toSet());
        List<Double> percentages = all.stream().map(this::quizPercent).toList();
        long passed = percentages.stream().filter(p -> p >= 60.0).count();
        return new QuizStatistics(
                all.size(),
                students.size(),
                weightedAverage(percentages),
                all.isEmpty() ? 0 : passed * 100.0 / all.size());
    }

    private CoursePerformance coursePerformance(Course course, Long studentId) {
        List<Submission> ss =
                submissions.findByStudentId(studentId).stream()
                        .filter(
                                s ->
                                        s.getAssignment()
                                                .getLesson()
                                                .getModule()
                                                .getCourse()
                                                .getId()
                                                .equals(course.getId()))
                        .toList();
        List<QuizSubmission> qs =
                quizSubmissions.findByStudentId(studentId).stream()
                        .filter(
                                q ->
                                        q.getQuiz()
                                                .getModule()
                                                .getCourse()
                                                .getId()
                                                .equals(course.getId()))
                        .toList();
        List<Double> scores = new ArrayList<>();
        ss.stream()
                .filter(s -> s.getScore() != null)
                .forEach(s -> scores.add(s.getScore() * 100.0 / s.getAssignment().getMaxScore()));
        qs.stream().forEach(q -> scores.add(quizPercent(q)));
        return new CoursePerformance(
                course.getId(),
                course.getTitle(),
                ss.size(),
                assignmentCount(course),
                weightedAverage(
                        ss.stream()
                                .filter(s -> s.getScore() != null)
                                .map(s -> s.getScore() * 100.0 / s.getAssignment().getMaxScore())
                                .toList()),
                qs.size(),
                weightedAverage(qs.stream().map(this::quizPercent).toList()),
                weightedAverage(scores));
    }

    private StudentRanking studentScores(User u) {
        StudentPerformance p = student(u.getId());
        List<Double> scores = new ArrayList<>();
        submissions.findByStudentId(u.getId()).stream()
                .filter(s -> s.getScore() != null)
                .forEach(s -> scores.add(s.getScore() * 100.0 / s.getAssignment().getMaxScore()));
        quizSubmissions.findByStudentId(u.getId()).stream()
                .forEach(q -> scores.add(quizPercent(q)));
        return new StudentRanking(
                u.getId(),
                u.getName(),
                p.courses().size(),
                scores.size(),
                weightedAverage(scores),
                0);
    }

    private int assignmentCount(Course c) {
        return c.getModules().stream()
                .mapToInt(
                        m -> m.getLessons().stream().mapToInt(l -> l.getAssignments().size()).sum())
                .sum();
    }

    private double quizPercent(QuizSubmission q) {
        return q.getTotalQuestions() == 0 ? 0 : q.getScore() * 100.0 / q.getTotalQuestions();
    }

    private double weightedAverage(List<Double> values) {
        return values.isEmpty()
                ? 0
                : values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
