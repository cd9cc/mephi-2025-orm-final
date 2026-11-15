package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Course;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"teacher", "category", "tags"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findDetailedById(@Param("id") Long id);
}
