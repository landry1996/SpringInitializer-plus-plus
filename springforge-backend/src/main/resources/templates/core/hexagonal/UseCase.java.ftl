package ${packageName}.${moduleName}.application;

public interface ${entityName}UseCase {

    ${entityName}Response execute(${entityName}Command command);

    record ${entityName}Command(String name) {}

    record ${entityName}Response(java.util.UUID id, String name) {}
}
