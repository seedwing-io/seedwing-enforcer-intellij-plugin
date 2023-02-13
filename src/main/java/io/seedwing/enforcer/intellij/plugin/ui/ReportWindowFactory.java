package io.seedwing.enforcer.intellij.plugin.ui;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

public class ReportWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String ID = "seedwing.report";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("Seedwing Report");
        toolWindow.setStripeTitle("Seeding Report");
    }

}
