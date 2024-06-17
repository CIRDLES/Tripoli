package org.cirdles.tripoli.utilities.collections;

import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.SpeciesColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class TripoliSpeciesColorMap implements Map<SpeciesRecordInterface, SpeciesColors>, Serializable {
    private final Map<SpeciesRecordInterface, SpeciesColors> mapOfSpeciesToColors;

    public TripoliSpeciesColorMap() {
        super();
        mapOfSpeciesToColors = Collections.synchronizedSortedMap(new TreeMap<>());
    }

    @Override
    public int size() {
        return mapOfSpeciesToColors.size();
    }

    @Override
    public boolean isEmpty() {
        return mapOfSpeciesToColors.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapOfSpeciesToColors.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mapOfSpeciesToColors.containsValue(value);
    }

    @Override
    public SpeciesColors get(Object key) {
        return mapOfSpeciesToColors.get(key);
    }

    @Nullable
    @Override
    public SpeciesColors put(SpeciesRecordInterface key, SpeciesColors value) {
        return mapOfSpeciesToColors.put(key,value);
    }

    @Override
    public SpeciesColors remove(Object key) {
        return mapOfSpeciesToColors.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends SpeciesRecordInterface, ? extends SpeciesColors> m) {
        m.forEach((this::put));
    }

    @Override
    public void clear() {
        mapOfSpeciesToColors.clear();
    }

    @NotNull
    @Override
    public Set<SpeciesRecordInterface> keySet() {
        return mapOfSpeciesToColors.keySet();
    }

    @NotNull
    @Override
    public Collection<SpeciesColors> values() {
        return mapOfSpeciesToColors.values();
    }

    @NotNull
    @Override
    public Set<Entry<SpeciesRecordInterface, SpeciesColors>> entrySet() {
        return mapOfSpeciesToColors.entrySet();
    }
}
