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
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.IOException;
import java.nio.file.Path;

public class BeamDataOutputDriverExperiment {

    public static LinePlotBuilder[] modelTest(Path dataFile, LoggingCallbackInterface loggingCallback) throws IOException, RecoverableCondition {
        PeakShapeProcessor_OPPhoenix peakShapeProcessor_opPhoenix
                = PeakShapeProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        PeakShapeOutputDataRecord peakShapeOutputDataRecord = peakShapeProcessor_opPhoenix.prepareInputDataModelFromFile(dataFile);
        LinePlotBuilder[] gBeamLinePlotBuilder = gatherBeamWidth(peakShapeOutputDataRecord, loggingCallback);

        return gBeamLinePlotBuilder;
    }

    static LinePlotBuilder[] gatherBeamWidth(PeakShapeOutputDataRecord peakShapeOutputDataRecord, LoggingCallbackInterface loggingCallback) throws RecoverableCondition {

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        double maxBeam, maxBeamIndex, thresholdIntensity;
        // Spline basis Basis
        int basisDegree = 3;
        // int orderDiff = 2;
        double beamKnots = Math.ceil(peakShapeOutputDataRecord.beamWindow() / peakShapeOutputDataRecord.deltaMagnetMass()) - (2 * basisDegree);
        int nInterp = 1000;

        double xLower = peakShapeOutputDataRecord.peakCenterMass() - peakShapeOutputDataRecord.beamWindow() / 2;
        double xUpper = peakShapeOutputDataRecord.peakCenterMass() + peakShapeOutputDataRecord.beamWindow() / 2;

        Matrix beamMassInterp = MatLab.linspace(xLower, xUpper, nInterp);
        Primitive64Store beamMassInterpOJ = MatLab.linspaceOJ(xLower, xUpper, nInterp);
        Matrix Basis = SplineBasisModel.bBase(beamMassInterp, xLower, xUpper, beamKnots, basisDegree);
        Primitive64Store BasisOJ = SplineBasisModel.bBase(beamMassInterpOJ, xLower, xUpper, beamKnots, basisDegree);
        double deltaBeamMassInterp = beamMassInterpOJ.get(0, 1) - beamMassInterpOJ.get(0, 0);

        // Calculate integration matrix G, depends on matrix B and peakShapeOutputDataRecord
        int numMagnetMasses = peakShapeOutputDataRecord.magnetMassesOJ().getRowDim();
        Matrix gMatrix = new Matrix(numMagnetMasses, nInterp, 0);
        Primitive64Store gMatrixOJ = storeFactory.make(numMagnetMasses, nInterp);


        for (int iMass = 0; iMass < numMagnetMasses; iMass++) {
            Primitive64Store term1 = MatLab.greaterOrEqual(beamMassInterpOJ, peakShapeOutputDataRecord.collectorLimitsOJ().get(iMass, 0));
            Primitive64Store term2 = MatLab.lessOrEqual(beamMassInterpOJ, peakShapeOutputDataRecord.collectorLimitsOJ().get(iMass, 1));
            Primitive64Store massesInCollector = MatLab.arrayMultiply(term1,term2);
            Primitive64Store firstMassIndexInside;
            Primitive64Store lastMassIndexInside;
            if (!(MatLab.find(massesInCollector, 1, "first").get(0, 0) == 0 && MatLab.find(massesInCollector, 1, "last").get(0, 0) == 0)) {
                firstMassIndexInside = MatLab.find(massesInCollector, 1, "first");
                lastMassIndexInside = MatLab.find(massesInCollector, 1, "last");
                for (int i = (int) (firstMassIndexInside.get(0, 0) + 1); i < (int) (lastMassIndexInside.get(0, 0) + 0); i++) {
                    gMatrix.set(iMass, i, deltaBeamMassInterp);
                    gMatrixOJ.set(iMass, i, deltaBeamMassInterp);
                }

                gMatrix.set(iMass, (int) (firstMassIndexInside.get(0, 0) + 0), deltaBeamMassInterp / 2);
                gMatrix.set(iMass, (int) (lastMassIndexInside.get(0, 0) + 0), deltaBeamMassInterp / 2);
                gMatrixOJ.set(iMass, (int) (firstMassIndexInside.get(0, 0) + 0), deltaBeamMassInterp / 2);
                gMatrixOJ.set(iMass, (int) (lastMassIndexInside.get(0, 0) + 0), deltaBeamMassInterp / 2);
            }
        }

        // Trim peakShapeOutputDataRecord
        int newDataSet = 0;
        Matrix hasModelBeam = MatLab.any(gMatrix, 2);
        Primitive64Store hasModelBeamOJ = MatLab.any(gMatrixOJ, 2);
        for (int i = 0; i < hasModelBeamOJ.getRowDim(); i++) {
            for (int j = 0; j < hasModelBeamOJ.getColDim(); j++) {
                if (hasModelBeamOJ.get(i, 0) == 1) {
                    newDataSet++;
                }
            }
        }

        double[][] trimGMatrix = new double[newDataSet][gMatrixOJ.getColDim()];
        int j = 0;
        for (int i = 0; i < gMatrix.getRowDimension(); i++) {
            if (hasModelBeamOJ.get(i, 0) > 0) {
                trimGMatrix[j] = gMatrixOJ.toRawCopy2D()[i];
                j++;
            }
        }
        Matrix TrimGMatrix = new Matrix(trimGMatrix);
        Primitive64Store trimGMatrixOJ = storeFactory.rows(trimGMatrix);

        double[][] trimMagnetMasses = new double[newDataSet][peakShapeOutputDataRecord.magnetMassesOJ().getRowDim()];
        int h = 0;

        for (int i = 0; i < peakShapeOutputDataRecord.magnetMassesOJ().getRowDim(); i++) {
            if (hasModelBeamOJ.get(i, 0) > 0) {
                trimMagnetMasses[h] = peakShapeOutputDataRecord.magnetMassesOJ().toRawCopy2D()[i];

                h++;
            }
        }

        double[][] trimPeakIntensity = new double[newDataSet][peakShapeOutputDataRecord.magnetMassesOJ().getRowDim()];
        int k = 0;
        for (int i = 0; i < peakShapeOutputDataRecord.measuredPeakIntensitiesOJ().getRowDim(); i++) {
            if (hasModelBeamOJ.get(i, 0) > 0) {
                trimPeakIntensity[k] = peakShapeOutputDataRecord.measuredPeakIntensitiesOJ().toRawCopy2D()[i];
                k++;
            }
        }

        Matrix magnetMasses = new Matrix(trimMagnetMasses);
        Primitive64Store magnetMassesOJ = storeFactory.rows(trimMagnetMasses);
        Matrix measuredPeakIntensities = new Matrix(trimPeakIntensity);
        Primitive64Store measuredPeakIntensitiesOJ = storeFactory.rows(trimPeakIntensity);

        double[] massData = magnetMassesOJ.transpose().toRawCopy2D()[0];
        double[] intensityData = measuredPeakIntensitiesOJ.transpose().toRawCopy2D()[0];


        // WLS and NNLS
        Matrix GB = TrimGMatrix.times(Basis);
        MatrixStore<Double> GBOJ = trimGMatrixOJ.multiply(BasisOJ);
        Matrix WData = MatLab.diag(MatLab.rDivide(MatLab.max(measuredPeakIntensities, 1), 1));
        MatrixStore<Double> wData =  MatLab.diag(MatLab.rDivide(MatLab.max(measuredPeakIntensitiesOJ, 1), 1));

        Cholesky<Double> decompChol = Cholesky.PRIMITIVE.make();
        decompChol.decompose(wData);
        Matrix test1 = new Matrix(WData.chol().getL().getArray()).times(GB);
        MatrixStore<Double> test1OJ = decompChol.getL().multiply(GBOJ);
        Matrix test2 = new Matrix(WData.chol().getL().getArray()).times(measuredPeakIntensities);
        MatrixStore<Double> test2OJ = decompChol.getL().multiply(measuredPeakIntensitiesOJ);
        Matrix BeamWNNLS = MatLab.solveNNLS(test1, test2);
        MatrixStore<Double> beamWNNLS = MatLab.solveNNLS(test1OJ, test2OJ);

        // Determine peak width
        Matrix beamShape = Basis.times(BeamWNNLS);
        MatrixStore<Double> beamShapeOJ = BasisOJ.multiply(beamWNNLS);
        maxBeam = beamShape.normInf();
        double MaxBeam = MatLab.normInf(beamShapeOJ);
        maxBeamIndex = 0;
        int index = 0;
        for (int i = 0; i < beamShapeOJ.getRowDim(); i++) {
            for (int l = 0; l < beamShapeOJ.getColDim(); l++) {
                if (beamShapeOJ.get(i, l) == MaxBeam) {
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