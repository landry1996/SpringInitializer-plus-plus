package com.springforge;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    @Disabled("ArchUnit does not yet support Java 25 class file version 69")
    void verifyModularStructure() {
        ApplicationModules.of(SpringForgeApplication.class).verify();
    }
}
