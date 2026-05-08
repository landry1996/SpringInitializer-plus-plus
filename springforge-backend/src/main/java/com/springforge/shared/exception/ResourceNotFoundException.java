package com.springforge.shared.exception;

import java.util.UUID;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, UUID id) {
        super("RESOURCE_NOT_FOUND", resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", resource + " not found: " + identifier);
    }
}
