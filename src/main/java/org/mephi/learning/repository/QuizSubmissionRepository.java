package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.QuizSubmission;
import org.springframework.data.jpa.repository.*;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    boolean existsByQuizId(Long quizId);

    boolean existsByQuizModuleCourseId(Long courseId);

    boolean existsByQuizModuleId(Long moduleId);

    List<QuizSubmission> findByStudentId(Long studentId);

    List<QuizSubmission> findByQuizId(Long quizId);

    List<QuizSubmission> findByQuizModuleCourseId(Long courseId);
}
