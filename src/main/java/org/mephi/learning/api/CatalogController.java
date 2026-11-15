package org.mephi.learning.api;

import jakarta.validation.Valid;
import java.util.List;
import org.mephi.learning.api.Requests.NameInput;
import org.mephi.learning.api.Views.NameView;
import org.mephi.learning.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CourseService service;

    public CatalogController(CourseService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<NameView> categories() {
        return service.categories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public NameView category(@Valid @RequestBody NameInput input) {
        return service.createCategory(input);
    }

    @PutMapping("/categories/{id}")
    public NameView updateCategory(@PathVariable Long id, @Valid @RequestBody NameInput input) {
        return service.updateCategory(id, input);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
    }

    @GetMapping("/tags")
    public List<NameView> tags() {
        return service.tags();
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public NameView tag(@Valid @RequestBody NameInput input) {
        return service.createTag(input);
    }

    @PutMapping("/tags/{id}")
    public NameView updateTag(@PathVariable Long id, @Valid @RequestBody NameInput input) {
        return service.updateTag(id, input);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        service.deleteTag(id);
    }
}
