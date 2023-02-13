package io.seedwing.enforcer.intellij.plugin.ui;

import java.awt.*;

import javax.swing.*;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;

import io.seedwing.enforcer.intellij.plugin.protocol.commands.SeedwingReport;

public class ReportWindow {
    private static final Logger LOG = Logger.getInstance(ReportWindow.class);

    private final JComponent content;

    private final JBCefBrowser browser;

    /**
     * The, more or less, unaltered seeding reports CSS.
     */
    private static final String SEEDWING_STYLES;

    /**
     * Styles used by this plugin to make it look better.
     */
    private static final String PLUGIN_STYLES;

    static {
        String styles = "";
        try (var stream = ReportWindow.class.getResourceAsStream("/assets/styles.css")) {
            styles = new String(stream.readAllBytes());
        } catch (Exception e) {
            LOG.error("Failed to load resources", e);
        }
        SEEDWING_STYLES = styles;

        styles = "";
        try (var stream = ReportWindow.class.getResourceAsStream("/assets/plugin.css")) {
            styles = new String(stream.readAllBytes());
        } catch (Exception e) {
            LOG.error("Failed to load resources", e);
        }
        PLUGIN_STYLES = styles;
    }

    public ReportWindow(ToolWindow toolWindow, SeedwingReport[] reports) {

        if (!JBCefApp.isSupported()) {
            this.browser = null;
            this.content = new JLabel("Browser view not supported");
            // Fallback to an alternative browser-less solution
            return;
        }

        this.browser = new JBCefBrowser();
        this.content = this.browser.getComponent();
        this.browser.setOpenLinksInExternalBrowser(false);

        this.browser.loadHTML(renderHtml(reports));

    }

    private String themeStyles() {
        /*
         * It's a mess! I hope there is a better way to deal with this, but I didn't find anything reasonable so far.
         * Colors can be extracted to some degree, but fonts just feels impossible. So let's not bother with
         * "font family names", HiDPI font size issues, and the missing "weight" information, or the fact that the IDE
         * has more fonts than the system, but the HTML viewer hasn't. Let's just define something  reasonable for the user.
         */
        StringBuilder s = new StringBuilder();

        s.append(":root {\n");

        var global = EditorColorsManager.getInstance().getGlobalScheme();

        appendColorVariable(s, "--vscode-editor-background", global.getDefaultBackground());
        appendColorVariable(s, "--vscode-editor-foreground", global.getDefaultForeground());

        appendColorVariable(s, "--vscode-panel-background", global.getDefaultBackground());
        appendColorVariable(s, "--vscode-panelTitle-activeForeground", global.getDefaultForeground());

        s.append("}\n");

        if (isLight()) {
            s.append(":root { color-scheme: light; }");
        } else {
            s.append(":root { color-scheme: dark; }");
        }

        s.append("\n\nhtml {\n" +
                "  color: var(--vscode-editor-foreground);\n" +
                "  background-color: var(--vscode-editor-background);\n" +
                "}\n\n");

        return s.toString();
    }

    /**
     * Light or dark?
     *
     * @return {@code true} if this theme might be light, {@code false} otherwise
     */
    private static boolean isLight() {

        var global = EditorColorsManager.getInstance().getGlobalScheme();

        // we calculate the V from HSV and use if to check, if it might be light or dark.
        // this leans towards a light theme, but it is better than nothing.

        var max = Math.max(global.getDefaultBackground().getRed(), Math.max(global.getDefaultBackground().getGreen(), global.getDefaultBackground().getBlue()));
        return max > 64;
    }

    private static void appendColorVariable(StringBuilder s, String name, Color color) {
        s.append(name).append(": rgb(")
                .append(color.getRed()).append(',').append(color.getGreen()).append(',').append(color.getBlue())
                .append(");\n");
    }

    private String renderHtml(SeedwingReport[] reports) {

        StringBuilder s = new StringBuilder();

        var classes = "";
        if (isLight()) {
            classes = "vscode-light";
        }

        s.append("<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>Seedwing Enforcer Report</title>\n" +
                "    <style>").append(themeStyles()).append(SEEDWING_STYLES).append(PLUGIN_STYLES).append("</style>\n" +
                "  </head>\n" +
                "  <body class=\"").append(classes).append("\">\n" +
                "    <header>\n" +
                "      <h1>Seedwing Enforcer Report</h1>\n" +
                "    </header>\n" +
                "    <main>\n");

        for (SeedwingReport report : reports) {
            s
                    .append("<section>")
                    .append("<h2>").append(report.getTitle()).append("</h2>")
                    .append("<div class=\"sw-rationale\">")
                    .append(report.getHtml())
                    .append("</div>")
                    .append("</section>")
                    .append('\n');
        }

        s.append("    </main>\n" +
                "  </body>\n" +
                "</html>");

        return s.toString();

    }

    public JComponent getContent() {
        return this.content;
    }
}
