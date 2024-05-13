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
    private TripoliConstants.ReductionModeEnum reductionMode = TripoliConstants.ReductionModeEnum.BLOCK;
    private TripoliConstants.ETReduxExportTypeEnum etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.NONE;
    private int columnIndex;
    private boolean treatAsIsotopicRatio;
    private boolean displayed;
    private boolean inverted;
    private AnalysisStatsRecord analysisStatsRecord;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord = new TreeMap<>();

    public UserFunction(String name, int columnIndex) {
        this(name, columnIndex, false, true);
    }

    public UserFunction(String name, int columnIndex, boolean treatAsIsotopicRatio, boolean displayed) {
        this.name = name;
        this.etReduxName = "";
        this.invertedETReduxName = "";
        this.oxideCorrected = false;
        this.columnIndex = columnIndex;
        this.reductionMode = treatAsIsotopicRatio ? TripoliConstants.ReductionModeEnum.BLOCK : TripoliConstants.ReductionModeEnum.CYCLE;
        this.treatAsIsotopicRatio = treatAsIsotopicRatio;
        this.displayed = displayed;
        this.inverted = false;

        if (name.contains("20")) {
            etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.Pb;
        } else if (name.contains("23")) {
            etReduxExportTypeEnum = TripoliConstants.ETReduxExportTypeEnum.U;
        }
    }

    public AnalysisStatsRecord calculateAnalysisStatsRecord(AnalysisInterface analysis) {
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

    public boolean isTreatAsIsotopicRatio() {
        return treatAsIsotopicRatio;
    }

    public void setTreatAsIsotopicRatio(boolean treatAsIsotopicRatio) {
        this.treatAsIsotopicRatio = treatAsIsotopicRatio;
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

    public Map<Integer, PlotBlockCyclesRecord> getMapBlockIdToBlockCyclesRecord() {
        return mapBlockIdToBlockCyclesRecord;
    }

    public void setMapBlockIdToBlockCyclesRecord(Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord) {
        this.mapBlockIdToBlockCyclesRecord = mapBlockIdToBlockCyclesRecord;
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
}