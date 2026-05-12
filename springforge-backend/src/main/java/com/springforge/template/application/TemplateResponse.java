package com.springforge.template.application;

import com.springforge.template.domain.Template;
import java.util.UUID;

public record TemplateResponse(
        UUID id, String name, String path, String category,
        String scope, String content, int version, String blueprintType
) {
    public static TemplateResponse from(Template t) {
        return new TemplateResponse(t.getId(), t.getName(), t.getPath(), t.getCategory(),
                t.getScope().name(), t.getContent(), t.getVersion(), t.getBlueprintType());
    }
}
