package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import com.google.common.primitives.Doubles;
import jama.Matrix;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static org.apache.commons.math3.stat.StatUtils.mean;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.MathUtilities.*;

public class TestDriver {

    public static void demo() {
        // TODO: https://stackoverflow.com/questions/1881172/matlab-matrix-functions-in-java

        double[] dataInt = extractDoubleData("data-int.txt");
        double[] dataDet = extractDoubleData("data-det.txt");
        double[] dataIso = extractDoubleData("data-iso.txt");
        boolean[] dataIsOP = extractBooleanData("data-isOP.txt");

        MCMC2DataRecord mcmc2DataRecord = new MCMC2DataRecord(
                null, null, null, null, dataInt, dataIsOP, dataDet, dataIso, null, null);

        boolean[] isIsotopeA = filterDataByValue(dataIso, 1);
        boolean[] isIsotopeB = filterDataByValue(dataIso, 2);

        double rough_lograb = mean(logVector(leftDivideVectors(filterDataByFlags(dataInt, isIsotopeA), filterDataByFlags(dataInt, isIsotopeB))));
        double rough_logCb = max(1.0, mean((logVector(filterDataByFlags(dataInt, isIsotopeB)))));

        boolean[] inBL_det1 = logicalAnd(invertSelector(dataIsOP), filterDataByValue(dataDet, 1));
        boolean[] inBL_det2 = logicalAnd(invertSelector(dataIsOP), filterDataByValue(dataDet, 2));

        double rough_ref1 = mean(filterDataByFlags(dataInt, inBL_det1));
        double rough_ref2 = mean(filterDataByFlags(dataInt, inBL_det2));

        /*
            mRough = [rough.lograb; rough.logCb; rough.ref1; rough.ref2];
            functionToMinimize = @(m) -loglikLeastSquares(m, data, setup);
            opts = optimoptions('fminunc', 'Display', 'off');
            modelInitial = fminunc(@(m) functionToMinimize(m), mRough, opts);
            %llInitial = -negLogLik;
            dvarCurrent = updateDataVariance(modelInitial, setup);
            %dhatCurrent = evaluateModel(modelInitial, setup);
         */

        MCMC2ModelRecord mRough = new MCMC2ModelRecord(rough_lograb, rough_logCb, rough_ref1,rough_ref2);
        MCMC2SetupRecord setup = new MCMC2SetupRecord(
                (int)1e2, (int)1e2,
                new Detector(Detector.DetectorTypeEnum.FARADAY, "F1", 0, Detector.AmplifierTypeEnum.RESISTANCE, 1e11, 1, 0, 0),
                new double[(int)1e2], new double[(int)1e2]).initializeIntegrationTimes(1,1);

        // https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/optimization/direct/PowellOptimizer.html
        // https://github.com/imagej/imagej-legacy/blob/master/src/main/resources/script_templates/ImageJ_1.x/Examples/Optimization_Example.java
//        PowellOptimizer powellOptimizer = new PowellOptimizer(0,0);
// TODO: implement fminunc
        // for now:
        MCMC2ModelRecord maxlikModel = new MCMC2ModelRecord(rough_lograb, rough_logCb, rough_ref1,rough_ref2);
        double[] maxlikDVar = updateDataVariance(maxlikModel, setup);
        double[][] matrixG = makeG(maxlikModel, mcmc2DataRecord);

        /*maxlik.CM = inv(G'*diag(1./maxlik.dvar)*G);*/
        double[] maxlikDVarInverted = rightScalarVectorDivision(1, maxlikDVar);
        double[][] maxlikDVarInvertedDiagonal = new double[maxlikDVarInverted.length][maxlikDVarInverted.length];
        for (int i = 0; i < maxlikDVarInverted.length; i ++){
            maxlikDVarInvertedDiagonal[i][i] = 1.0 / maxlikDVarInverted[i];
        }
        Matrix maxlikDVarInvertedDiagonalM = new Matrix(maxlikDVarInvertedDiagonal);
        Matrix matrixGM = new Matrix(matrixG);
        Matrix maxlikCMM = matrixGM.transpose().times(maxlikDVarInvertedDiagonalM).times(matrixGM).inverse();
        double[][] maxLikeCM = maxlikCMM.getArray();

        System.out.println("END OF DEMO");
    }


    private static boolean[] logicalAnd(boolean[] arrayA, boolean[] arrayB){
        boolean[] logicalAnd = new boolean[arrayA.length];
        for (int i = 0; i < arrayA.length; i ++){
            logicalAnd[i] = arrayA[i] & arrayB[i];
        }
        return logicalAnd;
    }

    private static double[] extractDoubleData(String fileName) {
        List<String> contentsByLine = extractFileContentsByLine(fileName);
        String[] contentsByLineArray = contentsByLine.toArray(new String[0]);
        double[] contentsAsDoubles = Arrays.stream(contentsByLineArray)
                .mapToDouble(Double::parseDouble)
                .toArray();
        return contentsAsDoubles;
    }

    private static boolean[] extractBooleanData(String fileName) {
        List<String> contentsByLine = extractFileContentsByLine(fileName);
        boolean[] contentsAsBooleans = new boolean[contentsByLine.size()];
        String[] contentsByLineArray = contentsByLine.toArray(new String[0]);
        for (int i = 0; i < contentsAsBooleans.length; i ++){
            contentsAsBooleans[i] = (Integer.parseInt(contentsByLineArray[i]) == 1)? true : false;
        }
        return contentsAsBooleans;
    }

    private static List<String> extractFileContentsByLine(String fileName){
        ResourceExtractor RESOURCE_EXTRACTOR
                = new ResourceExtractor(Tripoli.class);
        List<String> contentsByLine = new ArrayList<>();
        try {
            Path dataPath = RESOURCE_EXTRACTOR
                    .extractResourceAsFile("/org/cirdles/tripoli/dataSourceProcessors/dataSources/syntheticData/SyntheticOutToTripoli/" + fileName).toPath();
            contentsByLine.addAll(Files.readAllLines(dataPath, Charset.defaultCharset()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentsByLine;
    }

    static boolean[] filterDataByValue(double[] source, double value) {
        boolean[] extracted = new boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            extracted[i] = (source[i] == value);
        }
        return extracted;
    }

    private static double[] filterDataByFlags(double[] source, boolean[] selector) {
        List<Double> retrievedValuesList = new ArrayList<>();
        for (int i = 0; i < source.length; i++) {
            if (selector[i]) {
                retrievedValuesList.add(source[i]);
            }
        }
        return Doubles.toArray(retrievedValuesList);
    }



    private static boolean[] invertSelector(boolean[] selector){
        boolean[] invertedSelector = new boolean[selector.length];
        for (int i = 0; i < selector.length; i++) {
            invertedSelector[i] = !selector[i];
        }
        return invertedSelector;
    }

}
