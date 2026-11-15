package org.mephi.learning.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.mephi.learning.entity.*;

public final class Requests {
    private Requests() {}

    public record UserInput(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotNull Role role) {}

    public record RegisterInput(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 200) String password) {}

    public record LoginInput(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 200) String password) {}

    public record ProfileInput(
            @Size(max = 4000) String bio, @HttpUrl @Size(max = 255) String avatarUrl) {}

    public record NameInput(@NotBlank @Size(max = 120) String name) {}

    public record CourseInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String description,
            @NotNull @Positive Long teacherId,
            @NotNull @Positive Long categoryId,
            Set<@NotNull @Positive Long> tagIds) {}

    public record ModuleInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 4000) String description,
            @Min(0) int orderIndex) {}

    public record LessonInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 20000) String content,
            @HttpUrl @Size(max = 255) String videoUrl,
            @Min(0) int orderIndex) {}

    public record MaterialInput(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @HttpUrl @Size(max = 2000) String url) {}

    public record AssignmentInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String description,
            Instant dueDate,
            @Min(1) @Max(10000) int maxScore) {}

    public record StudentInput(@NotNull @Positive Long studentId) {}

    public record SubmissionInput(
            @NotNull @Positive Long studentId, @NotBlank @Size(max = 20000) String content) {}

    public record GradeInput(
            @NotNull @Positive Long teacherId,
            @NotNull @Min(0) Integer score,
            @Size(max = 4000) String feedback) {}

    public record QuizInput(
            @NotBlank @Size(max = 200) String title,
            @NotEmpty @Size(max = 100) List<@NotNull @Valid QuestionInput> questions) {}

    public record QuestionInput(
            @NotBlank @Size(max = 2000) String text,
            @NotNull QuestionType type,
            @Size(min = 2, max = 20) @NotNull List<@NotNull @Valid OptionInput> options) {}

    public record OptionInput(@NotBlank @Size(max = 2000) String text, boolean correct) {}

    public record QuizAttempt(
            @NotNull @Positive Long studentId,
            @NotNull Map<@NotNull @Positive Long, @NotNull Set<@NotNull @Positive Long>> answers) {}

    public record ReviewInput(
            @NotNull @Positive Long studentId,
            @Min(1) @Max(5) int rating,
            @Size(max = 4000) String comment) {}
}
