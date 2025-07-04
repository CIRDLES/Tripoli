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

package org.cirdles.tripoli.expressions.userFunctions;

import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord;
import org.cirdles.tripoli.sessions.analysis.BlockStatsRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord.*;

/**
 * @author James F. Bowring
 */
public class UserFunction implements Comparable, Serializable {
    @Serial
    private static final long serialVersionUID = -5408855769497340457L;
    private String name;
    private String etReduxName;
    private String invertedETReduxName;
    private boolean oxideCorrected;
    private TripoliConstants.ReductionModeEnum reductionMode;
    private TripoliConstants.ETReduxExportTypeEnum etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.NONE;
    private int columnIndex;
    private boolean treatAsIsotopicRatio;
    private boolean treatAsCustomExpression;
    private boolean displayed;
    private boolean inverted;
    private AnalysisStatsRecord analysisStatsRecord;
    private ExpressionTreeInterface customExpression;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord = new TreeMap<>();
    private int[] concatenatedBlockCounts;

    public UserFunction(String name, int columnIndex) {
        this(name, columnIndex, false, true);
    }

    public UserFunction(String name, int columnIndex, boolean treatAsIsotopicRatio, boolean displayed) {
        this.name = name;
        this.etReduxName = "";
        this.invertedETReduxName = "";
        this.oxideCorrected = false;
        this.columnIndex = columnIndex;
        this.reductionMode = TripoliConstants.ReductionModeEnum.CYCLE;
        this.treatAsIsotopicRatio = treatAsIsotopicRatio;
        this.displayed = displayed;
        this.inverted = false;
        this.concatenatedBlockCounts = new int[]{-1};

        if (name.contains("20")) {
            etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.Pb;
        } else if (name.contains("23")) {
            etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.U;
        }
    }

    public UserFunction copy() {
        UserFunction userFunction = new UserFunction(this.name, this.columnIndex, this.treatAsIsotopicRatio, this.displayed);
        userFunction.setEtReduxName(this.etReduxName);
        userFunction.setInvertedETReduxName(this.invertedETReduxName);
        userFunction.setOxideCorrected(this.oxideCorrected);
        userFunction.setReductionMode(this.reductionMode);
        userFunction.setInverted(this.inverted);
        userFunction.setEtReduxExportType(this.etReduxExportTypeEnum);
        userFunction.setTreatAsCustomExpression(this.treatAsCustomExpression);
        userFunction.setCustomExpression(this.customExpression);

        return userFunction;
    }

    public UserFunctionDisplay calcUserFunctionDisplay() {
        return new UserFunctionDisplay(name, displayed, inverted);
    }

    public AnalysisStatsRecord calculateAnalysisStatsRecord(AnalysisInterface analysis) {
        if (!invertedETReduxName.isEmpty()) {
            // detect if ratio to be treated as function because of negative or zero value Issue #214
            boolean allPositive = true;
            for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
                PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
                if ((plotBlockCyclesRecord != null) && allPositive) {
                    for (int i = 0; i < plotBlockCyclesRecord.cycleMeansData().length; i++) {
                        if ((plotBlockCyclesRecord.cycleMeansData()[i] <= 0.0) && (plotBlockCyclesRecord.cyclesIncluded()[i])) {
                            allPositive = false;
                        }
                    }
                }
            }

            treatAsIsotopicRatio = allPositive;
        }

        analysisStatsRecord = generateAnalysisStatsRecord(generateAnalysisBlockStatsRecords(this, mapBlockIdToBlockCyclesRecord));
        for (int i = 0; i < analysisStatsRecord.blockStatsRecords().length; i++) {
            BlockStatsRecord blockStatsRecord = analysisStatsRecord.blockStatsRecords()[i];
            int blockID = blockStatsRecord.blockID();
            boolean[] cyclesIncluded = blockStatsRecord.cyclesIncluded();
            analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID,
                    analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).updateIncludedCycles(this, cyclesIncluded));
        }
        return analysisStatsRecord;
    }

    public String getName() {
        return name;
    }

    public String getEtReduxName() {
        return etReduxName;
    }

    public void setEtReduxName(String etReduxName) {
        this.etReduxName = etReduxName;
    }

    public String getInvertedETReduxName() {
        return invertedETReduxName;
    }

    public void setInvertedETReduxName(String invertedETReduxName) {
        this.invertedETReduxName = invertedETReduxName;
    }

    public String showCorrectName() {
        String retVal = name;
        if (inverted && treatAsIsotopicRatio) {
            String[] nameSplit = name.split("/");
            retVal = nameSplit[1] + "/" + nameSplit[0];
        }
        return retVal;
    }

    public boolean isOxideCorrected() {
        return oxideCorrected;
    }

    public void setOxideCorrected(boolean oxideCorrected) {
        this.oxideCorrected = oxideCorrected;
    }

    public TripoliConstants.ReductionModeEnum getReductionMode() {
        return reductionMode;
    }

    public void setReductionMode(TripoliConstants.ReductionModeEnum reductionMode) {
        this.reductionMode = reductionMode;
    }

    public TripoliConstants.ETReduxExportTypeEnum getEtReduxExportType() {
        return etReduxExportTypeEnum;
    }

    public void setEtReduxExportType(TripoliConstants.ETReduxExportTypeEnum etReduxExportTypeEnum) {
        this.etReduxExportTypeEnum = etReduxExportTypeEnum;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean isTreatAsIsotopicRatio() {
        return treatAsIsotopicRatio;
    }

    public void setTreatAsIsotopicRatio(boolean treatAsIsotopicRatio) {
        this.treatAsIsotopicRatio = treatAsIsotopicRatio;
    }

    public boolean isTreatAsCustomExpression() {return treatAsCustomExpression;}

    public void setTreatAsCustomExpression(boolean treatAsCustomExpression) {
        this.treatAsCustomExpression = treatAsCustomExpression;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public AnalysisStatsRecord getAnalysisStatsRecord() {
        return analysisStatsRecord;
    }

    public void setAnalysisStatsRecord(AnalysisStatsRecord analysisStatsRecord) {
        this.analysisStatsRecord = analysisStatsRecord;
    }

    public Map<Integer, PlotBlockCyclesRecord> getMapBlockIdToBlockCyclesRecord() {
        return mapBlockIdToBlockCyclesRecord;
    }

    public void setMapBlockIdToBlockCyclesRecord(Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord) {
        this.mapBlockIdToBlockCyclesRecord = mapBlockIdToBlockCyclesRecord;
    }

    public int[] getConcatenatedBlockCounts() {
        return concatenatedBlockCounts;
    }

    public void setConcatenatedBlockCounts(int[] concatenatedBlockCounts) {
        this.concatenatedBlockCounts = concatenatedBlockCounts;
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        UserFunction userFunction = (UserFunction) o;
        return this.name.compareTo(userFunction.name);
    }

    public String showBlockMean() {
        return prettyPrintRatioBlockMean(this);
    }

    public String showCycleMean() {
        return prettyPrintRatioCycleMean(this);
    }

    public String getCorrectETReduxName() {
        return inverted ? invertedETReduxName : etReduxName;
    }


    public ExpressionTreeInterface getCustomExpression() {
        return customExpression;
    }

    public void setCustomExpression(ExpressionTreeInterface customExpression) {
        this.customExpression = customExpression;

    }
}