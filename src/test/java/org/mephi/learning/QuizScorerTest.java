package org.mephi.learning;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.service.*;
import org.springframework.test.util.ReflectionTestUtils;

class QuizScorerTest {
    Quiz quiz;

    @BeforeEach
    void setup() {
        quiz = new Quiz();
        Question q = new Question();
        ReflectionTestUtils.setField(q, "id", 1L);
        q.setType(QuestionType.SINGLE_CHOICE);
        for (long id = 10; id < 12; id++) {
            AnswerOption o = new AnswerOption();
            ReflectionTestUtils.setField(o, "id", id);
            o.setCorrect(id == 10);
            q.getOptions().add(o);
        }
        quiz.getQuestions().add(q);
    }

    @Test
    void correctAnswerEarnsOnePoint() {
        assertThat(QuizScorer.score(quiz, Map.of(1L, Set.of(10L)))).isOne();
    }

    @Test
    void wrongAnswerEarnsZero() {
        assertThat(QuizScorer.score(quiz, Map.of(1L, Set.of(11L)))).isZero();
    }

    @Test
    void omittedAnswerEarnsZero() {
        assertThat(QuizScorer.score(quiz, Map.of())).isZero();
    }

    @Test
    void multipleAnswersForSingleChoiceAreRejected() {
        assertThatThrownBy(() -> QuizScorer.score(quiz, Map.of(1L, Set.of(10L, 11L))))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void multipleChoiceUsesExactSet() {
        Question q = quiz.getQuestions().get(0);
        q.setType(QuestionType.MULTIPLE_CHOICE);
        q.getOptions().get(1).setCorrect(true);
        assertThat(QuizScorer.score(quiz, Map.of(1L, Set.of(10L)))).isZero();
        assertThat(QuizScorer.score(quiz, Map.of(1L, Set.of(10L, 11L)))).isOne();
    }
}
