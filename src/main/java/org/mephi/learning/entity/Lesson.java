package org.mephi.learning.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 20000)
    private String content;

    private String videoUrl;

    @Column(nullable = false)
    private int orderIndex;

    @OneToMany(
            mappedBy = "lesson",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(
            mappedBy = "lesson",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("id ASC")
    private List<LessonMaterial> materials = new ArrayList<>();

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<LessonMaterial> getMaterials() {
        return materials;
    }
}
