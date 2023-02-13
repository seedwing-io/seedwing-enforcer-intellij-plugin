package io.seedwing.enforcer.intellij.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

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
        if (file.getName().endsWith("payload.json")) {
            holder
                    .newAnnotation(HighlightSeverity.WARNING, "Just testing 2")
                    .withFix(new TestIntentionAction())
                    .create();
        }
    }

    private static class TestIntentionAction implements IntentionAction {
        @Override
        public @IntentionName @NotNull String getText() {
            return "Test fix";
        }

        @Override
        public @NotNull @IntentionFamilyName String getFamilyName() {
            return "Test fixes";
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
