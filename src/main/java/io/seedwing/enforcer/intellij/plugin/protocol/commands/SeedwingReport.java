package io.seedwing.enforcer.intellij.plugin.protocol.commands;

public class SeedwingReport {

    public static String ID = "seedwingEnforcer.showReport";

    public static Class<SeedwingReport[]> ARGUMENT_TYPE = SeedwingReport[].class;

    private String title;

    private String html;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return this.html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
