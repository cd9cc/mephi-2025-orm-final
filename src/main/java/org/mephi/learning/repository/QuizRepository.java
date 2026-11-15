package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Quiz;
import org.springframework.data.jpa.repository.*;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByModuleId(Long moduleId);
}
