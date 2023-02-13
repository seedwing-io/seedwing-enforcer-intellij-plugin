package io.seedwing.enforcer.intellij.plugin;

import java.util.Optional;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.utils.DocumentUtils;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

public final class Conversions {

    private Conversions() {
    }

    public static @NotNull Optional<TextRange> convertRange(@NotNull Editor editor, @NotNull Range range) {
        final int start = DocumentUtils.LSPPosToOffset(editor, range.getStart());
        final int end = DocumentUtils.LSPPosToOffset(editor, range.getEnd());
        if (start >= end) {
            return Optional.empty();
        }
        return Optional.of(new TextRange(start, end));
    }

    public static HighlightSeverity mapSeverity(DiagnosticSeverity severity) {
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

}
