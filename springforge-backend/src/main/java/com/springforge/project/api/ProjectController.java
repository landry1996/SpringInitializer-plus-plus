package com.springforge.project.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springforge.project.application.CreateProjectRequest;
import com.springforge.project.application.ProjectResponse;
import com.springforge.project.application.UpdateProjectRequest;
import com.springforge.project.domain.Project;
import com.springforge.project.domain.ProjectRepository;
import com.springforge.project.domain.ProjectStatus;
import com.springforge.shared.exception.ResourceNotFoundException;
import com.springforge.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public ProjectController(ProjectRepository projectRepository, ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        String configJson = serializeConfig(request.configuration());
        Project project = new Project(
                request.name(), request.groupId(), request.artifactId(),
                request.description(), configJson, user.id()
        );
        return ProjectResponse.from(projectRepository.save(project));
    }

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return projectRepository.findByOwnerId(user.id()).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        if (!project.getOwnerId().equals(user.id())) {
            throw new ResourceNotFoundException("Project", id);
        }
        return ProjectResponse.from(project);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        if (!project.getOwnerId().equals(user.id())) {
            throw new ResourceNotFoundException("Project", id);
        }
        if (request.name() != null) project.setName(request.name());
        if (request.description() != null) project.setDescription(request.description());
        if (request.configuration() != null) project.setConfigJson(serializeConfig(request.configuration()));
        return ProjectResponse.from(projectRepository.save(project));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        if (!project.getOwnerId().equals(user.id())) {
            throw new ResourceNotFoundException("Project", id);
        }
        project.setStatus(ProjectStatus.DELETED);
        projectRepository.save(project);
    }

    private String serializeConfig(Object config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration format");
        }
    }
}
