package io.seedwing.enforcer.intellij.plugin.lsp;

import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.DefaultLanguageClient;

import com.intellij.openapi.application.ApplicationManager;

import io.seedwing.enforcer.intellij.plugin.service.DiscoveredDependencies;
import io.seedwing.enforcer.intellij.plugin.service.UpdatedDependenciesParameter;

public class ExtendedLanguageClient extends DefaultLanguageClient implements EnforcerExtension {

    public ExtendedLanguageClient(@NotNull ClientContext context) {
        super(context);
    }

    @Override
    public void updatedDependencies(UpdatedDependenciesParameter params) {
        ApplicationManager.getApplication().getService(DiscoveredDependencies.class)
                .update(params);
    }
}
