package com.springforge.preset.api;

import com.springforge.preset.application.CreatePresetRequest;
import com.springforge.preset.application.PresetResponse;
import com.springforge.preset.domain.Preset;
import com.springforge.preset.domain.PresetRepository;
import com.springforge.shared.exception.ResourceNotFoundException;
import com.springforge.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/presets")
public class PresetController {

    private final PresetRepository presetRepository;

    public PresetController(PresetRepository presetRepository) {
        this.presetRepository = presetRepository;
    }

    @PostMapping
    public ResponseEntity<PresetResponse> create(
            @Valid @RequestBody CreatePresetRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Preset preset = new Preset(
            request.name(),
            request.description(),
            request.configuration(),
            user.id()
        );
        preset.setShared(request.shared());
        Preset saved = presetRepository.save(preset);
        return ResponseEntity.status(HttpStatus.CREATED).body(PresetResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<PresetResponse>> listMyPresets(@AuthenticationPrincipal AuthenticatedUser user) {
        List<PresetResponse> presets = presetRepository.findByOwnerId(user.id())
            .stream()
            .map(PresetResponse::from)
            .toList();
        return ResponseEntity.ok(presets);
    }

    @GetMapping("/shared")
    public ResponseEntity<List<PresetResponse>> listShared() {
        List<PresetResponse> presets = presetRepository.findShared()
            .stream()
            .map(PresetResponse::from)
            .toList();
        return ResponseEntity.ok(presets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresetResponse> getById(@PathVariable UUID id) {
        Preset preset = presetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Preset", id));
        return ResponseEntity.ok(PresetResponse.from(preset));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Preset preset = presetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Preset", id));
        if (!preset.getOwnerId().equals(user.id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        presetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
