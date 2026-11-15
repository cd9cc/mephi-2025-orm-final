package org.mephi.learning.repository;

import java.util.*;
import org.mephi.learning.entity.Profile;
import org.springframework.data.jpa.repository.*;

public interface ProfileRepository extends JpaRepository<Profile, Long> {}
