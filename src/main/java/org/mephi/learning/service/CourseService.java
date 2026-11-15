package org.mephi.learning.service;

import jakarta.validation.Valid;
import java.util.*;
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
public class CourseService {
    private final CourseRepository courses;
    private final CategoryRepository categories;
    private final TagRepository tags;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository results;
    private final CourseReviewRepository reviews;
    private final EntityAccess access;

    public CourseService(
            CourseRepository courses,
            CategoryRepository categories,
            TagRepository tags,
            EnrollmentRepository enrollments,
            SubmissionRepository submissions,
            QuizSubmissionRepository results,
            CourseReviewRepository reviews,
            EntityAccess access) {
        this.courses = courses;
        this.categories = categories;
        this.tags = tags;
        this.enrollments = enrollments;
        this.submissions = submissions;
        this.results = results;
        this.reviews = reviews;
        this.access = access;
    }

    public List<CourseSummary> list(Long categoryId) {
        if (categoryId != null) access.require(categories, categoryId, "Category");
        return (categoryId == null ? courses.findAll() : courses.findByCategoryId(categoryId))
                .stream().map(Views::summary).toList();
    }

    public CourseView get(Long id) {
        return Views.course(
                courses.findDetailedById(id)
                        .orElseThrow(() -> DomainException.missing("Course", id)));
    }

    @Transactional
    public CourseView create(@Valid CourseInput input) {
        Course c = new Course();
        apply(c, input);
        courses.saveAndFlush(c);
        return Views.course(c);
    }

    @Transactional
    public CourseView update(Long id, @Valid CourseInput input) {
        Course c = access.require(courses, id, "Course");
        apply(c, input);
        return Views.course(c);
    }

    private void apply(Course c, CourseInput input) {
        c.setTitle(input.title().trim());
        c.setDescription(input.description());
        c.setTeacher(access.teacher(input.teacherId()));
        c.setCategory(access.require(categories, input.categoryId(), "Category"));
        c.getTags().clear();
        if (input.tagIds() != null)
            for (Long id : input.tagIds()) c.getTags().add(access.require(tags, id, "Tag"));
    }

    @Transactional
    public void delete(Long id) {
        Course c = access.require(courses, id, "Course");
        if (enrollments.existsByCourseId(id)
                || submissions.existsByAssignmentLessonModuleCourseId(id)
                || results.existsByQuizModuleCourseId(id)
                || reviews.existsByCourseId(id))
            throw DomainException.conflict(
                    "Course has enrollments, results or reviews and cannot be deleted");
        courses.delete(c);
        courses.flush();
    }

    public List<NameView> categories() {
        return categories.findAll().stream()
                .map(c -> new NameView(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public NameView createCategory(@Valid NameInput input) {
        if (categories.existsByName(input.name().trim()))
            throw DomainException.conflict("Category already exists");
        Category c = new Category();
        c.setName(input.name().trim());
        categories.save(c);
        return new NameView(c.getId(), c.getName());
    }

    @Transactional
    public NameView updateCategory(Long id, @Valid NameInput input) {
        Category c = access.require(categories, id, "Category");
        if (!c.getName().equals(input.name().trim())
                && categories.existsByName(input.name().trim()))
            throw DomainException.conflict("Category already exists");
        c.setName(input.name().trim());
        return new NameView(id, c.getName());
    }

    @Transactional
    public void deleteCategory(Long id) {
        categories.delete(access.require(categories, id, "Category"));
        categories.flush();
    }

    public List<NameView> tags() {
        return tags.findAll().stream().map(t -> new NameView(t.getId(), t.getName())).toList();
    }

    @Transactional
    public NameView createTag(@Valid NameInput input) {
        if (tags.existsByName(input.name().trim()))
            throw DomainException.conflict("Tag already exists");
        Tag t = new Tag();
        t.setName(input.name().trim());
        tags.save(t);
        return new NameView(t.getId(), t.getName());
    }

    @Transactional
    public NameView updateTag(Long id, @Valid NameInput input) {
        Tag t = access.require(tags, id, "Tag");
        if (!t.getName().equals(input.name().trim()) && tags.existsByName(input.name().trim()))
            throw DomainException.conflict("Tag already exists");
        t.setName(input.name().trim());
        return new NameView(id, t.getName());
    }

    @Transactional
    public void deleteTag(Long id) {
        tags.delete(access.require(tags, id, "Tag"));
        tags.flush();
    }
}
