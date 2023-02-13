package io.seedwing.enforcer.intellij.plugin.lsp;

import static org.wso2.lsp4intellij.utils.ApplicationUtils.computableReadAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.Diagnostic;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.ServerStatus;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;
import org.wso2.lsp4intellij.listeners.LSPCaretListenerImpl;
import org.wso2.lsp4intellij.utils.FileUtils;

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

    public static @NotNull Optional<ExtendedEditorEventManager> findManager(@NotNull PsiFile file) {
        var languageServerWrapper = LanguageServerWrapper.forVirtualFile(file.getVirtualFile(), file.getProject());
        if (languageServerWrapper == null || languageServerWrapper.getStatus() != ServerStatus.INITIALIZED) {
            return Optional.empty();
        }

        var virtualFile = file.getVirtualFile();
        if (FileUtils.isFileSupported(virtualFile) && IntellijLanguageClient.isExtensionSupported(virtualFile)) {
            var uri = FileUtils.VFSToURI(virtualFile);
            // Use file-level manager (needs fixing)

            return cast(Optional.ofNullable(EditorEventManagerBase.forUri(uri)));

        } else {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<ExtendedEditorEventManager> findManager(@NotNull Editor editor) {
        return cast(Optional.ofNullable(EditorEventManagerBase.forEditor(editor)));
    }

    static @NotNull Optional<ExtendedEditorEventManager> cast(Optional<? super EditorEventManagerBase> manager) {
        return manager
                .filter(ExtendedEditorEventManager.class::isInstance)
                .map(ExtendedEditorEventManager.class::cast);
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
