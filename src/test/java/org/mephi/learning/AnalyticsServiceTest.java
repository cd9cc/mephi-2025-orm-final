package org.mephi.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mephi.learning.entity.QuizSubmission;
import org.mephi.learning.entity.User;
import org.mephi.learning.repository.EnrollmentRepository;
import org.mephi.learning.repository.QuizSubmissionRepository;
import org.mephi.learning.repository.SubmissionRepository;
import org.mephi.learning.repository.UserRepository;
import org.mephi.learning.service.AnalyticsService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock UserRepository users;
    @Mock EnrollmentRepository enrollments;
    @Mock SubmissionRepository submissions;
    @Mock QuizSubmissionRepository quizSubmissions;
    @InjectMocks AnalyticsService analytics;

    @Test
    void quizStatisticsAggregatesAttemptsStudentsAverageAndPassRate() {
        QuizSubmission passed = new QuizSubmission();
        passed.setScore(8);
        passed.setTotalQuestions(10);
        QuizSubmission failed = new QuizSubmission();
        failed.setScore(4);
        failed.setTotalQuestions(10);
        User student = new User();
        student.setName("Student");
        passed.setStudent(student);
        failed.setStudent(student);
        when(quizSubmissions.findAll()).thenReturn(List.of(passed, failed));

        var result = analytics.quizStatistics();

        assertThat(result.attempts()).isEqualTo(2);
        assertThat(result.averagePercent()).isEqualTo(60.0);
        assertThat(result.passRatePercent()).isEqualTo(50.0);
    }
}
