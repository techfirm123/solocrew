package com.solocrew;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModuleStructureTests {

    @Test
    void verifiesApplicationModuleStructure() {
        ApplicationModules.of(SoloCrewApplication.class)
            .verify();
    }
}
