package io.seedwing.enforcer.intellij.plugin.service;

import java.util.Objects;

public class Dependency {
    private final String purl;

    public Dependency(String purl) {
        this.purl = purl;
    }

    public String getPurl() {
        return this.purl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dependency that = (Dependency) o;
        return Objects.equals(this.purl, that.purl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.purl);
    }
}
