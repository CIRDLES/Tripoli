package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import com.google.common.primitives.Doubles;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static org.apache.commons.math3.stat.StatUtils.mean;

public class TestDriver {

    public static void demo() {
        // TODO: https://stackoverflow.com/questions/1881172/matlab-matrix-functions-in-java

        double[] dataInt = extractDoubleData("data-int.txt");
        double[] dataDet = extractDoubleData("data-det.txt");
        double[] dataIso = extractDoubleData("data-iso.txt");
        boolean[] dataIsOP = extractBooleanData("data-isOP.txt");

        boolean[] isIsotopeA = filterDataByValue(dataIso, 1);
        boolean[] isIsotopeB = filterDataByValue(dataIso, 2);

        double rough_lograb = mean(logVector(leftDivideVectors(filterDataByFlags(dataInt, isIsotopeA), filterDataByFlags(dataInt, isIsotopeB))));
        double rough_logCb = max(1.0, mean((logVector(filterDataByFlags(dataInt, isIsotopeB)))));

        boolean[] inBL_det1 = logicalAnd(invertSelector(dataIsOP), filterDataByValue(dataDet, 1));
        boolean[] inBL_det2 = logicalAnd(invertSelector(dataIsOP), filterDataByValue(dataDet, 2));

        double rough_ref1 = mean(filterDataByFlags(dataInt, inBL_det1));
        double rough_ref2 = mean(filterDataByFlags(dataInt, inBL_det2));

        /*
         * m0 = [rough.lograb; rough.logCb; rough.ref1; rough.ref2];
         * [modelCurrent, negLogLik] = fminunc(@(m) -loglikLeastSquares(m, data, setup), m0);
         *
         *
         */

        MCMC2ModelRecord mRough = new MCMC2ModelRecord(rough_lograb, rough_logCb, rough_ref1,rough_ref2);
        MCMC2SetupRecord setup = new MCMC2SetupRecord(
                (int)1e2, (int)1e2, "F", 1e11, 1, new double[(int)1e2], new double[(int)1e2]).initializeIntegrationTimes(1,1);

        // https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/optimization/direct/PowellOptimizer.html
        // https://github.com/imagej/imagej-legacy/blob/master/src/main/resources/script_templates/ImageJ_1.x/Examples/Optimization_Example.java
        
        PowellOptimizer powellOptimizer = new PowellOptimizer(0,0);


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

    private static boolean[] filterDataByValue(double[] source, double value) {
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

    private static double[] leftDivideVectors(double[] vectorA, double[] vectorB) {
        // precondition a and b same length
        double[] divided = new double[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            divided[i] = vectorA[i] / ((vectorB[i] != 0) ? vectorB[i] : 1.0);
        }
        return divided;
    }

    private static double[] logVector(double[] source) {
        double[] logVector = new double[source.length];
        for (int i = 0; i < source.length; i++) {
            logVector[i] = StrictMath.log(source[i]);
        }
        return logVector;
    }

    private static boolean[] invertSelector(boolean[] selector){
        boolean[] invertedSelector = new boolean[selector.length];
        for (int i = 0; i < selector.length; i++) {
            invertedSelector[i] = !selector[i];
        }
        return invertedSelector;
    }

}
