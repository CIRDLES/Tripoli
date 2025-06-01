/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.utilities.collections;

import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.settings.plots.species.SpeciesColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_DEFAULT_HEX_COLORS;

public class TripoliSpeciesColorMap implements Map<SpeciesRecordInterface, SpeciesColors>, Serializable {
    private final Map<SpeciesRecordInterface, SpeciesColors> mapOfSpeciesToColors;
    private int index;

    public TripoliSpeciesColorMap() {
        super();
        mapOfSpeciesToColors = Collections.synchronizedSortedMap(new TreeMap<>());
        index = 0;
    }

    public TripoliSpeciesColorMap(Map<SpeciesRecordInterface, SpeciesColors> other) {
        this();
        other.forEach(((speciesRecordInterface, speciesColors) ->
                put(speciesRecordInterface.copy(), speciesColors.copy())));
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
        if (key instanceof SpeciesRecordInterface speciesRecordInterfaceKey && !containsKey(key)) {
            put(speciesRecordInterfaceKey,
                    new SpeciesColors(
                            TRIPOLI_DEFAULT_HEX_COLORS.get(index * 4),
                            TRIPOLI_DEFAULT_HEX_COLORS.get(index * 4 + 1),
                            TRIPOLI_DEFAULT_HEX_COLORS.get(index * 4 + 2),
                            TRIPOLI_DEFAULT_HEX_COLORS.get(index * 4 + 3)
                    ));
            index = (index + 4) % TRIPOLI_DEFAULT_HEX_COLORS.length();
        }
        return mapOfSpeciesToColors.get(key);
    }

    @Nullable
    @Override
    public SpeciesColors put(SpeciesRecordInterface key, SpeciesColors value) {
        return mapOfSpeciesToColors.put(key, value);
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
