package org.mephi.learning.service;

import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class EntityAccess {
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    public EntityAccess(UserRepository users, EnrollmentRepository enrollments) {
        this.users = users;
        this.enrollments = enrollments;
    }

    public <T> T require(JpaRepository<T, Long> repository, Long id, String type) {
        if (id == null) throw DomainException.invalid(type + " id is required");
        return repository.findById(id).orElseThrow(() -> DomainException.missing(type, id));
    }

    public User user(Long id) {
        return require(users, id, "User");
    }

    public User student(Long id) {
        User u = user(id);
        if (u.getRole() != Role.STUDENT) throw DomainException.invalid("User must be a student");
        return u;
    }

    public User teacher(Long id) {
        User u = user(id);
        if (u.getRole() != Role.TEACHER)
            throw DomainException.invalid("Course teacher must have TEACHER role");
        return u;
    }

    public User enrolledStudent(Long id, Course course) {
        User u = student(id);
        if (!enrollments.existsByStudentIdAndCourseId(id, course.getId()))
            throw DomainException.conflict("Student is not enrolled in this course");
        return u;
    }
}
