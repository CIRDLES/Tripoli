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
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord;
import org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord;
import org.cirdles.tripoli.utilities.xml.MeasuredRatioModelXMLConverter;
import org.cirdles.tripoli.utilities.xml.XMLSerializerInterface;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord.generateGeometricMeanStats;

/**
 * Ported from OG Tripoli; based on Bowring's ValueModel
 */
public class MeasuredUserFunction implements Comparable, Serializable, XMLSerializerInterface {
    private String name;
    private double value;
    private String uncertaintyType;
    private double oneSigma;
    private boolean fracCorr; // fractionation corrected by Tripoli
    private boolean oxideCorr; // oxide corrected by Tripoli

    public MeasuredUserFunction() {
    }

    public MeasuredUserFunction(
            String name) {
        this(name, 0, 0, false, false);
    }

    public MeasuredUserFunction(
            String name, double value, double oneSigma, boolean fracCorr, boolean oxideCorr) {
        this.name = name;
        this.value = value;
        this.uncertaintyType = "PCT";
        this.oneSigma = oneSigma;
        this.fracCorr = fracCorr;
        this.oxideCorr = oxideCorr;
    }

    public void refreshStats(UserFunction userFunction) {
        AnalysisStatsRecord analysisStatsRecord = userFunction.getAnalysisStatsRecord();
        double selectedMean = 0;
        double selectedOneSigmaPct = 0;
        if (userFunction.isTreatAsIsotopicRatio()) {
            if (userFunction.getReductionMode().equals(TripoliConstants.ReductionModeEnum.BLOCK)) {
                double geoWeightedMeanRatio = exp(analysisStatsRecord.blockModeWeightedMean());
                double geoWeightedMeanRatioPlusOneSigma = exp(analysisStatsRecord.blockModeWeightedMean() + analysisStatsRecord.blockModeWeightedMeanOneSigma());
                double geoWeightedMeanRatioPlusOneSigmaPct = (geoWeightedMeanRatioPlusOneSigma - geoWeightedMeanRatio) / geoWeightedMeanRatio * 100.0;

                selectedMean = geoWeightedMeanRatio;
                selectedOneSigmaPct = geoWeightedMeanRatioPlusOneSigmaPct;

            } else {
                GeometricMeanStatsRecord geometricMeanStatsRecord =
                        generateGeometricMeanStats(analysisStatsRecord.cycleModeMean(), analysisStatsRecord.cycleModeStandardDeviation(), analysisStatsRecord.cycleModeStandardError());
                double geoMean = geometricMeanStatsRecord.geoMean();

                double geoMeanPlusOneStandardError = geometricMeanStatsRecord.geoMeanPlusOneStdErr();
                double geoMeanMinusOneStandardError = geometricMeanStatsRecord.geoMeanMinusOneStdErr();
                double geoMeanRatioPlusOneStdErrPct = (geoMeanPlusOneStandardError - geoMean) / geoMean * 100.0;
                double geoMeanRatioMinusOneStdErrPct = (geoMean - geoMeanMinusOneStandardError) / geoMean * 100.0;

                double smallerGeoMeanRatioOneStdErrPct = Math.min(geoMeanRatioPlusOneStdErrPct, geoMeanRatioMinusOneStdErrPct);

//
//
//
//                GeometricMeanStatsRecord geometricMeanStatsRecord =
//                        generateGeometricMeanStats(analysisStatsRecord.cycleModeMean(), analysisStatsRecord.cycleModeStandardDeviation(), analysisStatsRecord.cycleModeStandardError());
//                double geoMean = geometricMeanStatsRecord.geoMean();
//
//                double geoMeanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();
//                double geoMeanMinusOneStandardDeviation = geometricMeanStatsRecord.geoMeanMinusOneStdDev();
//                double geoMeanRatioPlusOneSigmaPct = (geoMeanPlusOneStandardDeviation - geoMean) / geoMean * 100.0;
//                double geoMeanRatioMinusOneSigmaPct = (geoMean - geoMeanMinusOneStandardDeviation) / geoMean * 100.0;
//                double smallerGeoMeanRatioForOneSigmaPct = Math.min(geoMeanRatioPlusOneSigmaPct, geoMeanRatioMinusOneSigmaPct);
//                int countOfTrailingDigitsForOneSigmaPct = countOfTrailingDigitsForSigFig(smallerGeoMeanRatioForOneSigmaPct, 2);
//                double plusSigmaPct = (new BigDecimal(geoMeanRatioPlusOneSigmaPct).setScale(countOfTrailingDigitsForOneSigmaPct, RoundingMode.HALF_UP)).doubleValue();

                selectedMean = geoMean;
                selectedOneSigmaPct = smallerGeoMeanRatioOneStdErrPct;
            }
        } else {
            if (userFunction.getReductionMode().equals(TripoliConstants.ReductionModeEnum.BLOCK)) {
                double weightedMean = analysisStatsRecord.blockModeWeightedMean();
                if (!Double.isNaN(weightedMean)) {
                    selectedMean = weightedMean;
                    double weightedMeanOneSigma = analysisStatsRecord.blockModeWeightedMeanOneSigma();
                    selectedOneSigmaPct = weightedMeanOneSigma / weightedMean * 100;
                }
            } else {
                double cycleModeMean = analysisStatsRecord.cycleModeMean();
                if (!Double.isNaN(cycleModeMean)) {
                    selectedMean = cycleModeMean;
                    double cycleModeStandardError = analysisStatsRecord.cycleModeStandardError();
                    selectedOneSigmaPct = cycleModeStandardError / cycleModeMean * 100;

                }
            }
        }

        setValue(selectedMean);
        setOneSigma(selectedOneSigmaPct);
        setUncertaintyType("PCT");
        setOxideCorr(userFunction.isOxideCorrected());
    }

    public String showClipBoardOutput() {
        String retval = "";
        retval += name + " (Mean, %StdErr)\n";
        retval += value + "\n";
        retval += oneSigma + "\n\n";

        return retval;
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
        xstream.alias("MeasuredUserFunctionModel", MeasuredUserFunction.class);
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof MeasuredUserFunction) {
            return (name + value).compareTo(((MeasuredUserFunction) o).name + ((MeasuredUserFunction) o).value);
        } else return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        MeasuredUserFunction that = (MeasuredUserFunction) o;
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
