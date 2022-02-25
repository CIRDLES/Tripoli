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

package org.cirdles.tripoli.valueModels;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import static org.cirdles.tripoli.TripoliConstants.DEFAULT_OBJECT_NAME;

/**
 * @author James F. Bowring
 */
public class ValueModel implements Serializable, Comparable<ValueModel>, ValueModelInterface {

    private static final long serialVersionUID = -2165611302657545964L;

    private String name;
    private BigDecimal value;
    private BigDecimal oneSigmaAbs;
    private BigDecimal oneSigmaSysAbs;

    private ValueModel() {
        this(DEFAULT_OBJECT_NAME, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name) {
        this(name, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value) {
        this(name, value, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value, BigDecimal oneSigmaAbs) {
        this(name, value, oneSigmaAbs, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value, BigDecimal oneSigmaAbs, BigDecimal oneSigmaSysAbs) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
        this.oneSigmaAbs = oneSigmaAbs;
        this.oneSigmaSysAbs = oneSigmaSysAbs;
    }

    /**
     * @param name
     * @return
     */
    public static ValueModel createEmptyNamedValueModel(String name) {
        return new ValueModel(name);
    }

    public static ValueModel createFullNamedValueModel(String name, BigDecimal value, BigDecimal oneSigmaAbs, BigDecimal oneSigmaSysAbs) {
        return new ValueModel(name, value, oneSigmaAbs, oneSigmaSysAbs);
    }

    public static ValueModel createCopyOfValueModel(@NotNull ValueModel valueModel) {
        ValueModel valueModelCopy =
                createFullNamedValueModel(valueModel.getName(), valueModel.getValue(), valueModel.getOneSigmaAbs(), valueModel.getOneSigmaSysAbs());
        return valueModelCopy;
    }

    /**
     *
     * @return
     */
    public boolean hasPositiveVarUnct() {
        return oneSigmaAbs.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     *
     * @return
     */
    public boolean hasPositiveSysUnct() {
        return oneSigmaSysAbs.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * @return
     */
    public String prettyPrintValueModel() {
        return "ValueModel " + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getOneSigmaAbs() {
        return oneSigmaAbs;
    }

    public void setOneSigmaAbs(BigDecimal oneSigmaAbs) {
        this.oneSigmaAbs = oneSigmaAbs;
    }

    public BigDecimal getOneSigmaSysAbs() {
        return oneSigmaSysAbs;
    }

    public void setOneSigmaSysAbs(BigDecimal oneSigmaSysAbs) {
        this.oneSigmaSysAbs = oneSigmaSysAbs;
    }

    @Override
    public int compareTo(@NotNull ValueModel valueModel) throws ClassCastException {
        String name = valueModel.getName();
        return this.getName().trim().compareToIgnoreCase(name.trim());
    }

    // TODO: equals, hashcode, copy

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();

        ObjectStreamClass myObject = ObjectStreamClass.lookup(
                Class.forName(ValueModel.class.getCanonicalName()));
        long theSUID = myObject.getSerialVersionUID();

        System.out.println("Customized De-serialization of ValueModel " + theSUID);
    }
}