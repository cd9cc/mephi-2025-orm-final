package org.mephi.learning.config;

import java.util.*;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.UserRepository;
import org.mephi.learning.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoData implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoData.class);
    private final UserRepository repository;
    private final UserService users;
    private final CourseService courses;
    private final ContentService content;
    private final EnrollmentService enrollments;
    private final AssignmentService assignments;
    private final QuizService quizzes;

    public DemoData(
            UserRepository repository,
            UserService users,
            CourseService courses,
            ContentService content,
            EnrollmentService enrollments,
            AssignmentService assignments,
            QuizService quizzes) {
        this.repository = repository;
        this.users = users;
        this.courses = courses;
        this.content = content;
        this.enrollments = enrollments;
        this.assignments = assignments;
        this.quizzes = quizzes;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) return;
        var teacher = users.create(new UserInput("Alex Morgan", "alex@example.test", Role.TEACHER));
        var student = users.create(new UserInput("Sam Lee", "sam@example.test", Role.STUDENT));
        users.create(new UserInput("Dana Kim", "dana@example.test", Role.STUDENT));
        users.putProfile(
                student.id(), new ProfileInput("Learning Java and relational databases", null));
        var category = courses.createCategory(new NameInput("Software engineering"));
        var tag = courses.createTag(new NameInput("Java"));
        var course =
                courses.create(
                        new CourseInput(
                                "Hibernate foundations",
                                "Build a learning platform while exploring entities, relationships and transactions.",
                                teacher.id(),
                                category.id(),
                                Set.of(tag.id())));
        var module =
                content.addModule(
                        course.id(),
                        new ModuleInput("Mapping your domain", "Entities and relationships", 0));
        var lesson =
                content.addLesson(
                        module.id(),
                        new LessonInput(
                                "Your first entity",
                                "An entity maps a Java object to a database row. Give it an @Id and a no-argument constructor, then persist it through a repository.",
                                null,
                                0));
        content.addMaterial(
                lesson.id(),
                new MaterialInput(
                        "Hibernate user guide",
                        "https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html"));
        var assignment =
                content.addAssignment(
                        lesson.id(),
                        new AssignmentInput(
                                "Map a Course entity",
                                "Create a Course with an id, title and description.",
                                null,
                                100));
        content.addLesson(
                module.id(),
                new LessonInput(
                        "Relationships",
                        "Use @ManyToOne for a course teacher and @OneToMany for course modules. Keep both sides of a bidirectional relationship consistent.",
                        null,
                        1));
        var second =
                content.addModule(
                        course.id(),
                        new ModuleInput(
                                "Transactions and fetching",
                                "Work safely with lazy relationships",
                                1));
        content.addLesson(
                second.id(),
                new LessonInput(
                        "Lazy loading",
                        "Lazy collections are initialized inside an open persistence context. Map entities to DTOs in the service transaction before returning them through the API.",
                        null,
                        0));
        var quiz =
                quizzes.create(
                        module.id(),
                        new QuizInput(
                                "Entity essentials",
                                List.of(
                                        new QuestionInput(
                                                "Which annotation identifies an entity's primary key?",
                                                QuestionType.SINGLE_CHOICE,
                                                List.of(
                                                        new OptionInput("@Id", true),
                                                        new OptionInput("@Column", false))),
                                        new QuestionInput(
                                                "Which relationships can represent collections?",
                                                QuestionType.MULTIPLE_CHOICE,
                                                List.of(
                                                        new OptionInput("@OneToMany", true),
                                                        new OptionInput("@ManyToMany", true),
                                                        new OptionInput("@ManyToOne", false))))));
        enrollments.enroll(course.id(), student.id());
        var submission =
                assignments.submit(
                        assignment.id(),
                        new SubmissionInput(
                                student.id(),
                                "@Entity class Course { @Id @GeneratedValue Long id; String title; String description; }"));
        assignments.grade(
                submission.id(),
                new GradeInput(
                        teacher.id(),
                        90,
                        "Good start. Add a no-argument constructor and column constraints."));
        Map<Long, Set<Long>> answers = new HashMap<>();
        answers.put(
                quiz.questions().get(0).id(),
                Set.of(quiz.questions().get(0).options().get(0).id()));
        answers.put(
                quiz.questions().get(1).id(),
                Set.of(
                        quiz.questions().get(1).options().get(0).id(),
                        quiz.questions().get(1).options().get(1).id()));
        quizzes.take(quiz.id(), new QuizAttempt(student.id(), answers));
        enrollments.review(course.id(), student.id(), 5, "A useful introduction to ORM.");
        log.info(
                "Created demo course {} with teacher {}, student {}, assignment {} and quiz {}",
                course.id(),
                teacher.id(),
                student.id(),
                assignment.id(),
                quiz.id());
    }
}
