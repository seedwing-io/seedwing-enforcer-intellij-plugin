package io.seedwing.enforcer.intellij.plugin.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;

import io.seedwing.enforcer.intellij.plugin.service.Dependency;
import io.seedwing.enforcer.intellij.plugin.service.DiscoveredDependencies;

public class DependenciesToolWindow {

    private final DefaultTreeModel model;

    private final DefaultMutableTreeNode root;

    private final Map<String, DefaultMutableTreeNode> roots = new ConcurrentHashMap<>();

    private final Tree contentTree;

    public DependenciesToolWindow(ToolWindow toolWindow) {

        this.contentTree = new SimpleTree();
        this.contentTree.setRootVisible(false);

        Disposer.register(
                toolWindow.getDisposable(),
                ApplicationManager.getApplication().getService(DiscoveredDependencies.class)
                        .register(state -> {
                            ApplicationManager.getApplication()
                                    .invokeLater(() -> setDependencies(state));
                        })
        );

        this.root = new DefaultMutableTreeNode();
        this.model = new DefaultTreeModel(this.root);

        this.contentTree.setModel(this.model);
    }

    private void setDependencies(Map<String, List<Dependency>> dependencies) {
        for (var entry : dependencies.entrySet()) {
            setDependenciesFor(entry.getKey(), entry.getValue());
        }
    }

    private void setDependenciesFor(String root, List<Dependency> dependencies) {
        var node = this.roots.computeIfAbsent(root, x -> {
            var newNode = new DefaultMutableTreeNode(x);
            this.root.add(newNode);
            //this.model.insertNodeInto(newNode, this.root, this.root.getChildCount());
            this.model.nodeStructureChanged(this.root);
            return newNode;
        });

        var ds = new ArrayList<>(dependencies);
        ds.sort(Comparator.comparing(Dependency::getPurl));

        // FIXME: this is ugly, but painless for now

        node.removeAllChildren();
        for (var d : ds) {
            var depNode = new DefaultMutableTreeNode(d.getPurl());
            node.add(depNode);
        }
        this.model.nodeStructureChanged(node);
    }

    public JComponent getContent() {
        return this.contentTree;
    }

}
