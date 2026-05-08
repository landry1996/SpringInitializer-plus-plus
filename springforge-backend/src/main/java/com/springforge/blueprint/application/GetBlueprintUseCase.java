package com.springforge.blueprint.application;

import com.springforge.blueprint.domain.BlueprintRepository;
import com.springforge.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetBlueprintUseCase {

    private final BlueprintRepository blueprintRepository;

    public GetBlueprintUseCase(BlueprintRepository blueprintRepository) {
        this.blueprintRepository = blueprintRepository;
    }

    public BlueprintResponse execute(UUID id) {
        return blueprintRepository.findById(id)
                .map(BlueprintResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Blueprint", id));
    }
}
