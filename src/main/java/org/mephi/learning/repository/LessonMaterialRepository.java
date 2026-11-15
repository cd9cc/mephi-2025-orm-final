package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.LessonMaterial;
import org.springframework.data.jpa.repository.*;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {}
