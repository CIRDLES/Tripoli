package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.ojalgo.RecoverableCondition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public enum SingleBlockPeakDriver {
    ;

    public static final PlotBuilder[] PLOT_BUILDERS = new PlotBuilder[0];

    public static PlotBuilder[] buildForSinglePeakBlock(int blockNumber, Map<Integer, List<File>> peakGroups) throws TripoliException {
        PlotBuilder[] plotBuilders;
        if (null == peakGroups.get(blockNumber)) {
            plotBuilders = PLOT_BUILDERS;
        } else {
            plotBuilders = new PlotBuilder[peakGroups.get(blockNumber).size()];
            try {
                for (int i = 0; i < peakGroups.get(blockNumber).size(); ++i) {
                    File peakFile = peakGroups.get(blockNumber).get(i);
                    PeakShapeProcess peakShapeProcess = PeakShapeProcess.createPeakShapeProcess(peakFile.toPath());
                    peakShapeProcess.initializePeakShapeProcess();
                    plotBuilders[i] = peakShapeProcess.beamShapeCollectorWidth(blockNumber);
                }
            } catch (RecoverableCondition | IOException e) {
                throw new TripoliException("Ojalgo RecoverableCondition");
            }
        }

        return plotBuilders;
    }


}