package org.mephi.learning.api;

import jakarta.validation.Valid;
import java.util.List;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courses;
    private final ContentService content;
    private final EnrollmentService enrollments;
    private final QuizService quizzes;

    public CourseController(
            CourseService courses,
            ContentService content,
            EnrollmentService enrollments,
            QuizService quizzes) {
        this.courses = courses;
        this.content = content;
        this.enrollments = enrollments;
        this.quizzes = quizzes;
    }

    @GetMapping
    public List<CourseSummary> list(@RequestParam(required = false) Long categoryId) {
        return courses.list(categoryId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseView create(@Valid @RequestBody CourseInput input) {
        return courses.create(input);
    }

    @GetMapping("/{id}")
    public CourseView get(@PathVariable Long id) {
        return courses.get(id);
    }

    @PutMapping("/{id}")
    public CourseView update(@PathVariable Long id, @Valid @RequestBody CourseInput input) {
        return courses.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courses.delete(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleView addModule(@PathVariable Long id, @Valid @RequestBody ModuleInput input) {
        return content.addModule(id, input);
    }

    @PostMapping("/{id}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentView enroll(@PathVariable Long id, @Valid @RequestBody StudentInput input) {
        return enrollments.enroll(id, input.studentId());
    }

    @DeleteMapping("/{id}/enrollments/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable Long id, @PathVariable Long studentId) {
        enrollments.unenroll(id, studentId);
    }

    @GetMapping("/{id}/enrollments")
    public List<EnrollmentView> students(@PathVariable Long id) {
        return enrollments.students(id);
    }

    @GetMapping("/{id}/quiz-results")
    public List<ResultView> results(@PathVariable Long id) {
        return quizzes.forCourse(id);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewView> reviews(@PathVariable Long id) {
        return enrollments.reviews(id);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewView review(@PathVariable Long id, @Valid @RequestBody ReviewInput input) {
        return enrollments.review(id, input.studentId(), input.rating(), input.comment());
    }
}
