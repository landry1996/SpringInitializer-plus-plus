package com.springforge.intellij;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

public class SpringForgeModuleBuilder extends ModuleBuilder {

    @Override
    public String getName() {
        return "SpringForge";
    }

    @Override
    public String getPresentableName() {
        return "SpringForge Project";
    }

    @Override
    public String getDescription() {
        return "Create a new Spring Boot project using SpringForge with advanced architecture patterns.";
    }

    @Override
    public ModuleType<?> getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) {
        doAddContentEntry(rootModel);
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext,
                                                 @NotNull com.intellij.ide.util.projectWizard.ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{new SpringForgeWizardStep()};
    }

    private static class SpringForgeWizardStep extends ModuleWizardStep {
        @Override
        public javax.swing.JComponent getComponent() {
            javax.swing.JPanel panel = new javax.swing.JPanel();
            panel.add(new javax.swing.JLabel("Configure your SpringForge project in the next dialog."));
            return panel;
        }

        @Override
        public void updateDataModel() {
            // Configuration is handled by ProjectConfigDialog
        }
    }
}
