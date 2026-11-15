package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Assignment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query(
            "select a from Assignment a where a.lesson.module.course.id in (select e.course.id from Enrollment e where e.student.id = :studentId)")
    List<Assignment> findForStudent(@Param("studentId") Long studentId);
}
