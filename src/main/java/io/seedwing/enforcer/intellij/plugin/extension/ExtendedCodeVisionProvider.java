package io.seedwing.enforcer.intellij.plugin.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.utils.FileUtils;

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind;
import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.CodeVisionProvider;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.codeVision.CodeVisionState;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.CodeVisionPredefinedActionEntry;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;

import io.seedwing.enforcer.intellij.plugin.Conversions;
import io.seedwing.enforcer.intellij.plugin.lsp.ExtendedEditorEventManager;
import kotlin.Pair;

@SuppressWarnings("UnstableApiUsage")
public class ExtendedCodeVisionProvider implements CodeVisionProvider<LanguageServerWrapper> {

    private static final String ID = "seedwing.lsp";

    private static final Logger LOG = Logger.getInstance(ExtendedCodeVisionProvider.class);

    @NotNull
    @Override
    public CodeVisionAnchorKind getDefaultAnchor() {
        return CodeVisionAnchorKind.Default;
    }

    @NotNull
    @Override
    public String getGroupId() {
        return "lsp";
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Seedwing Enforcer";
    }

    @NotNull
    @Override
    public List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return new ArrayList<>();
    }

    @NotNull
    @Override
    public List<TextRange> collectPlaceholders(@NotNull Editor editor) {
        return new ArrayList<>();
    }

    @NotNull
    @Override
    public CodeVisionState computeCodeVision(@NotNull Editor editor, LanguageServerWrapper wrapper) {

        TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier(FileUtils.editorToURIString(editor));

        var result = new ArrayList<Pair<TextRange, CodeVisionEntry>>();
        try {
            var response = wrapper.getRequestManager()
                    .codeLens(new CodeLensParams(textDocumentIdentifier))
                    .get(5, TimeUnit.SECONDS);

            for (var entry : response) {
                var rangeOps = Conversions.convertRange(editor, entry.getRange());
                if (rangeOps.isEmpty()) {
                    continue;
                }
                var range = rangeOps.get();
                var vision = new ClickableTextCodeVisionEntry(
                        entry.getCommand().getTitle(),
                        ID,
                        (x, e) -> {
                            runCommand(wrapper, entry.getCommand());
                            return null;
                        }, null, entry.getCommand().getTitle(),
                        "Hover me! Oh, wait, you already did!",
                        new ArrayList<>()
                );

                result.add(new Pair<>(range, vision));
            }

        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to create code lens", e);
        }

        return new CodeVisionState.Ready(result);
    }

    @NotNull
    @Override
    public List<Pair<TextRange, CodeVisionEntry>> computeForEditor(@NotNull Editor editor, LanguageServerWrapper languageServerWrapper) {
        return new ArrayList<>();
    }

    @Override
    public void handleClick(@NotNull Editor editor, @NotNull TextRange textRange, @NotNull CodeVisionEntry codeVisionEntry) {
        if (codeVisionEntry instanceof CodeVisionPredefinedActionEntry) {
            ((CodeVisionPredefinedActionEntry) codeVisionEntry).onClick(editor);
        }
    }

    @Override
    public void handleExtraAction(@NotNull Editor editor, @NotNull TextRange textRange, @NotNull String s) {
    }

    @Override
    public LanguageServerWrapper precomputeOnUiThread(@NotNull Editor editor) {
        var managerOpt = ExtendedEditorEventManager.findManager(editor);
        return managerOpt
                .map(extendedEditorEventManager -> extendedEditorEventManager.wrapper)
                .orElse(null);
    }

    @Override
    public boolean shouldRecomputeForEditor(@NotNull Editor editor, LanguageServerWrapper languageServerWrapper) {
        return true;
    }

    private void runCommand(LanguageServerWrapper wrapper, Command command) {
        wrapper.getRequestManager()
                .executeCommand(new ExecuteCommandParams(
                        command.getCommand(),
                        command.getArguments()
                ))
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        LOG.error("Executing a language server protocol command failed", ex);
                    }
                });
    }
}
