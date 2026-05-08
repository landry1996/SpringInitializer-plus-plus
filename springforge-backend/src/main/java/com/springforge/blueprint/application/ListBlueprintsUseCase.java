package com.springforge.blueprint.application;

import com.springforge.blueprint.domain.ArchitectureType;
import com.springforge.blueprint.domain.BlueprintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListBlueprintsUseCase {

    private final BlueprintRepository blueprintRepository;

    public ListBlueprintsUseCase(BlueprintRepository blueprintRepository) {
        this.blueprintRepository = blueprintRepository;
    }

    public List<BlueprintResponse> execute(String type) {
        if (type != null && !type.isBlank()) {
            return blueprintRepository.findByType(ArchitectureType.valueOf(type.toUpperCase()))
                    .stream().map(BlueprintResponse::from).toList();
        }
        return blueprintRepository.findAll().stream().map(BlueprintResponse::from).toList();
    }
}
