package io.seedwing.enforcer.intellij.plugin.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.intellij.openapi.Disposable;

public class DiscoveredDependencies {

    private final Map<String, List<Dependency>> state = new HashMap<>();

    private final Map<Object, Consumer<Map<String, List<Dependency>>>> consumers = new ConcurrentHashMap<>();

    public Disposable register(Consumer<Map<String, List<Dependency>>> consumer) {

        var key = new Object();
        this.consumers.put(key, consumer);

        consumer.accept(Collections.unmodifiableMap(this.state));

        return new Disposable() {
            @Override
            public void dispose() {
                DiscoveredDependencies.this.consumers.remove(key);
            }
        };
    }

    public void update(UpdatedDependenciesParameter params) {
        this.state.put(params.getRoot(), Collections.unmodifiableList(params.getDependencies()));

        var state = Collections.unmodifiableMap(this.state);
        for (var consumer : this.consumers.values()) {
            consumer.accept(state);
        }
    }
}
