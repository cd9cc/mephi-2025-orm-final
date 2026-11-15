package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Tag;
import org.springframework.data.jpa.repository.*;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
}
