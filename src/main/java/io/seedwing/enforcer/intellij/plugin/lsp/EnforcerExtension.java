package io.seedwing.enforcer.intellij.plugin.lsp;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import io.seedwing.enforcer.intellij.plugin.service.UpdatedDependenciesParameter;

public interface EnforcerExtension {
    @JsonNotification("enforcer/updatedDependencies")
    void updatedDependencies(UpdatedDependenciesParameter params);
}
