package io.seedwing.enforcer.intellij.plugin.ui;

import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.ServerStatus;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;
import org.wso2.lsp4intellij.utils.DocumentUtils;
import org.wso2.lsp4intellij.utils.FileUtils;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import io.seedwing.enforcer.intellij.plugin.lsp.ExtendedEditorEventManager;

public class ExtendedAnnotator extends ExternalAnnotator<List<Diagnostic>, List<Diagnostic>> implements DumbAware {
    @Override
    public @Nullable List<Diagnostic> collectInformation(
            @NotNull PsiFile file,
            @NotNull Editor editor,
            boolean hasErrors
    ) {
        return findManager(file)
                .map(EditorEventManager::getDiagnostics)
                .orElse(null);
    }

    @Override
    public @Nullable List<Diagnostic> doAnnotate(List<Diagnostic> collectedInfo) {
        // we don't need to process anything here, just apply
        return collectedInfo;
    }

    @Override
    public void apply(
            @NotNull PsiFile file,
            List<Diagnostic> annotationResult,
            @NotNull AnnotationHolder holder
    ) {
        var managerOpt = findManager(file);
        if (managerOpt.isEmpty()) {
            return;
        }
        var manager = managerOpt.get();

        for (var diagnostic : annotationResult) {

            // convert range type

            var rangeOpt = convertRange(manager.editor, diagnostic.getRange());
            if (rangeOpt.isEmpty()) {
                continue;
            }
            var range = rangeOpt.get();

            // create annotation builder

            var builder = holder.newAnnotation(
                    mapSeverity(diagnostic.getSeverity()),
                    diagnostic.getMessage()
            );

            // apply range

            if (range.getStartOffset() == 0 && range.getEndOffset() == 0) {
                builder = builder.fileLevel();
            } else {
                builder = builder.range(range);
            }

            // process "deprecated" tag

            if (diagnostic.getTags() != null && diagnostic.getTags().contains(DiagnosticTag.Deprecated)) {
                builder = builder.
                        highlightType(ProblemHighlightType.LIKE_DEPRECATED);
            }

            // convert code to tooltip

            if (diagnostic.getCode() != null) {
                var tooltip = diagnostic.getCode().map(
                        code -> code,
                        Object::toString
                );
                builder = builder.tooltip(tooltip);
            }

            // now build the annotation

            builder
                    .create();
        }
    }

    private static @NotNull Optional<TextRange> convertRange(@NotNull Editor editor, @NotNull Range range) {
        final int start = DocumentUtils.LSPPosToOffset(editor, range.getStart());
        final int end = DocumentUtils.LSPPosToOffset(editor, range.getEnd());
        if (start >= end) {
            return Optional.empty();
        }
        return Optional.of(new TextRange(start, end));
    }

    private static HighlightSeverity mapSeverity(DiagnosticSeverity severity) {
        switch (severity) {
        case Error:
            return HighlightSeverity.ERROR;
        case Warning:
            return HighlightSeverity.WARNING;
        case Information:
            return HighlightSeverity.WEAK_WARNING;
        case Hint:
            return HighlightSeverity.INFORMATION;
        }

        return HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING;
    }

    private @NotNull Optional<ExtendedEditorEventManager> findManager(@NotNull PsiFile file) {
        var languageServerWrapper = LanguageServerWrapper.forVirtualFile(file.getVirtualFile(), file.getProject());
        if (languageServerWrapper == null || languageServerWrapper.getStatus() != ServerStatus.INITIALIZED) {
            return Optional.empty();
        }

        var virtualFile = file.getVirtualFile();
        if (FileUtils.isFileSupported(virtualFile) && IntellijLanguageClient.isExtensionSupported(virtualFile)) {
            var uri = FileUtils.VFSToURI(virtualFile);
            // Use file-level manager (needs fixing)

            return Optional.ofNullable(EditorEventManagerBase.forUri(uri))
                    .filter(ExtendedEditorEventManager.class::isInstance)
                    .map(ExtendedEditorEventManager.class::cast);

        } else {
            return Optional.empty();
        }
    }
}
