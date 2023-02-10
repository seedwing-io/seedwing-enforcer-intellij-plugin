package io.seedwing.enforcer.intellij.plugin.service;

import java.util.List;

public class UpdatedDependenciesParameter {

    private String root;

    private List<Dependency> dependencies;

    public String getRoot() {
        return this.root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
