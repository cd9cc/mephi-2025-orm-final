package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Enrollment;
import org.mephi.learning.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.*;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
}
