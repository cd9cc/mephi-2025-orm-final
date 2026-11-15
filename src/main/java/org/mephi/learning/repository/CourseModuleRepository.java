package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.CourseModule;
import org.springframework.data.jpa.repository.*;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {}
