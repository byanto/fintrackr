package com.budiyanto.fintrackr;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTests {

    private final ApplicationModules modules = ApplicationModules.of(FintrackrApplication.class);

    @Test
    void verifyModularStructure() {
        modules.verify();
    }

}
