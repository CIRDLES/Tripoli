package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.PeakShapesOverlayBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PeakShapeProcessor_PhoenixTextFile;
import org.ojalgo.RecoverableCondition;

import java.io.IOException;
import java.nio.file.Path;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerBuiltinModelFactory.massSpectrometerModelBuiltinMap;

public class PeakShapeProcess {


    private static double measBeamWidthAMU;
    private final Path dataFile;
    private PeakShapeOutputDataRecord peakShapeOutputDataRecord;

    private PeakShapeProcess(Path dataFile) {
        this.dataFile = dataFile;
        this.peakShapeOutputDataRecord = null;
    }


    public static synchronized PeakShapeProcess createPeakShapeProcess(Path dataFile) {
        return new PeakShapeProcess(dataFile);
    }

    public static double getMeasBeamWidthAMU() {
        return measBeamWidthAMU;
    }

    public void initializePeakShapeProcess() throws IOException {
        PeakShapeProcessor_PhoenixTextFile peakShapeProcessor_PhoenixTextFile
                = PeakShapeProcessor_PhoenixTextFile.initializeWithMassSpectrometer(massSpectrometerModelBuiltinMap.get(MassSpectrometerContextEnum.PHOENIX_FULL.getMassSpectrometerName()));
        peakShapeOutputDataRecord = peakShapeProcessor_PhoenixTextFile.prepareInputDataModelFromFile(dataFile);
    }

    public synchronized PlotBuilder beamShapeCollectorWidth(int blockID) throws RecoverableCondition {
        return PeakShapesOverlayBuilder.initializePeakShape(blockID, peakShapeOutputDataRecord,
                new String[]{peakShapeOutputDataRecord.massID() + " / Peak Mass: " + peakShapeOutputDataRecord.peakCenterMass(), peakShapeOutputDataRecord.massID()},
                "Mass (amu)",
                "Peak Intensities");
    }

}