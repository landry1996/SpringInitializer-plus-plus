package ${packageName}.${moduleName}.infrastructure;

import ${packageName}.${moduleName}.domain.${entityName};
import ${packageName}.${moduleName}.domain.${entityName}Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ${entityName}JpaAdapter implements ${entityName}Repository {

    private final ${entityName}JpaRepository jpaRepository;

    public ${entityName}JpaAdapter(${entityName}JpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<${entityName}> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public ${entityName} save(${entityName} entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

interface ${entityName}JpaRepository extends JpaRepository<${entityName}, UUID> {}
