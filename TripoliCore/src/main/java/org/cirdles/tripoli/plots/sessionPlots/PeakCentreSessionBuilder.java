package org.cirdles.tripoli.plots.sessionPlots;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.PeakShapesOverlayRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PeakCentreSessionBuilder extends PlotBuilder {

    private PeakCentreSessionRecord peakCentreSessionRecord;
    private int blockCount;

    public PeakCentreSessionBuilder(){}

    protected PeakCentreSessionBuilder(int blockCount, List<PeakShapesOverlayRecord> peakShapesOverlayRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.blockCount = blockCount;
        peakCentreSessionRecord = generatePeakCentreSession(peakShapesOverlayRecords);

    }


    public static PeakCentreSessionBuilder initializePeakCentreSession(int blockCount,
                                                                       List<PeakShapesOverlayRecord> peakShapesOverlayRecords,
                                                                       String[] title,
                                                                       String xAxisLabel,
                                                                       String yAxisLabel) {
        PeakCentreSessionBuilder peakCentreSessionBuilder = new PeakCentreSessionBuilder(blockCount, peakShapesOverlayRecords, title, xAxisLabel,yAxisLabel);
        peakCentreSessionBuilder.peakCentreSessionRecord = peakCentreSessionBuilder.generatePeakCentreSession(peakShapesOverlayRecords);
        return peakCentreSessionBuilder;
    }



    private PeakCentreSessionRecord generatePeakCentreSession(List<PeakShapesOverlayRecord> peakShapesOverlayRecords) {
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

        return new PeakCentreSessionRecord(
                blockCount,
                mapBlockToPeakOverlayRecord,
                blockIds,
                blockWidths,
                title,
                xAxisLabel,
                yAxisLabel
        );
    }


    public PeakCentreSessionRecord getPeakCentreSessionRecord() {
        return peakCentreSessionRecord;
    }
}
