package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Lesson;
import org.springframework.data.jpa.repository.*;

public interface LessonRepository extends JpaRepository<Lesson, Long> {}
