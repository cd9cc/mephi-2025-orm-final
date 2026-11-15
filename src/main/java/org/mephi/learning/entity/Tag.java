package org.mephi.learning.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Course> courses = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Course> getCourses() {
        return courses;
    }
}
