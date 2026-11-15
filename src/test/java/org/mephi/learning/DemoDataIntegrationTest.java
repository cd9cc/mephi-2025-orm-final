package org.mephi.learning;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mephi.learning.config.DemoData;
import org.mephi.learning.repository.*;
import org.mephi.learning.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
            "app.demo.enabled=true",
            "spring.datasource.url=jdbc:h2:mem:demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
        })
@ActiveProfiles("test")
class DemoDataIntegrationTest {
    @Autowired DemoData demo;
    @Autowired UserRepository users;
    @Autowired CourseService courses;
    @Autowired EnrollmentService enrollments;
    @Autowired AssignmentService assignments;
    @Autowired QuizService quizzes;

    @Test
    void demoSeedsFullWorkflowAndDoesNotDuplicateOnRestart() {
        assertThat(users.count()).isEqualTo(3);
        var course = courses.get(courses.list(null).get(0).id());
        assertThat(course.modules()).hasSize(2);
        var student = enrollments.students(course.id()).get(0).student();
        assertThat(assignments.forStudent(student.id()).get(0).score()).isEqualTo(90);
        assertThat(quizzes.forStudent(student.id()).get(0).score()).isEqualTo(2);
        assertThat(enrollments.reviews(course.id())).hasSize(1);
        demo.run();
        assertThat(users.count()).isEqualTo(3);
        assertThat(courses.list(null)).hasSize(1);
    }
}
