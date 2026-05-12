package ${packageName}.${moduleName}.application;

import ${packageName}.${moduleName}.domain.${entityName};
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ${entityName}Mapper {

    ${entityName}Response toResponse(${entityName} entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ${entityName} toEntity(Create${entityName}Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(Update${entityName}Request request, @MappingTarget ${entityName} entity);
}
