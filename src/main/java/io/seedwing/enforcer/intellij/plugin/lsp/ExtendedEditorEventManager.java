package io.seedwing.enforcer.intellij.plugin.lsp;

import static org.wso2.lsp4intellij.utils.ApplicationUtils.computableReadAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.listeners.LSPCaretListenerImpl;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class ExtendedEditorEventManager extends EditorEventManager {

    private List<Diagnostic> diagnostics = new ArrayList<>();

    public ExtendedEditorEventManager(
            Editor editor,
            DocumentListener documentListener,
            EditorMouseListener mouseListener,
            EditorMouseMotionListener mouseMotionListener,
            LSPCaretListenerImpl caretListener,
            RequestManager requestmanager,
            ServerOptions serverOptions,
            LanguageServerWrapper wrapper
    ) {
        super(
                editor,
                documentListener,
                mouseListener,
                mouseMotionListener,
                caretListener,
                requestmanager,
                serverOptions,
                wrapper
        );
    }

    @Override
    public void diagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = new ArrayList<>(diagnostics);
        updateErrorAnnotations();
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return this.diagnostics;
    }

    /**
     * Triggers force full DaemonCodeAnalyzer execution.
     */
    private void updateErrorAnnotations() {
        var project = getProject();

        computableReadAction(() -> {
            final PsiFile file = PsiDocumentManager.getInstance(project)
                    .getCachedPsiFile(this.editor.getDocument());
            if (file == null) {
                return null;
            }
            this.LOG.debug("Triggering force full DaemonCodeAnalyzer execution.");
            DaemonCodeAnalyzer.getInstance(project).restart(file);
            return null;
        });
    }
}
