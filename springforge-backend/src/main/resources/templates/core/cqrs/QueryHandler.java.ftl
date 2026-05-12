package ${packageName}.${moduleName}.query;

import ${packageName}.${moduleName}.domain.${entityName}ReadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ${entityName}QueryHandler {

    private final ${entityName}ReadRepository readRepository;

    public ${entityName}QueryHandler(${entityName}ReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    public ${entityName}View handle(${entityName}Query.FindById query) {
        return readRepository.findViewById(query.id())
            .orElseThrow(() -> new IllegalArgumentException("Not found: " + query.id()));
    }

    public List<${entityName}View> handle(${entityName}Query.FindAll query) {
        return readRepository.findAllViews(query.page(), query.size());
    }
}
