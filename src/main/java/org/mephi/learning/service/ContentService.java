package org.mephi.learning.service;

import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class ContentService {
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonMaterialRepository materials;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository results;
    private final EntityAccess access;
    private final EnrollmentRepository enrollments;
    private final EmailNotificationService notifications;

    public ContentService(
            CourseRepository courses,
            CourseModuleRepository modules,
            LessonRepository lessons,
            LessonMaterialRepository materials,
            AssignmentRepository assignments,
            SubmissionRepository submissions,
            QuizSubmissionRepository results,
            EntityAccess access,
            EnrollmentRepository enrollments,
            EmailNotificationService notifications) {
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
        this.materials = materials;
        this.assignments = assignments;
        this.submissions = submissions;
        this.results = results;
        this.access = access;
        this.enrollments = enrollments;
        this.notifications = notifications;
    }

    @Transactional
    public ModuleView addModule(Long courseId, @Valid ModuleInput input) {
        Course c = access.require(courses, courseId, "Course");
        CourseModule m = new CourseModule();
        m.setCourse(c);
        apply(m, input);
        c.getModules().add(m);
        modules.saveAndFlush(m);
        return Views.module(m);
    }

    public ModuleView module(Long id) {
        return Views.module(access.require(modules, id, "Module"));
    }

    @Transactional
    public ModuleView updateModule(Long id, @Valid ModuleInput input) {
        CourseModule m = access.require(modules, id, "Module");
        apply(m, input);
        return Views.module(m);
    }

    private void apply(CourseModule m, ModuleInput input) {
        m.setTitle(input.title().trim());
        m.setDescription(input.description());
        m.setOrderIndex(input.orderIndex());
    }

    @Transactional
    public void deleteModule(Long id) {
        CourseModule m = access.require(modules, id, "Module");
        if (submissions.existsByAssignmentLessonModuleId(id) || results.existsByQuizModuleId(id))
            throw DomainException.conflict("Module has student results");
        m.getCourse().getModules().remove(m);
        modules.delete(m);
        modules.flush();
    }

    @Transactional
    public LessonView addLesson(Long moduleId, @Valid LessonInput input) {
        CourseModule m = access.require(modules, moduleId, "Module");
        Lesson l = new Lesson();
        l.setModule(m);
        apply(l, input);
        m.getLessons().add(l);
        lessons.saveAndFlush(l);
        return Views.lesson(l);
    }

    public LessonView lesson(Long id) {
        return Views.lesson(access.require(lessons, id, "Lesson"));
    }

    @Transactional
    public LessonView updateLesson(Long id, @Valid LessonInput input) {
        Lesson l = access.require(lessons, id, "Lesson");
        apply(l, input);
        return Views.lesson(l);
    }

    private void apply(Lesson l, LessonInput input) {
        l.setTitle(input.title().trim());
        l.setContent(input.content());
        l.setVideoUrl(input.videoUrl());
        l.setOrderIndex(input.orderIndex());
    }

    @Transactional
    public void deleteLesson(Long id) {
        Lesson l = access.require(lessons, id, "Lesson");
        if (submissions.existsByAssignmentLessonId(id))
            throw DomainException.conflict("Lesson has student submissions");
        l.getModule().getLessons().remove(l);
        lessons.delete(l);
        lessons.flush();
    }

    @Transactional
    public MaterialView addMaterial(Long lessonId, @Valid MaterialInput input) {
        Lesson l = access.require(lessons, lessonId, "Lesson");
        LessonMaterial m = new LessonMaterial();
        m.setLesson(l);
        m.setTitle(input.title().trim());
        m.setUrl(input.url());
        l.getMaterials().add(m);
        return Views.material(materials.saveAndFlush(m));
    }

    @Transactional
    public MaterialView updateMaterial(Long id, @Valid MaterialInput input) {
        LessonMaterial m = access.require(materials, id, "Material");
        m.setTitle(input.title().trim());
        m.setUrl(input.url());
        return Views.material(m);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        LessonMaterial m = access.require(materials, id, "Material");
        m.getLesson().getMaterials().remove(m);
        materials.delete(m);
    }

    @Transactional
    public AssignmentView addAssignment(Long lessonId, @Valid AssignmentInput input) {
        Lesson l = access.require(lessons, lessonId, "Lesson");
        Assignment a = new Assignment();
        a.setLesson(l);
        apply(a, input);
        l.getAssignments().add(a);
        Assignment saved = assignments.saveAndFlush(a);
        notifications.assignmentAdded(
                saved,
                enrollments
                        .findByCourseIdAndStatus(
                                l.getModule().getCourse().getId(), EnrollmentStatus.ACTIVE)
                        .stream()
                        .map(e -> e.getStudent().getEmail())
                        .collect(Collectors.toSet()));
        return Views.assignment(saved);
    }

    public AssignmentView assignment(Long id) {
        return Views.assignment(access.require(assignments, id, "Assignment"));
    }

    @Transactional
    public AssignmentView updateAssignment(Long id, @Valid AssignmentInput input) {
        Assignment a = access.require(assignments, id, "Assignment");
        if (submissions.existsByAssignmentId(id))
            throw DomainException.conflict("Assignment has submissions and cannot be changed");
        apply(a, input);
        return Views.assignment(a);
    }

    private void apply(Assignment a, AssignmentInput input) {
        a.setTitle(input.title().trim());
        a.setDescription(input.description());
        a.setDueDate(input.dueDate());
        a.setMaxScore(input.maxScore());
    }

    @Transactional
    public void deleteAssignment(Long id) {
        Assignment a = access.require(assignments, id, "Assignment");
        if (submissions.existsByAssignmentId(id))
            throw DomainException.conflict("Assignment has student submissions");
        a.getLesson().getAssignments().remove(a);
        assignments.delete(a);
        assignments.flush();
    }
}
