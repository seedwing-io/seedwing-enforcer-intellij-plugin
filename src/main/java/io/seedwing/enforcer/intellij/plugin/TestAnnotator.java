package io.seedwing.enforcer.intellij.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public class TestAnnotator extends ExternalAnnotator<Object, Object> {

    private static final Object MARKER = new Object();

    @Override
    public @Nullable Object collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return TestAnnotator.MARKER;
    }

    @Override
    public @Nullable Object doAnnotate(Object collectedInfo) {
        return TestAnnotator.MARKER;
    }

    @Override
    public void apply(@NotNull PsiFile file, Object annotationResult, @NotNull AnnotationHolder holder) {
        /*
        if (file.getName().endsWith("payload.json") || file.getName().endsWith("pom.xml")) {
            holder
                    .newAnnotation(HighlightSeverity.WARNING, "Just testing 2")
                    .create();

        }*/
    }
}
