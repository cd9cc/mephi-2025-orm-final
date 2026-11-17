package org.mephi.learning.service;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class AssignmentService {
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;
    private final EntityAccess access;
    private final EmailNotificationService notifications;

    public AssignmentService(
            AssignmentRepository assignments,
            SubmissionRepository submissions,
            EntityAccess access,
            EmailNotificationService notifications) {
        this.assignments = assignments;
        this.submissions = submissions;
        this.access = access;
        this.notifications = notifications;
    }

    @Transactional
    public SubmissionView submit(Long assignmentId, @Valid SubmissionInput input) {
        Assignment a = access.require(assignments, assignmentId, "Assignment");
        User s = access.enrolledStudent(input.studentId(), a.getLesson().getModule().getCourse());
        if (submissions.existsByStudentIdAndAssignmentId(s.getId(), assignmentId))
            throw DomainException.conflict("Student has already submitted this assignment");
        if (a.getDueDate() != null && a.getDueDate().isBefore(Instant.now()))
            throw DomainException.conflict("Assignment deadline has passed");
        Submission submission = new Submission();
        submission.setStudent(s);
        submission.setAssignment(a);
        submission.setContent(input.content());
        return Views.submission(submissions.saveAndFlush(submission));
    }

    @Transactional
    public SubmissionView grade(Long id, @Valid GradeInput input) {
        Submission s = access.require(submissions, id, "Submission");
        User teacher = access.teacher(input.teacherId());
        requireCurrentTeacher(teacher);
        if (!teacher.getId()
                .equals(s.getAssignment().getLesson().getModule().getCourse().getTeacher().getId()))
            throw new DomainException(
                    HttpStatus.FORBIDDEN, "Only the course teacher can grade this submission");
        if (input.score() > s.getAssignment().getMaxScore())
            throw DomainException.invalid("Score exceeds assignment maximum");
        s.setScore(input.score());
        s.setFeedback(input.feedback());
        notifications.submissionGraded(s);
        return Views.submission(s);
    }

    private void requireCurrentTeacher(User teacher) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return;
        if (!teacher.getEmail().equalsIgnoreCase(authentication.getName()))
            throw new DomainException(
                    HttpStatus.FORBIDDEN, "Only the authenticated course teacher can grade");
    }

    public List<SubmissionView> forAssignment(Long id) {
        access.require(assignments, id, "Assignment");
        return submissions.findByAssignmentId(id).stream().map(Views::submission).toList();
    }

    public List<SubmissionView> forStudent(Long id) {
        access.student(id);
        return submissions.findByStudentId(id).stream().map(Views::submission).toList();
    }

    public List<AssignmentView> assignmentsForStudent(Long id) {
        access.student(id);
        return assignments.findForStudent(id).stream().map(Views::assignment).toList();
    }
}
