package ${packageName};

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
<#if architecture == "HEXAGONAL" || architecture == "DDD">
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
</#if>

@AnalyzeClasses(packages = "${packageName}", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

<#if architecture == "HEXAGONAL">
    @ArchTest
    static final ArchRule hexagonal_architecture =
        layeredArchitecture().consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("API").definedBy("..api..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "API")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("API", "Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

<#elseif architecture == "DDD">
    @ArchTest
    static final ArchRule ddd_layered_architecture =
        layeredArchitecture().consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("API").definedBy("..api..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "API")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("API")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

<#elseif architecture == "LAYERED">
    @ArchTest
    static final ArchRule layered_architecture =
        layeredArchitecture().consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

</#if>
    @ArchTest
    static final ArchRule controllers_should_be_in_api_package =
        classes().that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..api..");

    @ArchTest
    static final ArchRule no_spring_in_domain =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..");
}
