package org.mephi.learning.api;

import java.time.Instant;
import java.util.List;
import org.mephi.learning.entity.*;

public final class Views {
    private Views() {}

    public record UserView(Long id, String name, String email, Role role) {}

    public record ProfileView(Long id, Long userId, String bio, String avatarUrl) {}

    public record NameView(Long id, String name) {}

    public record CourseSummary(
            Long id, String title, String description, UserView teacher, NameView category) {}

    public record CourseView(
            Long id,
            String title,
            String description,
            UserView teacher,
            NameView category,
            List<NameView> tags,
            List<ModuleView> modules) {}

    public record ModuleView(
            Long id,
            String title,
            String description,
            int orderIndex,
            List<LessonView> lessons,
            Long quizId) {}

    public record LessonView(
            Long id,
            String title,
            String content,
            String videoUrl,
            int orderIndex,
            List<AssignmentView> assignments,
            List<MaterialView> materials) {}

    public record MaterialView(Long id, String title, String url) {}

    public record AssignmentView(
            Long id,
            Long lessonId,
            String title,
            String description,
            Instant dueDate,
            int maxScore) {}

    public record EnrollmentView(
            Long id,
            UserView student,
            CourseSummary course,
            Instant enrolledAt,
            EnrollmentStatus status) {}

    public record SubmissionView(
            Long id,
            Long assignmentId,
            UserView student,
            String content,
            Instant submittedAt,
            Integer score,
            String feedback) {}

    public record QuizView(Long id, Long moduleId, String title, List<QuestionView> questions) {}

    public record QuestionView(Long id, String text, QuestionType type, List<OptionView> options) {}

    public record OptionView(Long id, String text) {}

    public record ResultView(
            Long id,
            Long quizId,
            UserView student,
            int score,
            int totalQuestions,
            Instant takenAt) {}

    public record ReviewView(
            Long id, UserView student, int rating, String comment, Instant createdAt) {}

    public static UserView user(User u) {
        return new UserView(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }

    public static CourseSummary summary(Course c) {
        return new CourseSummary(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                user(c.getTeacher()),
                new NameView(c.getCategory().getId(), c.getCategory().getName()));
    }

    public static CourseView course(Course c) {
        var s = summary(c);
        return new CourseView(
                s.id(),
                s.title(),
                s.description(),
                s.teacher(),
                s.category(),
                c.getTags().stream()
                        .map(t -> new NameView(t.getId(), t.getName()))
                        .sorted(java.util.Comparator.comparing(NameView::id))
                        .toList(),
                c.getModules().stream().map(Views::module).toList());
    }

    public static ModuleView module(CourseModule m) {
        return new ModuleView(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getOrderIndex(),
                m.getLessons().stream().map(Views::lesson).toList(),
                m.getQuiz() == null ? null : m.getQuiz().getId());
    }

    public static LessonView lesson(Lesson l) {
        return new LessonView(
                l.getId(),
                l.getTitle(),
                l.getContent(),
                l.getVideoUrl(),
                l.getOrderIndex(),
                l.getAssignments().stream().map(Views::assignment).toList(),
                l.getMaterials().stream().map(Views::material).toList());
    }

    public static MaterialView material(LessonMaterial m) {
        return new MaterialView(m.getId(), m.getTitle(), m.getUrl());
    }

    public static AssignmentView assignment(Assignment a) {
        return new AssignmentView(
                a.getId(),
                a.getLesson().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueDate(),
                a.getMaxScore());
    }

    public static EnrollmentView enrollment(Enrollment e) {
        return new EnrollmentView(
                e.getId(),
                user(e.getStudent()),
                summary(e.getCourse()),
                e.getEnrolledAt(),
                e.getStatus());
    }

    public static SubmissionView submission(Submission s) {
        return new SubmissionView(
                s.getId(),
                s.getAssignment().getId(),
                user(s.getStudent()),
                s.getContent(),
                s.getSubmittedAt(),
                s.getScore(),
                s.getFeedback());
    }

    public static QuizView quiz(Quiz q) {
        return new QuizView(
                q.getId(),
                q.getModule().getId(),
                q.getTitle(),
                q.getQuestions().stream()
                        .map(
                                question ->
                                        new QuestionView(
                                                question.getId(),
                                                question.getText(),
                                                question.getType(),
                                                question.getOptions().stream()
                                                        .map(
                                                                o ->
                                                                        new OptionView(
                                                                                o.getId(),
                                                                                o.getText()))
                                                        .toList()))
                        .toList());
    }

    public static ResultView result(QuizSubmission r) {
        return new ResultView(
                r.getId(),
                r.getQuiz().getId(),
                user(r.getStudent()),
                r.getScore(),
                r.getTotalQuestions(),
                r.getTakenAt());
    }

    public static ReviewView review(CourseReview r) {
        return new ReviewView(
                r.getId(), user(r.getStudent()), r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
