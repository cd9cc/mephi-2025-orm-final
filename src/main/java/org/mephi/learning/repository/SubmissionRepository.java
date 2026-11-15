package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Submission;
import org.springframework.data.jpa.repository.*;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    boolean existsByAssignmentId(Long assignmentId);

    boolean existsByAssignmentLessonModuleCourseId(Long courseId);

    boolean existsByAssignmentLessonModuleId(Long moduleId);

    boolean existsByAssignmentLessonId(Long lessonId);

    List<Submission> findByStudentId(Long studentId);

    List<Submission> findByAssignmentId(Long assignmentId);
}
