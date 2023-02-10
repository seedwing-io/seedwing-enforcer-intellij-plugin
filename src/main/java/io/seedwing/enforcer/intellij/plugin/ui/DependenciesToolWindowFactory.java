package io.seedwing.enforcer.intellij.plugin.ui;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class DependenciesToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DependenciesToolWindow window = new DependenciesToolWindow(toolWindow);
        ContentFactory factory = ContentFactory.SERVICE.getInstance();
        Content content = factory.createContent(window.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
