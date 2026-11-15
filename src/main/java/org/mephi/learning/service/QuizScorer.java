package org.mephi.learning.service;

import java.util.*;
import java.util.stream.Collectors;
import org.mephi.learning.entity.*;

/**
 * Exact-set scoring: a multiple-choice answer earns one point only if all correct options are
 * selected.
 */
public final class QuizScorer {
    private QuizScorer() {}

    public static int score(Quiz quiz, Map<Long, Set<Long>> answers) {
        Set<Long> questionIds =
                quiz.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
        if (!questionIds.containsAll(answers.keySet()))
            throw DomainException.invalid("Answer contains a question outside this quiz");
        int score = 0;
        for (Question q : quiz.getQuestions()) {
            Set<Long> selected = answers.getOrDefault(q.getId(), Set.of());
            Set<Long> allowed =
                    q.getOptions().stream().map(AnswerOption::getId).collect(Collectors.toSet());
            if (!allowed.containsAll(selected))
                throw DomainException.invalid("Selected option does not belong to its question");
            if (q.getType() == QuestionType.SINGLE_CHOICE && selected.size() > 1)
                throw DomainException.invalid("Single-choice question accepts at most one option");
            Set<Long> correct =
                    q.getOptions().stream()
                            .filter(AnswerOption::isCorrect)
                            .map(AnswerOption::getId)
                            .collect(Collectors.toSet());
            if (!correct.isEmpty() && correct.equals(selected)) score++;
        }
        return score;
    }
}
