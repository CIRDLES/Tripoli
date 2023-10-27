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

package org.cirdles.tripoli.species;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author James F. Bowring
 */
public class IsotopicRatio implements Serializable, Comparable {
    @Serial
    private static final long serialVersionUID = 676151940943728743L;
    private final SpeciesRecordInterface numerator;
    private final SpeciesRecordInterface denominator;
    private double analysisMean = 0.0;
    private double analysisOneSigmaAbs = 0.0;
    private double analysisDalyFaradayGainMean = 0.0;
    private double analysisDalyFaradayGainOneSigmaAbs = 0.0;
    private double[] ratioValuesForBlockEnsembles;
    private double[] logRatioValuesForBlockEnsembles;
    private boolean displayed;

    public IsotopicRatio(SpeciesRecordInterface numerator, SpeciesRecordInterface denominator, boolean displayed) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.displayed = displayed;
        this.ratioValuesForBlockEnsembles = new double[0];
        this.logRatioValuesForBlockEnsembles = new double[0];
    }

    public String prettyPrint() {
        return numerator.prettyPrintShortForm() + " / " + denominator.prettyPrintShortForm();
    }

    public SpeciesRecordInterface getNumerator() {
        return numerator;
    }

    public SpeciesRecordInterface getDenominator() {
        return denominator;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (numerator.getMassNumber() != ((IsotopicRatio) o).getNumerator().getMassNumber()) {
            return Integer.compare(numerator.getMassNumber(), ((IsotopicRatio) o).getNumerator().getMassNumber());
        } else {
            return Integer.compare(denominator.getMassNumber(), ((IsotopicRatio) o).getDenominator().getMassNumber());
        }
    }

    private double ratioValue() {
        return (numerator.getMassNumber() / denominator.getMassNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        IsotopicRatio otherIsopicRatio = (IsotopicRatio) o;
        return numerator.equals(otherIsopicRatio.numerator) && denominator.equals(otherIsopicRatio.denominator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    public double[] getRatioValuesForBlockEnsembles() {
        return ratioValuesForBlockEnsembles;
    }

    public void setRatioValuesForBlockEnsembles(double[] ratioValuesForBlockEnsembles) {
        this.ratioValuesForBlockEnsembles = ratioValuesForBlockEnsembles;
    }

    public double[] getLogRatioValuesForBlockEnsembles() {
        return logRatioValuesForBlockEnsembles;
    }

    public void setLogRatioValuesForBlockEnsembles(double[] logRatioValuesForBlockEnsembles) {
        this.logRatioValuesForBlockEnsembles = logRatioValuesForBlockEnsembles;
    }

    public double getAnalysisMean() {
        return analysisMean;
    }

    public void setAnalysisMean(double analysisMean) {
        this.analysisMean = analysisMean;
    }

    public double getAnalysisOneSigmaAbs() {
        return analysisOneSigmaAbs;
    }

    public void setAnalysisOneSigmaAbs(double analysisOneSigmaAbs) {
        this.analysisOneSigmaAbs = analysisOneSigmaAbs;
    }

    public double getAnalysisDalyFaradayGainMean() {
        return analysisDalyFaradayGainMean;
    }

    public void setAnalysisDalyFaradayGainMean(double analysisDalyFaradayGainMean) {
        this.analysisDalyFaradayGainMean = analysisDalyFaradayGainMean;
    }

    public double getAnalysisDalyFaradayGainOneSigmaAbs() {
        return analysisDalyFaradayGainOneSigmaAbs;
    }

    public void setAnalysisDalyFaradayGainOneSigmaAbs(double analysisDalyFaradayGainOneSigmaAbs) {
        this.analysisDalyFaradayGainOneSigmaAbs = analysisDalyFaradayGainOneSigmaAbs;
    }

    //    private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(IsotopicRatio.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.err.println("Customized De-serialization of IsotopicRatio "
//                + theSUID);
//    }
}