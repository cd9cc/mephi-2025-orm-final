package org.mephi.learning.service;

import java.util.List;
import org.mephi.learning.api.Views;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;
    private final CourseReviewRepository reviews;
    private final EntityAccess access;

    public EnrollmentService(
            EnrollmentRepository enrollments,
            CourseRepository courses,
            CourseReviewRepository reviews,
            EntityAccess access) {
        this.enrollments = enrollments;
        this.courses = courses;
        this.reviews = reviews;
        this.access = access;
    }

    @Transactional
    public EnrollmentView enroll(Long courseId, Long studentId) {
        User student = access.student(studentId);
        Course c = access.require(courses, courseId, "Course");
        if (enrollments.existsByStudentIdAndCourseId(studentId, courseId))
            throw DomainException.conflict("Student is already enrolled");
        Enrollment e = new Enrollment();
        e.setCourse(c);
        e.setStudent(student);
        return Views.enrollment(enrollments.saveAndFlush(e));
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        access.require(courses, courseId, "Course");
        access.student(studentId);
        Enrollment e =
                enrollments
                        .findByStudentIdAndCourseId(studentId, courseId)
                        .orElseThrow(
                                () -> DomainException.missing("Enrollment for student", studentId));
        enrollments.delete(e);
    }

    public List<EnrollmentView> students(Long courseId) {
        access.require(courses, courseId, "Course");
        return enrollments.findByCourseId(courseId).stream().map(Views::enrollment).toList();
    }

    public List<EnrollmentView> courses(Long studentId) {
        access.student(studentId);
        return enrollments.findByStudentId(studentId).stream().map(Views::enrollment).toList();
    }

    public List<ReviewView> reviews(Long courseId) {
        access.require(courses, courseId, "Course");
        return reviews.findByCourseId(courseId).stream().map(Views::review).toList();
    }

    @Transactional
    public ReviewView review(Long courseId, Long studentId, int rating, String comment) {
        Course c = access.require(courses, courseId, "Course");
        User s = access.enrolledStudent(studentId, c);
        if (rating < 1 || rating > 5)
            throw DomainException.invalid("Rating must be between 1 and 5");
        if (comment != null && comment.length() > 4000)
            throw DomainException.invalid("Comment is too long");
        if (reviews.existsByStudentIdAndCourseId(studentId, courseId))
            throw DomainException.conflict("Student has already reviewed this course");
        CourseReview r = new CourseReview();
        r.setCourse(c);
        r.setStudent(s);
        r.setRating(rating);
        r.setComment(comment);
        return Views.review(reviews.saveAndFlush(r));
    }
}
