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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import static org.cirdles.tripoli.constants.TripoliConstants.DEFAULT_OBJECT_NAME;

/**
 * @author James F. Bowring
 */
public class ValueModel implements Serializable, Comparable<ValueModel>, ValueModelInterface {

    private static final long serialVersionUID = -2165611302657545964L;

    protected String name;
    protected BigDecimal value;
    protected BigDecimal analyticalOneSigmaAbs;
    protected BigDecimal systematicOneSigmaAbs;

    protected ValueModel() {
        this(DEFAULT_OBJECT_NAME, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    protected ValueModel(String name) {
        this(name, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    protected ValueModel(String name, BigDecimal value) {
        this(name, value, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    protected ValueModel(String name, BigDecimal value, BigDecimal analyticalOneSigmaAbs) {
        this(name, value, analyticalOneSigmaAbs, BigDecimal.ZERO);
    }

    protected ValueModel(String name, BigDecimal value, BigDecimal analyticalOneSigmaAbs, BigDecimal systematicOneSigmaAbs) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
        this.analyticalOneSigmaAbs = analyticalOneSigmaAbs;
        this.systematicOneSigmaAbs = systematicOneSigmaAbs;
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
                createFullNamedValueModel(valueModel.name, valueModel.value, valueModel.analyticalOneSigmaAbs, valueModel.systematicOneSigmaAbs);
        return valueModelCopy;
    }

    /**
     * @return
     */
    public boolean hasPositiveVarUnct() {
        return 0 < analyticalOneSigmaAbs.compareTo(BigDecimal.ZERO);
    }

    /**
     * @return
     */
    public boolean hasPositiveSysUnct() {
        return 0 < systematicOneSigmaAbs.compareTo(BigDecimal.ZERO);
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

    public BigDecimal getAnalyticalOneSigmaAbs() {
        return analyticalOneSigmaAbs;
    }

    public void setAnalyticalOneSigmaAbs(BigDecimal analyticalOneSigmaAbs) {
        this.analyticalOneSigmaAbs = analyticalOneSigmaAbs;
    }

    public BigDecimal getSystematicOneSigmaAbs() {
        return systematicOneSigmaAbs;
    }

    public void setSystematicOneSigmaAbs(BigDecimal systematicOneSigmaAbs) {
        this.systematicOneSigmaAbs = systematicOneSigmaAbs;
    }

    @Override
    public int compareTo(@NotNull ValueModel valueModel) throws ClassCastException {
        String name = valueModel.name;
        return name.trim().compareToIgnoreCase(name.trim());
    }

    // TODO: equals, hashcode, copy

//    private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(ValueModel.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.err.println("Customized De-serialization of ValueModel " + theSUID);
//    }
}