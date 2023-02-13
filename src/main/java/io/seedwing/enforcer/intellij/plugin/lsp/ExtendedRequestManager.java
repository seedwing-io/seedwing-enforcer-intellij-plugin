package io.seedwing.enforcer.intellij.plugin.lsp;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;

public class ExtendedRequestManager extends DefaultRequestManager {

    private final Map<String, ExtensionManager.CommandHandler> commands;

    public ExtendedRequestManager(
            LanguageServerWrapper wrapper,
            LanguageServer server,
            LanguageClient client,
            ServerCapabilities serverCapabilities,
            Map<String, ExtensionManager.CommandHandler> commands
    ) {
        super(
                wrapper,
                server,
                client,
                serverCapabilities
        );
        this.commands = commands;
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {

        var handler = this.commands.get(params.getCommand());
        if (handler != null) {

            try {
                handler.handle(params.getArguments());
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }

        }

        var result = super.executeCommand(params);
        
        if (result == null) {
            return CompletableFuture.failedFuture(new Exception("Failed to send command request"));
        } else {
            return result;
        }
    }
}
