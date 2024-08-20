package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import com.google.common.primitives.Doubles;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.DataUtilities.*;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.MathUtilities.extractColumn;

public enum TestDriver {
    ;

    public static void demo() {
        // TODO: https://stackoverflow.com/questions/1881172/matlab-matrix-functions-in-java

        MCMC2DataRecord mcmc2DataRecord = syntheticData();

        MCMC2SetupRecord setup = new MCMC2SetupRecord(
                (int) 1.0e2, (int) 1.0e2,
                new Detector(Detector.DetectorTypeEnum.FARADAY, "F1", 0, Detector.AmplifierTypeEnum.RESISTANCE, 1.0e11, 1, 0, 0),
                new double[(int) 1.0e2],
                new double[(int) 1.0e2], null, (int) 2.0e4, 20, 4/*todo: use truth count*/, 8, 10)
                .initializeIntegrationTimes(1, 1);

        MaxLikelihoodRecord maxLikelihoodRecord = maxLikelihood(mcmc2DataRecord, setup);
        setup = setup.updateRecord(maxLikelihoodRecord.covarianceMatrix());

        MCMC2ChainRecord mcmc2ChainRecord = initializeChains(setup, mcmc2DataRecord, maxLikelihoodRecord);
        double[][] initModels = mcmc2ChainRecord.initModels();
        double[] initLogLiks = mcmc2ChainRecord.initLogLiks();

        int nSavedModels = setup.MCMCTrialsCount() / setup.seive();
        double[][][] modelChains = new double[setup.modelParameterCount()][nSavedModels][setup.chainsCount()];
        for (int i = 0; i < setup.modelParameterCount(); i++) {
            for (int j = 0; j < nSavedModels; j++) {
                Arrays.fill(modelChains[i][j], Double.NaN);
            }
        }
        double[][] loglikChains = new double[nSavedModels][setup.chainsCount()];
        for (int j = 0; j < nSavedModels; j++) {
            Arrays.fill(loglikChains[j], Double.NaN);
        }

//        parfor iChain = 1:setup.nChains
        double[][] outputModels = null;
        double[] outputLogLiks = null;

        for (int iChain = 0; iChain < setup.chainsCount(); iChain++) {
            MetropolisHastingsRecord metropolisHastingsRecord = MetropolisHastings(
                    iChain,
                    extractColumn(initModels, iChain),
                    initLogLiks[iChain],
                    mcmc2DataRecord,
                    setup);

            outputModels = metropolisHastingsRecord.outputModels();
            outputLogLiks = metropolisHastingsRecord.outputLogLiks();

            for (int i = 0; i < outputModels.length; i++) {
                for (int j = 0; j < outputModels[i].length; j++) {
                    modelChains[i][j][iChain] = outputModels[i][j];
                    loglikChains[j][iChain] = outputLogLiks[j];
                }
            }
        }


        System.out.println("END OF DEMO " + outputModels.length + outputLogLiks.length);
    }


    static boolean[] logicalAnd(boolean[] arrayA, boolean[] arrayB) {
        boolean[] logicalAnd = new boolean[arrayA.length];
        for (int i = 0; i < arrayA.length; i++) {
            logicalAnd[i] = arrayA[i] & arrayB[i];
        }
        return logicalAnd;
    }

    static double[] extractDoubleData(String fileName) {
        List<String> contentsByLine = extractFileContentsByLine(fileName);
        String[] contentsByLineArray = contentsByLine.toArray(new String[0]);
        double[] contentsAsDoubles = Arrays.stream(contentsByLineArray)
                .mapToDouble(Double::parseDouble)
                .toArray();
        return contentsAsDoubles;
    }

    static boolean[] extractBooleanData(String fileName) {
        List<String> contentsByLine = extractFileContentsByLine(fileName);
        boolean[] contentsAsBooleans = new boolean[contentsByLine.size()];
        String[] contentsByLineArray = contentsByLine.toArray(new String[0]);
        for (int i = 0; i < contentsAsBooleans.length; i++) {
            contentsAsBooleans[i] = 1 == Integer.parseInt(contentsByLineArray[i]);
        }
        return contentsAsBooleans;
    }

    private static List<String> extractFileContentsByLine(String fileName) {
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

    static double[] filterDataByFlags(double[] source, boolean[] selector) {
        List<Double> retrievedValuesList = new ArrayList<>();
        for (int i = 0; i < source.length; i++) {
            if (selector[i]) {
                retrievedValuesList.add(source[i]);
            }
        }
        return Doubles.toArray(retrievedValuesList);
    }


    static boolean[] invertSelector(boolean[] selector) {
        boolean[] invertedSelector = new boolean[selector.length];
        for (int i = 0; i < selector.length; i++) {
            invertedSelector[i] = !selector[i];
        }
        return invertedSelector;
    }


}
