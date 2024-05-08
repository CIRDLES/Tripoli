package org.cirdles.tripoli.utilities.collections;

import org.cirdles.tripoli.species.SpeciesColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class TripoliSpeciesColorMap implements Map<Integer, SpeciesColors>, Serializable {

    private final Map<Integer, SpeciesColors> mapOfSpeciesToColors;

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
        int idx = (Integer) key % size();
        if ((Integer) key > idx) {
            putReorganize((Integer) key, mapOfSpeciesToColors.get(idx));
        }
        return mapOfSpeciesToColors.get(key);
    }

    private SpeciesColors putReorganize(Integer key, SpeciesColors value) {
        SpeciesColors oldValue = mapOfSpeciesToColors.put(key, value);
        SpeciesColors originalValue = oldValue;
        int newKey = key;
        while (oldValue != null) {
            newKey += size();
            oldValue = mapOfSpeciesToColors.put(newKey, oldValue);
        }
        return originalValue;
    }

    @Nullable
    @Override
    public SpeciesColors put(Integer key, SpeciesColors value) {

        return mapOfSpeciesToColors.put(key,value);
    }

    @Override
    public SpeciesColors remove(Object key) {
        return mapOfSpeciesToColors.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends SpeciesColors> m) {
        clear();
        mapOfSpeciesToColors.putAll(m);
    }

    @Override
    public void clear() {
        mapOfSpeciesToColors.clear();
    }

    @NotNull
    @Override
    public Set<Integer> keySet() {
        return mapOfSpeciesToColors.keySet();
    }

    @NotNull
    @Override
    public Collection<SpeciesColors> values() {
        return mapOfSpeciesToColors.values();
    }

    @NotNull
    @Override
    public Set<Entry<Integer, SpeciesColors>> entrySet() {
        return mapOfSpeciesToColors.entrySet();
    }
}
