package com.springforge.blueprint.api;

import com.springforge.blueprint.application.BlueprintResponse;
import com.springforge.blueprint.application.GetBlueprintUseCase;
import com.springforge.blueprint.application.ListBlueprintsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintController {

    private final ListBlueprintsUseCase listBlueprintsUseCase;
    private final GetBlueprintUseCase getBlueprintUseCase;

    public BlueprintController(ListBlueprintsUseCase listBlueprintsUseCase,
                              GetBlueprintUseCase getBlueprintUseCase) {
        this.listBlueprintsUseCase = listBlueprintsUseCase;
        this.getBlueprintUseCase = getBlueprintUseCase;
    }

    @GetMapping
    public ResponseEntity<List<BlueprintResponse>> list(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(listBlueprintsUseCase.execute(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlueprintResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getBlueprintUseCase.execute(id));
    }
}
