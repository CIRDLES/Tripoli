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

/**
 * @author James F. Bowring
 */
public class ValueModel implements Serializable, ValueModelInterface {

    private static final long serialVersionUID = -2165611302657545964L;

    private String name;
    private BigDecimal value;
    private BigDecimal oneSigma;
    private BigDecimal oneSigmaSys;

    private ValueModel() {
        this("NO_NAME", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name) {
        this(name, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value) {
        this(name, value, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value, BigDecimal oneSigma) {
        this(name, value, oneSigma, BigDecimal.ZERO);
    }

    private ValueModel(String name, BigDecimal value, BigDecimal oneSigma, BigDecimal oneSigmaSys) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
        this.oneSigma = oneSigma;
        this.oneSigmaSys = oneSigmaSys;
    }

    /**
     * @param name
     * @return
     */
    public static ValueModel createEmptyNamedValueModel(String name) {
        return new ValueModel(name);
    }

    public static ValueModel createFullNamedValueModel(String name, BigDecimal value, BigDecimal oneSigma, BigDecimal oneSigmaSys) {
        return new ValueModel(name, value, oneSigma, oneSigmaSys);
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

    public BigDecimal getOneSigma() {
        return oneSigma;
    }

    public void setOneSigma(BigDecimal oneSigma) {
        this.oneSigma = oneSigma;
    }

    public BigDecimal getOneSigmaSys() {
        return oneSigmaSys;
    }

    public void setOneSigmaSys(BigDecimal oneSigmaSys) {
        this.oneSigmaSys = oneSigmaSys;
    }

    @Override
    public int compareTo(@NotNull ValueModel valueModel) throws ClassCastException {
        String name = valueModel.getName();
        return this.getName().trim().compareToIgnoreCase(name.trim());
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
//        System.out.println("Customized De-serialization of ValueModel " + theSUID);
//    }
}