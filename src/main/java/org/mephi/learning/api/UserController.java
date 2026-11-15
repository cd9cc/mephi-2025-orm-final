package org.mephi.learning.api;

import jakarta.validation.Valid;
import java.util.List;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    private final EnrollmentService enrollments;
    private final AssignmentService assignments;
    private final QuizService quizzes;

    public UserController(
            UserService users,
            EnrollmentService enrollments,
            AssignmentService assignments,
            QuizService quizzes) {
        this.users = users;
        this.enrollments = enrollments;
        this.assignments = assignments;
        this.quizzes = quizzes;
    }

    @GetMapping
    public List<UserView> list() {
        return users.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserView create(@Valid @RequestBody UserInput input) {
        return users.create(input);
    }

    @GetMapping("/{id}")
    public UserView get(@PathVariable Long id) {
        return users.get(id);
    }

    @PutMapping("/{id}")
    public UserView update(@PathVariable Long id, @Valid @RequestBody UserInput input) {
        return users.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        users.delete(id);
    }

    @GetMapping("/{id}/profile")
    public ProfileView profile(@PathVariable Long id) {
        return users.profile(id);
    }

    @PutMapping("/{id}/profile")
    public ProfileView profile(@PathVariable Long id, @Valid @RequestBody ProfileInput input) {
        return users.putProfile(id, input);
    }

    @GetMapping("/{id}/courses")
    public List<EnrollmentView> courses(@PathVariable Long id) {
        return enrollments.courses(id);
    }

    @GetMapping("/{id}/assignments")
    public List<AssignmentView> assignments(@PathVariable Long id) {
        return assignments.assignmentsForStudent(id);
    }

    @GetMapping("/{id}/submissions")
    public List<SubmissionView> submissions(@PathVariable Long id) {
        return assignments.forStudent(id);
    }

    @GetMapping("/{id}/quiz-results")
    public List<ResultView> results(@PathVariable Long id) {
        return quizzes.forStudent(id);
    }
}
