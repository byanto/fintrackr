package com.budiyanto.fintrackr;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.budiyanto.fintrackr",
        importOptions = {
                ImportOption.DoNotIncludeTests.class
        }
)
class ApplicationArchitectureTests {

    @ArchTest
    static final ArchRule domainPurityRule =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework..");

    @ArchTest
    static final ArchRule portfolioReconstitutionRule =
            noClasses()
                    .that().resideOutsideOfPackage("..adapter.out.persistence..")
                    .should().callMethodWhere(
                            target(name("reconstitute"))
                                    .and(target(owner(type(com.budiyanto.fintrackr.portfolio.domain.model.Portfolio.class))))
                    );

    @ArchTest
    static final ArchRule noSetterInDomainRule =
            noMethods()
                    .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                    .should().haveNameMatching("set[A-Z].*");
}
