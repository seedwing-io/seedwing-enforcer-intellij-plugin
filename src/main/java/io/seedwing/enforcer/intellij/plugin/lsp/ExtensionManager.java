package io.seedwing.enforcer.intellij.plugin.lsp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.extensions.LSPExtensionManager;
import org.wso2.lsp4intellij.listeners.EditorMouseListenerImpl;
import org.wso2.lsp4intellij.listeners.EditorMouseMotionListenerImpl;
import org.wso2.lsp4intellij.listeners.LSPCaretListenerImpl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;

public class ExtensionManager implements LSPExtensionManager {

    /**
     * A handler for handling commands locally.
     */
    @FunctionalInterface
    public interface CommandHandler {
        void handle(List<Object> arguments) throws Exception;
    }

    /**
     * A handler for local commands, enforcing a type.
     * <br>
     * The handler expects exactly one argument, which must be assignable to the provided class.
     *
     * @param <T> The type of the first argument to enforce.
     */
    @FunctionalInterface
    public interface TypedCommandHandler<T> {
        void handle(T argument) throws Exception;

        static <T> CommandHandler typedGsonHandler(Class<T> clazz, TypedCommandHandler<T> handler) {
            return arguments -> {

                if (arguments.size() != 1) {
                    throw new Exception("Handler must be called with exactly one argument, got: " + arguments.size());
                }

                var arg = arguments.get(0);

                final T argument;
                if (arg == null) {
                    argument = null;
                } else if (arg instanceof JsonElement) {
                    var gson = new GsonBuilder().create();
                    argument = gson.fromJson((JsonElement) arg, clazz);
                } else {
                    throw new Exception("Handler argument must be a GSON JsonElement");
                }

                handler.handle(argument);
            };
        }
    }

    private final Map<String, CommandHandler> commands = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DefaultRequestManager> T getExtendedRequestManagerFor(
            LanguageServerWrapper wrapper,
            LanguageServer server,
            LanguageClient client,
            ServerCapabilities serverCapabilities
    ) {
        return (T) new ExtendedRequestManager(
                wrapper,
                server,
                client,
                serverCapabilities,
                this.commands
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EditorEventManager> T getExtendedEditorEventManagerFor(
            Editor editor,
            DocumentListener documentListener,
            EditorMouseListenerImpl mouseListener,
            EditorMouseMotionListenerImpl mouseMotionListener,
            LSPCaretListenerImpl caretListener,
            RequestManager requestManager,
            ServerOptions serverOptions,
            LanguageServerWrapper wrapper
    ) {
        return (T) new ExtendedEditorEventManager(
                editor,
                documentListener,
                mouseListener,
                mouseMotionListener,
                caretListener,
                requestManager,
                serverOptions,
                wrapper
        );
    }

    @Override
    public Class<? extends LanguageServer> getExtendedServerInterface() {
        return ExtendedLanguageServer.class;
    }

    @Override
    public LanguageClient getExtendedClientFor(ClientContext context) {
        return new ExtendedLanguageClient(context);
    }

    public ExtensionManager registerLocalCommand(String command, CommandHandler handler) {
        this.commands.put(command, handler);
        return this;
    }
}
