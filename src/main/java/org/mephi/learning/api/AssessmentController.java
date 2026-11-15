package org.mephi.learning.api;

import jakarta.validation.Valid;
import java.util.List;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AssessmentController {
    private final AssignmentService assignments;
    private final QuizService quizzes;

    public AssessmentController(AssignmentService assignments, QuizService quizzes) {
        this.assignments = assignments;
        this.quizzes = quizzes;
    }

    @PostMapping("/assignments/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionView submit(@PathVariable Long id, @Valid @RequestBody SubmissionInput input) {
        return assignments.submit(id, input);
    }

    @GetMapping("/assignments/{id}/submissions")
    public List<SubmissionView> submissions(@PathVariable Long id) {
        return assignments.forAssignment(id);
    }

    @PutMapping("/submissions/{id}/grade")
    public SubmissionView grade(@PathVariable Long id, @Valid @RequestBody GradeInput input) {
        return assignments.grade(id, input);
    }

    @PostMapping("/modules/{id}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizView create(@PathVariable Long id, @Valid @RequestBody QuizInput input) {
        return quizzes.create(id, input);
    }

    @GetMapping("/quizzes/{id}")
    public QuizView get(@PathVariable Long id) {
        return quizzes.get(id);
    }

    @PutMapping("/quizzes/{id}")
    public QuizView update(@PathVariable Long id, @Valid @RequestBody QuizInput input) {
        return quizzes.update(id, input);
    }

    @DeleteMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizzes.delete(id);
    }

    @PostMapping("/quizzes/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultView take(@PathVariable Long id, @Valid @RequestBody QuizAttempt input) {
        return quizzes.take(id, input);
    }

    @GetMapping("/quizzes/{id}/submissions")
    public List<ResultView> results(@PathVariable Long id) {
        return quizzes.forQuiz(id);
    }
}
