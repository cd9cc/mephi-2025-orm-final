package org.mephi.learning;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.*;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.mephi.learning.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlatformIntegrationTest {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("TEST_DB_URL");
        if (url != null && !url.isBlank()) {
            registry.add("spring.datasource.url", () -> url);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.datasource.username", () -> System.getenv("TEST_DB_USER"));
            registry.add("spring.datasource.password", () -> System.getenv("TEST_DB_PASSWORD"));
        }
    }

    @Autowired UserService users;
    @Autowired CourseService courses;
    @Autowired ContentService content;
    @Autowired EnrollmentService enrollments;
    @Autowired AssignmentService assignments;
    @Autowired QuizService quizzes;
    @Autowired CourseRepository courseRepository;
    @Autowired CourseModuleRepository moduleRepository;
    @Autowired LessonRepository lessonRepository;
    @Autowired AssignmentRepository assignmentRepository;
    @Autowired UserRepository userRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired QuizRepository quizRepository;
    @Autowired TagRepository tagRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired PlatformTransactionManager transactions;

    @Value("${local.server.port}")
    int port;

    UserView teacher, student;
    NameView category;
    CourseView course;
    ModuleView module;
    LessonView lesson;
    AssignmentView assignment;

    @BeforeEach
    void fixture() {
        String suffix = UUID.randomUUID().toString();
        teacher =
                users.create(
                        new UserInput(
                                "Teacher", "teacher-" + suffix + "@example.test", Role.TEACHER));
        student =
                users.create(
                        new UserInput(
                                "Student", "student-" + suffix + "@example.test", Role.STUDENT));
        category = courses.createCategory(new NameInput("Category " + suffix));
        course =
                courses.create(
                        new CourseInput(
                                "Hibernate",
                                "Original description",
                                teacher.id(),
                                category.id(),
                                Set.of()));
        module = content.addModule(course.id(), new ModuleInput("Module", "Description", 0));
        lesson = content.addLesson(module.id(), new LessonInput("Lesson", "Text", null, 0));
        assignment =
                content.addAssignment(
                        lesson.id(), new AssignmentInput("Assignment", "Map an entity", null, 100));
    }

    QuizInput quizInput() {
        return new QuizInput(
                "Quiz",
                List.of(
                        new QuestionInput(
                                "Pick the primary-key annotation",
                                QuestionType.SINGLE_CHOICE,
                                List.of(
                                        new OptionInput("@Id", true),
                                        new OptionInput("@Column", false))),
                        new QuestionInput(
                                "Pick collection mappings",
                                QuestionType.MULTIPLE_CHOICE,
                                List.of(
                                        new OptionInput("OneToMany", true),
                                        new OptionInput("ManyToMany", true),
                                        new OptionInput("ManyToOne", false)))));
    }

    Map<Long, Set<Long>> correct(QuizView quiz) {
        return Map.of(
                quiz.questions().get(0).id(),
                Set.of(quiz.questions().get(0).options().get(0).id()),
                quiz.questions().get(1).id(),
                Set.of(
                        quiz.questions().get(1).options().get(0).id(),
                        quiz.questions().get(1).options().get(1).id()));
    }

    SubmissionView submit() {
        enrollments.enroll(course.id(), student.id());
        return assignments.submit(
                assignment.id(), new SubmissionInput(student.id(), "My solution"));
    }

    @Test
    void courseStructureIsPersistedAndReadableOutsideTransaction() {
        CourseView loaded = courses.get(course.id());
        assertThat(loaded.modules()).hasSize(1);
        assertThat(loaded.modules().get(0).lessons().get(0).assignments())
                .extracting(AssignmentView::title)
                .containsExactly("Assignment");
        assertThat(loaded.teacher().id()).isEqualTo(teacher.id());
    }

    @Test
    void courseCanBeUpdatedAndFiltered() {
        courses.update(
                course.id(),
                new CourseInput("Renamed", "Updated", teacher.id(), category.id(), Set.of()));
        assertThat(courses.get(course.id()).description()).isEqualTo("Updated");
        assertThat(courses.list(category.id()))
                .extracting(CourseSummary::title)
                .containsExactly("Renamed");
    }

    @Test
    void courseRejectsStudentAsTeacher() {
        assertThatThrownBy(
                        () ->
                                courses.create(
                                        new CourseInput(
                                                "Bad", null, student.id(), category.id(), null)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("TEACHER");
    }

    @Test
    void deletingUnusedCourseCascadesContentButKeepsSharedData() {
        var tag = courses.createTag(new NameInput("Tag " + UUID.randomUUID()));
        courses.update(
                course.id(),
                new CourseInput("Tagged", null, teacher.id(), category.id(), Set.of(tag.id())));
        var quiz = quizzes.create(module.id(), quizInput());
        courses.delete(course.id());
        assertThat(courseRepository.existsById(course.id())).isFalse();
        assertThat(moduleRepository.existsById(module.id())).isFalse();
        assertThat(lessonRepository.existsById(lesson.id())).isFalse();
        assertThat(assignmentRepository.existsById(assignment.id())).isFalse();
        assertThat(quizRepository.existsById(quiz.id())).isFalse();
        assertThat(userRepository.existsById(teacher.id())).isTrue();
        assertThat(tagRepository.existsById(tag.id())).isTrue();
    }

    @Test
    void enrolledCourseCannotBeDeleted() {
        enrollments.enroll(course.id(), student.id());
        assertThatThrownBy(() -> courses.delete(course.id())).isInstanceOf(DomainException.class);
        assertThat(courseRepository.existsById(course.id())).isTrue();
    }

    @Test
    void lazyCollectionFailsWhenDetachedAndDtoWorks() {
        Course detached = courseRepository.findById(course.id()).orElseThrow();
        assertThat(Hibernate.isInitialized(detached.getModules())).isFalse();
        assertThatThrownBy(() -> detached.getModules().size())
                .isInstanceOf(LazyInitializationException.class);
        assertThat(courses.get(course.id()).modules().get(0).lessons()).hasSize(1);
    }

    @Test
    void cascadePersistStoresNewModuleAndLesson() {
        Long id =
                new TransactionTemplate(transactions)
                        .execute(
                                status -> {
                                    Course c = new Course();
                                    c.setTitle("Cascade");
                                    c.setCategory(new Category());
                                    c.setCategory(
                                            courseRepository
                                                    .findById(course.id())
                                                    .orElseThrow()
                                                    .getCategory());
                                    c.setTeacher(
                                            userRepository.findById(teacher.id()).orElseThrow());
                                    CourseModule m = new CourseModule();
                                    m.setCourse(c);
                                    m.setTitle("Cascaded module");
                                    c.getModules().add(m);
                                    Lesson l = new Lesson();
                                    l.setModule(m);
                                    l.setTitle("Cascaded lesson");
                                    m.getLessons().add(l);
                                    return courseRepository.saveAndFlush(c).getId();
                                });
        assertThat(courses.get(id).modules().get(0).lessons())
                .extracting(LessonView::title)
                .containsExactly("Cascaded lesson");
    }

    @Test
    void enrollmentListsAndUnenrollmentWork() {
        enrollments.enroll(course.id(), student.id());
        assertThat(enrollments.students(course.id()))
                .extracting(e -> e.student().id())
                .containsExactly(student.id());
        assertThat(enrollments.courses(student.id()))
                .extracting(e -> e.course().id())
                .containsExactly(course.id());
        enrollments.unenroll(course.id(), student.id());
        assertThat(enrollments.courses(student.id())).isEmpty();
    }

    @Test
    void duplicateEnrollmentIsRejected() {
        enrollments.enroll(course.id(), student.id());
        assertThatThrownBy(() -> enrollments.enroll(course.id(), student.id()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    void databaseEnforcesEnrollmentUniqueness() {
        enrollments.enroll(course.id(), student.id());
        assertThatThrownBy(
                        () ->
                                new TransactionTemplate(transactions)
                                        .execute(
                                                status -> {
                                                    Enrollment duplicate = new Enrollment();
                                                    duplicate.setStudent(
                                                            userRepository.getReferenceById(
                                                                    student.id()));
                                                    duplicate.setCourse(
                                                            courseRepository.getReferenceById(
                                                                    course.id()));
                                                    return enrollmentRepository.saveAndFlush(
                                                            duplicate);
                                                }))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void teacherCannotEnrollAsStudent() {
        assertThatThrownBy(() -> enrollments.enroll(course.id(), teacher.id()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("student");
    }

    @Test
    void missingStudentIsReported() {
        assertThatThrownBy(() -> enrollments.enroll(course.id(), Long.MAX_VALUE))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void assignmentSubmissionAndGradePersist() {
        var s = submit();
        assertThat(assignments.grade(s.id(), new GradeInput(teacher.id(), 87, "Well done")).score())
                .isEqualTo(87);
        assertThat(assignments.forStudent(student.id()).get(0).feedback()).isEqualTo("Well done");
        assertThat(assignments.forAssignment(assignment.id())).hasSize(1);
        assertThat(assignments.assignmentsForStudent(student.id()))
                .extracting(AssignmentView::id)
                .containsExactly(assignment.id());
    }

    @Test
    void duplicateSubmissionIsRejected() {
        submit();
        assertThatThrownBy(
                        () ->
                                assignments.submit(
                                        assignment.id(),
                                        new SubmissionInput(student.id(), "Again")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void unenrolledStudentCannotSubmit() {
        assertThatThrownBy(
                        () ->
                                assignments.submit(
                                        assignment.id(),
                                        new SubmissionInput(student.id(), "Answer")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    void expiredAssignmentCannotBeSubmitted() {
        enrollments.enroll(course.id(), student.id());
        content.updateAssignment(
                assignment.id(),
                new AssignmentInput("Expired", null, Instant.now().minusSeconds(60), 100));
        assertThatThrownBy(
                        () ->
                                assignments.submit(
                                        assignment.id(), new SubmissionInput(student.id(), "Late")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("deadline");
    }

    @Test
    void gradeCannotExceedMaximum() {
        var s = submit();
        assertThatThrownBy(() -> assignments.grade(s.id(), new GradeInput(teacher.id(), 101, null)))
                .isInstanceOf(DomainException.class);
        assertThat(assignments.forStudent(student.id()).get(0).score()).isNull();
    }

    @Test
    void otherTeacherCannotGrade() {
        var s = submit();
        var other =
                users.create(
                        new UserInput(
                                "Other",
                                "other-" + UUID.randomUUID() + "@example.test",
                                Role.TEACHER));
        assertThatThrownBy(() -> assignments.grade(s.id(), new GradeInput(other.id(), 90, null)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Only the course teacher");
    }

    @Test
    void resultsSurviveUnenrollmentAndPreventDeletion() {
        submit();
        enrollments.unenroll(course.id(), student.id());
        assertThat(assignments.forStudent(student.id())).hasSize(1);
        assertThatThrownBy(() -> courses.delete(course.id())).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> content.deleteModule(module.id()))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> content.deleteLesson(lesson.id()))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> content.deleteAssignment(assignment.id()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void quizScoresSingleAndMultipleChoiceAndStoresResults() {
        enrollments.enroll(course.id(), student.id());
        var quiz = quizzes.create(module.id(), quizInput());
        var result = quizzes.take(quiz.id(), new QuizAttempt(student.id(), correct(quiz)));
        assertThat(result.score()).isEqualTo(2);
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(quizzes.forStudent(student.id())).hasSize(1);
        assertThat(quizzes.forQuiz(quiz.id())).hasSize(1);
        assertThat(quizzes.forCourse(course.id())).hasSize(1);
    }

    @Test
    void unansweredAndPartialMultipleChoiceEarnNoPoints() {
        enrollments.enroll(course.id(), student.id());
        var quiz = quizzes.create(module.id(), quizInput());
        var multi = quiz.questions().get(1);
        assertThat(
                        quizzes.take(
                                        quiz.id(),
                                        new QuizAttempt(
                                                student.id(),
                                                Map.of(
                                                        multi.id(),
                                                        Set.of(multi.options().get(0).id()))))
                                .score())
                .isZero();
        assertThat(quizzes.take(quiz.id(), new QuizAttempt(student.id(), Map.of())).score())
                .isZero();
    }

    @Test
    void foreignQuestionsAndOptionsAreRejected() {
        enrollments.enroll(course.id(), student.id());
        var quiz = quizzes.create(module.id(), quizInput());
        assertThatThrownBy(
                        () ->
                                quizzes.take(
                                        quiz.id(),
                                        new QuizAttempt(
                                                student.id(), Map.of(Long.MAX_VALUE, Set.of()))))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(
                        () ->
                                quizzes.take(
                                        quiz.id(),
                                        new QuizAttempt(
                                                student.id(),
                                                Map.of(
                                                        quiz.questions().get(0).id(),
                                                        Set.of(
                                                                quiz.questions()
                                                                        .get(1)
                                                                        .options()
                                                                        .get(0)
                                                                        .id())))))
                .isInstanceOf(DomainException.class);
        assertThat(quizzes.forQuiz(quiz.id())).isEmpty();
    }

    @Test
    void quizRequiresEnrollmentAndOnlyOneQuizPerModule() {
        var quiz = quizzes.create(module.id(), quizInput());
        assertThatThrownBy(
                        () -> quizzes.take(quiz.id(), new QuizAttempt(student.id(), correct(quiz))))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> quizzes.create(module.id(), quizInput()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void invalidQuizRollsBackAllQuestions() {
        var invalid =
                new QuizInput(
                        "Invalid",
                        List.of(
                                new QuestionInput(
                                        "No correct option",
                                        QuestionType.SINGLE_CHOICE,
                                        List.of(
                                                new OptionInput("A", false),
                                                new OptionInput("B", false)))));
        assertThatThrownBy(() -> quizzes.create(module.id(), invalid))
                .isInstanceOf(DomainException.class);
        assertThat(quizRepository.existsByModuleId(module.id())).isFalse();
    }

    @Test
    void quizCanBeUpdatedAndDeletedUntilItHasResults() {
        var quiz = quizzes.create(module.id(), quizInput());
        var updated = quizzes.update(quiz.id(), new QuizInput("Updated", quizInput().questions()));
        assertThat(updated.questions()).hasSize(2);
        assertThat(updated.title()).isEqualTo("Updated");
        quizzes.delete(quiz.id());
        assertThat(quizRepository.existsById(quiz.id())).isFalse();
        assertThat(content.module(module.id()).quizId()).isNull();
    }

    @Test
    void completedQuizCannotBeChangedOrDeleted() {
        enrollments.enroll(course.id(), student.id());
        var quiz = quizzes.create(module.id(), quizInput());
        quizzes.take(quiz.id(), new QuizAttempt(student.id(), correct(quiz)));
        assertThatThrownBy(() -> quizzes.update(quiz.id(), quizInput()))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> quizzes.delete(quiz.id())).isInstanceOf(DomainException.class);
    }

    @Test
    void profilesAreOneToOneAndCascadeWithUnusedUser() {
        users.putProfile(student.id(), new ProfileInput("Bio", "https://example.test/avatar.png"));
        Long profileId = users.profile(student.id()).id();
        users.putProfile(student.id(), new ProfileInput("Updated", null));
        assertThat(users.profile(student.id()).id()).isEqualTo(profileId);
        assertThat(users.profile(student.id()).bio()).isEqualTo("Updated");
        users.delete(student.id());
        assertThat(profileRepository.existsById(profileId)).isFalse();
    }

    @Test
    void duplicateEmailIsCaseInsensitive() {
        assertThatThrownBy(
                        () ->
                                users.create(
                                        new UserInput(
                                                "Duplicate",
                                                student.email().toUpperCase(Locale.ROOT),
                                                Role.STUDENT)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void reviewsRequireEnrollmentAndUniqueStudentCoursePair() {
        assertThatThrownBy(() -> enrollments.review(course.id(), student.id(), 5, "Good"))
                .isInstanceOf(DomainException.class);
        enrollments.enroll(course.id(), student.id());
        enrollments.review(course.id(), student.id(), 5, "Good");
        assertThat(enrollments.reviews(course.id()).get(0).rating()).isEqualTo(5);
        assertThatThrownBy(() -> enrollments.review(course.id(), student.id(), 4, "Again"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void contentAndMaterialsCanBeUpdatedAndDeleted() {
        var m =
                content.addMaterial(
                        lesson.id(), new MaterialInput("Guide", "https://example.test/guide"));
        content.updateMaterial(
                m.id(), new MaterialInput("Revised", "https://example.test/revised"));
        assertThat(content.lesson(lesson.id()).materials().get(0).title()).isEqualTo("Revised");
        content.deleteMaterial(m.id());
        assertThat(content.lesson(lesson.id()).materials()).isEmpty();
        content.updateAssignment(assignment.id(), new AssignmentInput("Revised", null, null, 50));
        assertThat(content.assignment(assignment.id()).maxScore()).isEqualTo(50);
        content.deleteAssignment(assignment.id());
        content.deleteLesson(lesson.id());
        content.deleteModule(module.id());
        assertThat(courses.get(course.id()).modules()).isEmpty();
    }

    @Test
    void apiReturnsValidationNotFoundAndConflictProblems() throws Exception {
        assertThat(
                        request(
                                        "/api/users",
                                        "POST",
                                        "{\"name\":\"\",\"email\":\"bad\",\"role\":\"STUDENT\"}")
                                .statusCode())
                .isEqualTo(400);
        assertThat(request("/api/courses/9223372036854775807", "GET", null).statusCode())
                .isEqualTo(404);
        assertThat(request("/api/users", "POST", "{broken").statusCode()).isEqualTo(400);
        String body = "{\"studentId\":" + student.id() + "}";
        assertThat(
                        request("/api/courses/" + course.id() + "/enrollments", "POST", body)
                                .statusCode())
                .isEqualTo(201);
        var duplicate = request("/api/courses/" + course.id() + "/enrollments", "POST", body);
        assertThat(duplicate.statusCode()).isEqualTo(409);
        assertThat(duplicate.body()).contains("already enrolled");
    }

    @Test
    void apiSerializesDetachedDtosAndDoesNotRevealCorrectAnswers() throws Exception {
        var quiz = quizzes.create(module.id(), quizInput());
        var response = request("/api/courses/" + course.id(), "GET", null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Assignment", "Lesson");
        var quizResponse = request("/api/quizzes/" + quiz.id(), "GET", null);
        assertThat(quizResponse.statusCode()).isEqualTo(200);
        assertThat(quizResponse.body()).doesNotContain("\"correct\"");
    }

    @Test
    void apiRejectsInvalidUrlsWithoutChangingData() throws Exception {
        var material =
                content.addMaterial(
                        lesson.id(), new MaterialInput("Guide", "https://example.test/guide"));
        String[][] requests = {
            {
                "/api/users/" + student.id() + "/profile",
                "PUT",
                "{\"avatarUrl\":\"not-a-url\"}",
                "avatarUrl"
            },
            {
                "/api/modules/" + module.id() + "/lessons",
                "POST",
                "{\"title\":\"Bad\",\"orderIndex\":0,\"videoUrl\":\"ftp://example.test/video\"}",
                "videoUrl"
            },
            {
                "/api/lessons/" + lesson.id(),
                "PUT",
                "{\"title\":\"Bad\",\"orderIndex\":0,\"videoUrl\":\"/relative\"}",
                "videoUrl"
            },
            {
                "/api/lessons/" + lesson.id() + "/materials",
                "POST",
                "{\"title\":\"Bad\",\"url\":\"not-a-url\"}",
                "url"
            },
            {
                "/api/materials/" + material.id(),
                "PUT",
                "{\"title\":\"Bad\",\"url\":\"https://\"}",
                "url"
            }
        };
        for (String[] input : requests) {
            var response = request(input[0], input[1], input[2]);
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                    .contains("application/problem+json");
            assertThat(response.body())
                    .contains(input[3], "absolute HTTP or HTTPS URL", "\"detail\"");
        }
        assertThat(content.lesson(lesson.id()).materials()).hasSize(1);
        assertThat(content.lesson(lesson.id()).materials().get(0).url())
                .isEqualTo("https://example.test/guide");
        assertThat(content.lesson(lesson.id()).title()).isEqualTo("Lesson");
    }

    HttpResponse<String> request(String path, String method, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        builder.header("Content-Type", "application/json");
        builder.method(
                method,
                body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        return HttpClient.newHttpClient()
                .send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
