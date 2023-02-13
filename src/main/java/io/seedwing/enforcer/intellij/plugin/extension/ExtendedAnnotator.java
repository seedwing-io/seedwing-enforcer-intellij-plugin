package io.seedwing.enforcer.intellij.plugin.extension;

import static io.seedwing.enforcer.intellij.plugin.Conversions.convertRange;
import static io.seedwing.enforcer.intellij.plugin.Conversions.mapSeverity;
import static io.seedwing.enforcer.intellij.plugin.lsp.ExtendedEditorEventManager.findManager;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiFile;

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

            // problem group

            builder = builder.problemGroup(diagnostic::getSource);

            // now build the annotation

            builder
                    .create();
        }
    }

}
