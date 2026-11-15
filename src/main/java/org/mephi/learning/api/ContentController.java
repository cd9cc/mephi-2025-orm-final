package org.mephi.learning.api;

import jakarta.validation.Valid;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContentController {
    private final ContentService service;

    public ContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping("/modules/{id}")
    public ModuleView module(@PathVariable Long id) {
        return service.module(id);
    }

    @PutMapping("/modules/{id}")
    public ModuleView updateModule(@PathVariable Long id, @Valid @RequestBody ModuleInput input) {
        return service.updateModule(id, input);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable Long id) {
        service.deleteModule(id);
    }

    @GetMapping("/lessons/{id}")
    public LessonView lesson(@PathVariable Long id) {
        return service.lesson(id);
    }

    @PutMapping("/lessons/{id}")
    public LessonView updateLesson(@PathVariable Long id, @Valid @RequestBody LessonInput input) {
        return service.updateLesson(id, input);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable Long id) {
        service.deleteLesson(id);
    }

    @GetMapping("/assignments/{id}")
    public AssignmentView assignment(@PathVariable Long id) {
        return service.assignment(id);
    }

    @PutMapping("/assignments/{id}")
    public AssignmentView updateAssignment(
            @PathVariable Long id, @Valid @RequestBody AssignmentInput input) {
        return service.updateAssignment(id, input);
    }

    @DeleteMapping("/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssignment(@PathVariable Long id) {
        service.deleteAssignment(id);
    }

    @PostMapping("/modules/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonView addLesson(@PathVariable Long id, @Valid @RequestBody LessonInput input) {
        return service.addLesson(id, input);
    }

    @PostMapping("/lessons/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentView addAssignment(
            @PathVariable Long id, @Valid @RequestBody AssignmentInput input) {
        return service.addAssignment(id, input);
    }

    @PostMapping("/lessons/{id}/materials")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialView addMaterial(
            @PathVariable Long id, @Valid @RequestBody MaterialInput input) {
        return service.addMaterial(id, input);
    }

    @PutMapping("/materials/{id}")
    public MaterialView updateMaterial(
            @PathVariable Long id, @Valid @RequestBody MaterialInput input) {
        return service.updateMaterial(id, input);
    }

    @DeleteMapping("/materials/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMaterial(@PathVariable Long id) {
        service.deleteMaterial(id);
    }
}
