package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.User;
import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
