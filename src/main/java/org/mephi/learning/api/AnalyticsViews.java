package org.mephi.learning.api;

import java.util.List;

public final class AnalyticsViews {
    private AnalyticsViews() {}

    public record CoursePerformance(
            Long courseId,
            String courseTitle,
            int assignmentsSubmitted,
            int assignmentsAvailable,
            double assignmentAveragePercent,
            int quizzesTaken,
            double quizAveragePercent,
            double overallPercent) {}

    public record StudentPerformance(
            Long studentId,
            String studentName,
            List<CoursePerformance> courses,
            double overallPercent) {}

    public record StudentRanking(
            Long studentId,
            String studentName,
            int courses,
            int gradedItems,
            double averagePercent,
            int position) {}

    public record QuizStatistics(
            long attempts, long distinctStudents, double averagePercent, double passRatePercent) {}
}
