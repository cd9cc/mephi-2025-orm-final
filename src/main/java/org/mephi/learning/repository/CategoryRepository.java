package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Category;
import org.springframework.data.jpa.repository.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
