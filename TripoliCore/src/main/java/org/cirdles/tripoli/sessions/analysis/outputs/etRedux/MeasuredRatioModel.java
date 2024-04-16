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

package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import com.thoughtworks.xstream.XStream;
import org.cirdles.tripoli.utilities.xml.MeasuredRatioModelXMLConverter;
import org.cirdles.tripoli.utilities.xml.XMLSerializerInterface;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Ported from OG Tripoli; based on Bowring's ValueModel
 */
public class MeasuredRatioModel implements Comparable, Serializable, XMLSerializerInterface {
    private String name;
    private double value;
    private String uncertaintyType;
    private double oneSigma;
    private boolean fracCorr; // fractionation corrected by Tripoli
    private boolean oxideCorr; // oxide corrected by Tripoli

    public MeasuredRatioModel() {
    }

    public MeasuredRatioModel(
            String name, double value, double oneSigma, boolean fracCorr, boolean oxideCorr) {
        this.name = name;
        this.value = value;
        this.uncertaintyType = "PCT";
        this.oneSigma = oneSigma;
        this.fracCorr = fracCorr;
        this.oxideCorr = oxideCorr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUncertaintyType() {
        return uncertaintyType;
    }

    public void setUncertaintyType(String uncertaintyType) {
        this.uncertaintyType = uncertaintyType;
    }

    public double getOneSigma() {
        return oneSigma;
    }

    public void setOneSigma(double oneSigma) {
        this.oneSigma = oneSigma;
    }

    public boolean isFracCorr() {
        return fracCorr;
    }

    public void setFracCorr(boolean fracCorr) {
        this.fracCorr = fracCorr;
    }

    public boolean isOxideCorr() {
        return oxideCorr;
    }

    public void setOxideCorr(boolean oxideCorr) {
        this.oxideCorr = oxideCorr;
    }

    /**
     * @param xstream
     */
    @Override
    public void customizeXstream(XStream xstream) {
        xstream.registerConverter(new MeasuredRatioModelXMLConverter());
        xstream.alias("MeasuredRatioModel", MeasuredRatioModel.class);
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof MeasuredRatioModel) {
            return (name + value).compareTo(((MeasuredRatioModel) o).name + ((MeasuredRatioModel) o).value);
        } else return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        MeasuredRatioModel that = (MeasuredRatioModel) o;
        return 0 == Double.compare(value, that.value)
                && 0 == Double.compare(oneSigma, that.oneSigma)
                && fracCorr == that.fracCorr
                && oxideCorr == that.oxideCorr
                && Objects.equals(name, that.name)
                && Objects.equals(uncertaintyType, that.uncertaintyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, uncertaintyType, oneSigma, fracCorr, oxideCorr);
    }
}
