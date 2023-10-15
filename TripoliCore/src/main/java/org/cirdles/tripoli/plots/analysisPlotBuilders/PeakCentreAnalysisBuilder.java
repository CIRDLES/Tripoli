package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.PeakShapesOverlayRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PeakCentreAnalysisBuilder extends PlotBuilder {

    private PeakCentreAnalysisRecord peakCentreAnalysisRecord;
    private int blockCount;

    public PeakCentreAnalysisBuilder() {
    }

    protected PeakCentreAnalysisBuilder(int blockCount, List<PeakShapesOverlayRecord> peakShapesOverlayRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.blockCount = blockCount;
        peakCentreAnalysisRecord = generatePeakCentreAnalysis(peakShapesOverlayRecords);

    }


    public static PeakCentreAnalysisBuilder initializeAnalysisPeakCentres(int blockCount,
                                                                          List<PeakShapesOverlayRecord> peakShapesOverlayRecords,
                                                                          String[] title,
                                                                          String xAxisLabel,
                                                                          String yAxisLabel) {
        PeakCentreAnalysisBuilder peakCentreAnalysisBuilder = new PeakCentreAnalysisBuilder(blockCount, peakShapesOverlayRecords, title, xAxisLabel, yAxisLabel);
        peakCentreAnalysisBuilder.peakCentreAnalysisRecord = peakCentreAnalysisBuilder.generatePeakCentreAnalysis(peakShapesOverlayRecords);
        return peakCentreAnalysisBuilder;
    }


    private PeakCentreAnalysisRecord generatePeakCentreAnalysis(List<PeakShapesOverlayRecord> peakShapesOverlayRecords) {
        List<Double> blockIdList = new ArrayList<>();
        List<Double> peakWidths = new ArrayList<>();

        Map<Integer, PeakShapesOverlayRecord> mapBlockToPeakOverlayRecord = new TreeMap<>();
        for (PeakShapesOverlayRecord peakShapesOverlayRecord : peakShapesOverlayRecords) {
            mapBlockToPeakOverlayRecord.put(peakShapesOverlayRecord.blockID(), peakShapesOverlayRecord);
            blockIdList.add((double) peakShapesOverlayRecord.blockID());
            peakWidths.add(peakShapesOverlayRecord.peakWidth());
        }

        double[] blockIds = blockIdList.stream().mapToDouble(d -> d).toArray();
        double[] blockWidths = peakWidths.stream().mapToDouble(d -> d).toArray();

        return new PeakCentreAnalysisRecord(
                blockCount,
                mapBlockToPeakOverlayRecord,
                blockIds,
                blockWidths,
                title,
                xAxisLabel,
                yAxisLabel
        );
    }


    public PeakCentreAnalysisRecord getPeakCentreAnalysisRecord() {
        return peakCentreAnalysisRecord;
    }
}