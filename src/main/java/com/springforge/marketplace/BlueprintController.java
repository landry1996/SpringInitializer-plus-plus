package com.springforge.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/marketplace/blueprints")
public class BlueprintController {

    private final BlueprintService blueprintService;

    public BlueprintController(BlueprintService blueprintService) {
        this.blueprintService = blueprintService;
    }

    @GetMapping
    public Page<Blueprint> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BlueprintCategory category,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "downloads") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        BlueprintSearchCriteria criteria = new BlueprintSearchCriteria(query, category, null, author, minRating, sortBy, sortDir);
        return blueprintService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    public Blueprint getById(@PathVariable String id) {
        return blueprintService.getById(id);
    }

    @PostMapping
    public Blueprint create(@RequestBody Blueprint blueprint) {
        return blueprintService.create(blueprint);
    }

    @PutMapping("/{id}")
    public Blueprint update(@PathVariable String id, @RequestBody Blueprint blueprint) {
        return blueprintService.update(id, blueprint);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        blueprintService.delete(id);
    }

    @PostMapping("/{id}/publish")
    public Blueprint publish(@PathVariable String id) {
        return blueprintService.publish(id);
    }

    @PostMapping("/{id}/rate")
    public Blueprint rate(@PathVariable String id, @RequestBody RatingRequest request) {
        return blueprintService.rate(id, request);
    }

    @GetMapping("/{id}/download")
    public Blueprint download(@PathVariable String id) {
        return blueprintService.download(id);
    }

    @GetMapping("/popular")
    public List<Blueprint> popular() {
        return blueprintService.getPopular();
    }

    @GetMapping("/top-rated")
    public List<Blueprint> topRated() {
        return blueprintService.getTopRated();
    }

    @GetMapping("/categories")
    public List<BlueprintCategory> categories() {
        return Arrays.asList(BlueprintCategory.values());
    }
}
