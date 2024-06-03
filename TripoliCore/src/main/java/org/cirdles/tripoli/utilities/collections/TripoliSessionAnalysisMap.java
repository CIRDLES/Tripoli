package org.cirdles.tripoli.utilities.collections;

import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class TripoliSessionAnalysisMap implements Map<String, AnalysisInterface>, Serializable{

    private final Map<String, AnalysisInterface> sessionMapOfAnalyses;
    private Session session;

    public TripoliSessionAnalysisMap() {
        super();
        this.sessionMapOfAnalyses = Collections.synchronizedSortedMap(new TreeMap<>());
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public int size() {
        return sessionMapOfAnalyses.size();
    }

    @Override
    public boolean isEmpty() {
        return sessionMapOfAnalyses.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return sessionMapOfAnalyses.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return sessionMapOfAnalyses.containsValue(value);
    }

    @Override
    public AnalysisInterface get(Object key) {
        return sessionMapOfAnalyses.get(key);
    }

    @Nullable
    @Override
    public AnalysisInterface put(String key, AnalysisInterface value) {
        if (this.session != null && value instanceof Analysis) {
            ((Analysis) value).initializeDefaultsFromSessionDefaults(this.session);
        }
        return sessionMapOfAnalyses.put(key, value);
    }

    @Override
    public AnalysisInterface remove(Object key) {
        return sessionMapOfAnalyses.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends AnalysisInterface> m) {
        if (this.session != null) {
            for (AnalysisInterface analysisInterface : m.values()) {
                if (analysisInterface instanceof Analysis) {
                   ((Analysis) analysisInterface).initializeDefaultsFromSessionDefaults(this.session);
                }
            }
        }
        sessionMapOfAnalyses.putAll(m);
    }

    @Override
    public void clear() {
        sessionMapOfAnalyses.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return sessionMapOfAnalyses.keySet();
    }

    @NotNull
    @Override
    public Collection<AnalysisInterface> values() {
        return sessionMapOfAnalyses.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, AnalysisInterface>> entrySet() {
        return sessionMapOfAnalyses.entrySet();
    }

}
