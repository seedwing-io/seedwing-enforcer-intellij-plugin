package io.seedwing.enforcer.intellij.plugin.service;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import io.seedwing.enforcer.intellij.plugin.protocol.commands.SeedwingReport;
import io.seedwing.enforcer.intellij.plugin.ui.ReportWindow;
import io.seedwing.enforcer.intellij.plugin.ui.ReportWindowFactory;

public class ReportService {

    /**
     * Get the current active project.
     *
     * @return The current active project, may be empty but never {@code null}.
     */

    private @NotNull Optional<Project> getCurrentProject() {
        var projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return Optional.ofNullable(projects[0]);
        } else {
            return Optional.empty();
        }
    }

    private void showReport(@NotNull Project project, SeedwingReport[] report) {
        var toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(ReportWindowFactory.ID);

        if (toolWindow != null) {
            toolWindow.activate(() -> {
                ReportWindow window = new ReportWindow(toolWindow, report);
                ContentFactory factory = ContentFactory.SERVICE.getInstance();
                Content content = factory.createContent(window.getContent(), "Seedwing Report", false);
                toolWindow.getContentManager().addContent(content);
            });
        }

    }

    public void showReport(SeedwingReport[] report) {
        getCurrentProject().ifPresent(project -> showReport(project, report));
    }
}
