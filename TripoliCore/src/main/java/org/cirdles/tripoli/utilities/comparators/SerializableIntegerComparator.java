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

package org.cirdles.tripoli.utilities.comparators;

import java.util.Comparator;

/**
 * @author James F. Bowring
 */
public enum SerializableIntegerComparator {
    ;
    // see: https://stackoverflow.com/questions/20978922/java-unable-to-serialize-a-objects-which-contain-treemaps-with-comparators
    public static final Comparator<Integer> SERIALIZABLE_COMPARATOR
            = new IntegerComparatorSerializable();

    private static class IntegerComparatorSerializable
            implements Comparator<Integer>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability
        private static final long serialVersionUID = 1L;

        public int compare(Integer i1, Integer i2) {
            return Integer.compare(i1, i2);
        }

        /**
         * Replaces the de-serialized object.
         */
        private Object readResolve() {
            return SERIALIZABLE_COMPARATOR;
        }
    }
}