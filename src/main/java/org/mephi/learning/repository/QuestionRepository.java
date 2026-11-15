package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Question;
import org.springframework.data.jpa.repository.*;

public interface QuestionRepository extends JpaRepository<Question, Long> {}
