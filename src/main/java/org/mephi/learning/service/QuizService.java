package org.mephi.learning.service;

import jakarta.validation.Valid;
import java.util.List;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizzes;
    private final CourseModuleRepository modules;
    private final CourseRepository courses;
    private final QuizSubmissionRepository results;
    private final EntityAccess access;

    public QuizService(
            QuizRepository quizzes,
            CourseModuleRepository modules,
            CourseRepository courses,
            QuizSubmissionRepository results,
            EntityAccess access) {
        this.quizzes = quizzes;
        this.modules = modules;
        this.courses = courses;
        this.results = results;
        this.access = access;
    }

    @Transactional
    public QuizView create(Long moduleId, @Valid QuizInput input) {
        CourseModule m = access.require(modules, moduleId, "Module");
        if (quizzes.existsByModuleId(moduleId))
            throw DomainException.conflict("Module already has a quiz");
        Quiz q = new Quiz();
        q.setModule(m);
        m.setQuiz(q);
        apply(q, input);
        return Views.quiz(quizzes.saveAndFlush(q));
    }

    public QuizView get(Long id) {
        return Views.quiz(access.require(quizzes, id, "Quiz"));
    }

    @Transactional
    public QuizView update(Long id, @Valid QuizInput input) {
        Quiz q = access.require(quizzes, id, "Quiz");
        if (results.existsByQuizId(id))
            throw DomainException.conflict("Quiz has results and cannot be changed");
        apply(q, input);
        quizzes.flush();
        return Views.quiz(q);
    }

    private void apply(Quiz q, QuizInput input) {
        q.setTitle(input.title().trim());
        q.getQuestions().clear();
        for (QuestionInput qi : input.questions()) {
            long correct = qi.options().stream().filter(OptionInput::correct).count();
            if (correct == 0 || (qi.type() == QuestionType.SINGLE_CHOICE && correct != 1))
                throw DomainException.invalid(
                        "Each question needs a correct answer; single-choice questions need exactly one");
            Question question = new Question();
            question.setQuiz(q);
            question.setText(qi.text());
            question.setType(qi.type());
            for (OptionInput oi : qi.options()) {
                AnswerOption o = new AnswerOption();
                o.setQuestion(question);
                o.setText(oi.text());
                o.setCorrect(oi.correct());
                question.getOptions().add(o);
            }
            q.getQuestions().add(question);
        }
    }

    @Transactional
    public void delete(Long id) {
        Quiz q = access.require(quizzes, id, "Quiz");
        if (results.existsByQuizId(id)) throw DomainException.conflict("Quiz has student results");
        q.getModule().setQuiz(null);
        quizzes.delete(q);
        quizzes.flush();
    }

    @Transactional
    public ResultView take(Long id, @Valid QuizAttempt input) {
        Quiz q = access.require(quizzes, id, "Quiz");
        User student = access.enrolledStudent(input.studentId(), q.getModule().getCourse());
        QuizSubmission r = new QuizSubmission();
        r.setQuiz(q);
        r.setStudent(student);
        r.setScore(QuizScorer.score(q, input.answers()));
        r.setTotalQuestions(q.getQuestions().size());
        return Views.result(results.saveAndFlush(r));
    }

    public List<ResultView> forStudent(Long id) {
        access.student(id);
        return results.findByStudentId(id).stream().map(Views::result).toList();
    }

    public List<ResultView> forQuiz(Long id) {
        access.require(quizzes, id, "Quiz");
        return results.findByQuizId(id).stream().map(Views::result).toList();
    }

    public List<ResultView> forCourse(Long id) {
        access.require(courses, id, "Course");
        return results.findByQuizModuleCourseId(id).stream().map(Views::result).toList();
    }
}
