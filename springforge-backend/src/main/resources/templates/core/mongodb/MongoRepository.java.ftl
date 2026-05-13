package ${packageName}.${moduleName}.infrastructure;

import ${packageName}.${moduleName}.domain.${entityName};
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ${entityName}MongoRepository extends MongoRepository<${entityName}, String> {

    List<${entityName}> findAllByOrderByCreatedAtDesc();
}
