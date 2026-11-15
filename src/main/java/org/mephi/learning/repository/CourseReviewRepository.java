package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.CourseReview;
import org.springframework.data.jpa.repository.*;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    boolean existsByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<CourseReview> findByCourseId(Long courseId);
}
