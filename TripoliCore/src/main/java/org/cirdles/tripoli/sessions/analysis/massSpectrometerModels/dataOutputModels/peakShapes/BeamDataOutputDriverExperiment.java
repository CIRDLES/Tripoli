package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.peakShapes;

import jama.Matrix;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.PeakShapeProcessor_OPPhoenix;
import org.cirdles.tripoli.utilities.callBacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.mathUtilities.MatLab;
import org.cirdles.tripoli.utilities.mathUtilities.SplineBasisModel;
import org.cirdles.tripoli.visualizationUtilities.linePlots.BeamShapeLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.GBeamLinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;

import java.io.IOException;
import java.nio.file.Path;

public class BeamDataOutputDriverExperiment {

    public static LinePlotBuilder[] modelTest(Path dataFile, LoggingCallbackInterface loggingCallback) throws IOException {
        PeakShapeProcessor_OPPhoenix peakShapeProcessor_opPhoenix
                = PeakShapeProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        PeakShapeOutputDataRecord peakShapeOutputDataRecord = peakShapeProcessor_opPhoenix.prepareInputDataModelFromFile(dataFile);
        LinePlotBuilder[] gBeamLinePlotBuilder = gatherBeamWidth(peakShapeOutputDataRecord, loggingCallback);

        return gBeamLinePlotBuilder;
    }

    static LinePlotBuilder[] gatherBeamWidth(PeakShapeOutputDataRecord peakShapeOutputDataRecord, LoggingCallbackInterface loggingCallback) {
        double maxBeam, maxBeamIndex, thresholdIntensity;
        // Spline basis Basis
        int basisDegree = 3;
        // int orderDiff = 2;
        double beamKnots = Math.ceil(peakShapeOutputDataRecord.beamWindow() / peakShapeOutputDataRecord.deltaMagnetMass()) - (2 * basisDegree);
        int nInterp = 1000;

        double xLower = peakShapeOutputDataRecord.peakCenterMass() - peakShapeOutputDataRecord.beamWindow() / 2;
        double xUpper = peakShapeOutputDataRecord.peakCenterMass() + peakShapeOutputDataRecord.beamWindow() / 2;

        Matrix beamMassInterp = MatLab.linspace(xLower, xUpper, nInterp);
        Matrix Basis = SplineBasisModel.bBase(beamMassInterp, xLower, xUpper, beamKnots, basisDegree);
        double deltaBeamMassInterp = beamMassInterp.get(0, 1) - beamMassInterp.get(0, 0);

        // Calculate integration matrix G, depends on matrix B and peakShapeOutputDataRecord
        int numMagnetMasses = peakShapeOutputDataRecord.magnetMassesOJ().getRowDim();
        Matrix gMatrix = new Matrix(numMagnetMasses, nInterp, 0);

        for (int iMass = 0; iMass < numMagnetMasses; iMass++) {
            Matrix term1 = MatLab.greaterOrEqual(beamMassInterp, peakShapeOutputDataRecord.collectorLimitsOJ().get(iMass, 0));
            Matrix term2 = MatLab.lessOrEqual(beamMassInterp, peakShapeOutputDataRecord.collectorLimitsOJ().get(iMass, 1));
            Matrix massesInCollector = term1.arrayTimes(term2);
            Matrix firstMassIndexInside;
            Matrix lastMassIndexInside;
            if (!(MatLab.find(massesInCollector, 1, "first").get(0, 0) == 0 && MatLab.find(massesInCollector, 1, "last").get(0, 0) == 0)) {
                firstMassIndexInside = MatLab.find(massesInCollector, 1, "first");
                lastMassIndexInside = MatLab.find(massesInCollector, 1, "last");
                for (int i = (int) firstMassIndexInside.get(0, 0) + 1; i < (int) lastMassIndexInside.get(0, 0); i++) {
                    gMatrix.set(iMass, i, deltaBeamMassInterp);

                }

                gMatrix.set(iMass, (int) firstMassIndexInside.get(0, 0), deltaBeamMassInterp / 2);
                gMatrix.set(iMass, (int) lastMassIndexInside.get(0, 0), deltaBeamMassInterp / 2);
            }
        }

        // Trim peakShapeOutputDataRecord
        int newDataSet = 0;
        Matrix hasModelBeam = MatLab.any(gMatrix, 2);
        for (int i = 0; i < hasModelBeam.getRowDimension(); i++) {
            for (int j = 0; j < hasModelBeam.getColumnDimension(); j++) {
                if (hasModelBeam.get(i, 0) == 1) {
                    newDataSet++;
                }
            }
        }

        double[][] trimGMatrix = new double[newDataSet][gMatrix.getColumnDimension()];
        int j = 0;
        for (int i = 0; i < gMatrix.getRowDimension(); i++) {
            if (hasModelBeam.get(i, 0) > 0) {
                trimGMatrix[j] = gMatrix.getArray()[i];
                j++;
            }
        }
        Matrix TrimGMatrix = new Matrix(trimGMatrix);

        double[][] trimMagnetMasses = new double[newDataSet][peakShapeOutputDataRecord.magnetMassesOJ().getRowDim()];
        int h = 0;

        for (int i = 0; i < peakShapeOutputDataRecord.magnetMassesOJ().getRowDim(); i++) {
            if (hasModelBeam.get(i, 0) > 0) {
                trimMagnetMasses[h] = peakShapeOutputDataRecord.magnetMassesOJ().toRawCopy2D()[i];

                h++;
            }
        }

        double[][] trimPeakIntensity = new double[newDataSet][peakShapeOutputDataRecord.magnetMassesOJ().getRowDim()];
        int k = 0;
        for (int i = 0; i < peakShapeOutputDataRecord.measuredPeakIntensitiesOJ().getRowDim(); i++) {
            if (hasModelBeam.get(i, 0) > 0) {
                trimPeakIntensity[k] = peakShapeOutputDataRecord.measuredPeakIntensitiesOJ().toRawCopy2D()[i];
                k++;
            }
        }

        Matrix magnetMasses = new Matrix(trimMagnetMasses);
        Matrix measuredPeakIntensities = new Matrix(trimPeakIntensity);

        double[] massData = new Matrix(trimMagnetMasses).transpose().getArray()[0];
        double[] intensityData = new Matrix(trimPeakIntensity).transpose().getArray()[0];


        // WLS and NNLS
        Matrix GB = TrimGMatrix.times(Basis);
        Matrix WData = MatLab.diag(MatLab.rDivide(MatLab.max(measuredPeakIntensities, 1), 1));
        Matrix test1 = new Matrix(WData.chol().getL().getArray()).times(GB);
        Matrix test2 = new Matrix(WData.chol().getL().getArray()).times(measuredPeakIntensities);
        Matrix BeamWNNLS = MatLab.solveNNLS(test1, test2);

        // Determine peak width
        Matrix beamShape = Basis.times(BeamWNNLS);
        maxBeam = beamShape.normInf();
        maxBeamIndex = 0;
        int index = 0;
        for (int i = 0; i < beamShape.getRowDimension(); i++) {
            for (int l = 0; l < beamShape.getColumnDimension(); l++) {
                if (beamShape.get(i, l) == maxBeam) {
                    maxBeamIndex = index;
                    break;
                }
                index++;
            }
        }
        thresholdIntensity = maxBeam * (0.01);

        Matrix peakLeft = beamShape.getMatrix(0, (int) maxBeamIndex - 1, 0, 0);
        Matrix leftAboveTheshold = MatLab.greaterThan(peakLeft, thresholdIntensity);
        Matrix leftThesholdChange = leftAboveTheshold.getMatrix(1, leftAboveTheshold.getRowDimension() - 1, 0, 0).minus(leftAboveTheshold.getMatrix(0, leftAboveTheshold.getRowDimension() - 2, 0, 0));
        int leftBoundary = (int) (MatLab.find(leftThesholdChange, 1, "last").get(0, 0) + 1);

        Matrix peakRight = beamShape.getMatrix((int) maxBeamIndex, beamShape.getRowDimension() - 1, 0, 0);
        Matrix rightAboveThreshold = MatLab.greaterThan(peakRight, thresholdIntensity);
        Matrix rightThesholdChange = rightAboveThreshold.getMatrix(0, rightAboveThreshold.getRowDimension() - 2, 0, 0).minus(rightAboveThreshold.getMatrix(1, rightAboveThreshold.getRowDimension() - 1, 0, 0));
        int rightBoundary = (int) (MatLab.find(rightThesholdChange, 1, "first").get(0, 0) + maxBeamIndex);

        Matrix gBeam = TrimGMatrix.times(beamShape);

        LinePlotBuilder[] linePlots = new LinePlotBuilder[2];
        // "beamShape"
        BeamShapeLinePlotBuilder beamShapeLinePlotBuilder
                = BeamShapeLinePlotBuilder.initializeBeamShapeLinePlot(beamMassInterp.getArray()[0], beamShape.transpose().getArray()[0], leftBoundary, rightBoundary);

        GBeamLinePlotBuilder gBeamLinePlotBuilder
                = GBeamLinePlotBuilder.initializeGBeamLinePlot(magnetMasses.transpose().getArray()[0], gBeam.transpose().getArray()[0], massData, intensityData);

        linePlots[0] = beamShapeLinePlotBuilder;
        linePlots[1] = gBeamLinePlotBuilder;

        return linePlots;
    }
}