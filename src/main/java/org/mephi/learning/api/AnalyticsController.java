package org.mephi.learning.api;

import java.util.List;
import org.mephi.learning.api.AnalyticsViews.*;
import org.mephi.learning.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/students/{id}")
    public StudentPerformance student(@PathVariable Long id) {
        return analytics.student(id);
    }

    @GetMapping("/ranking")
    public List<StudentRanking> ranking() {
        return analytics.ranking();
    }

    @GetMapping("/quizzes")
    public QuizStatistics quizzes() {
        return analytics.quizStatistics();
    }
}
