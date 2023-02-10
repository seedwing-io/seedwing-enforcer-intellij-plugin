package io.seedwing.enforcer.intellij.plugin;

import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.ProcessBuilderServerDefinition;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;

import io.seedwing.enforcer.intellij.plugin.lsp.ExtensionManager;

public class LspPreloadingActivity extends PreloadingActivity {
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {
        // FIXME: change to path to something reasonable
        var builder = new ProcessBuilder(System.getProperty("user.home") + "/git/seedwing-enforcer-lsp/target/debug/seedwing-enforcer-lsp");
        builder.environment()
                .put("RUST_LOG", "debug");
        builder
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        // The setup with "extensions" seems a bit weird. But we need to ensure that for all
        // extensions that are part of the comma seperated string, we perform a call to add the
        // extension manager.
        // Otherwise, it might happen that an extension triggers the creation of an instance without the extension

        String exts = ".enforcer.yaml,dog,pom.xml";

        IntellijLanguageClient.addServerDefinition(new ProcessBuilderServerDefinition(exts, builder));
        for (var ext : exts.split(",")) {
            IntellijLanguageClient.addExtensionManager(ext, new ExtensionManager());
        }

    }
}
