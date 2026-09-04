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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix;

import org.apache.commons.lang3.time.DateUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.utilities.comparators.LiveDataEntryComparator;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static org.cirdles.tripoli.constants.TripoliConstants.R18O_16O_DEFAULT_OXIDE_CORRECTION;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod.createAnalysisMethodFromCase1;


public class PhoenixLiveData implements Serializable {
    @Serial
    private static final long serialVersionUID = -8981972960059300836L;
    private static final Pattern FILE_PATTERN = Pattern.compile(".*-B(\\d+)-C(\\d+)\\.TXT", Pattern.CASE_INSENSITIVE);
    AnalysisInterface liveDataAnalysis;
    boolean initMetaData = true;
    MassSpecOutputBlockRecordLite blockRecordLite;
    MassSpecExtractedData massSpecExtractedData;
    double[][] cycleData;
    int numOfFunctions = 0;
    int cycleIndex = 0;
    int blockIndex = 0;
    String analysisNumber;
    int cyclesPerBlock = 0;
    int r270_267ColumnIndex = -1;
    int r265_267ColumnIndex = -1;
    private transient TreeSet<Path> pendingFiles = new TreeSet<>(LiveDataEntryComparator.blockCycleComparator);
    private int lastProcessedBlock = -1;
    private int lastProcessedCycle = 0;

    /**
     * Contains all the logic for operating on live data files output by Phoenix mass spectrometer.
     *
     * @throws TripoliException Thrown by analysis initialization
     */
    public PhoenixLiveData() throws TripoliException {
        liveDataAnalysis = AnalysisInterface.initializeNewAnalysis(0);
        massSpecExtractedData = new MassSpecExtractedData();
        massSpecExtractedData.setColumnHeaders(new String[]{"Cycle", "Time"});
        MassSpectrometerContextEnum massSpectrometerContext = liveDataAnalysis.getParameters().getMassSpectrometerContext();
        massSpecExtractedData.setMassSpectrometerContext(massSpectrometerContext);
        liveDataAnalysis.setMassSpecExtractedData(massSpecExtractedData);
    }

    /**
     * Checks massSpecDataFolder and its parent for the existence of LiveDataStatus.txt, retrieves the active livedata location
     * from the txt and returns the path of it.
     *
     * @param massSpecDataFolder user/mru supplied folder file
     * @return Path of the active LiveData folder
     */
    public static Path getLiveDataFolderPath(File massSpecDataFolder) {
        File liveDataStatusFile = new File(massSpecDataFolder, "LiveDataStatus.txt");
        File parentLiveDataStatusFile = new File(massSpecDataFolder.getParentFile(), "LiveDataStatus.txt");

        File mutatableMethodFolder = massSpecDataFolder;
        if (!liveDataStatusFile.exists() && !parentLiveDataStatusFile.exists()) {
            return null;
        }

        // Prefer massSpecDataFolder, fallback to parent
        if (!liveDataStatusFile.exists()) {
            liveDataStatusFile = parentLiveDataStatusFile;
            mutatableMethodFolder = massSpecDataFolder.getParentFile();
        }

        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(liveDataStatusFile));

            do {
                line = bufferedReader.readLine();
            } while (null !=line && !Objects.equals(line.split(",")[0], "Method"));
        } catch (IOException ignored) {
        }

        assert line != null;
        String[] methodParts = line.split("\\\\");
        String methodName = methodParts[methodParts.length - 2].replace("\"", "");

        return Path.of(mutatableMethodFolder + File.separator + methodName + File.separator + "LiveData");
    }

    public static Path findLiveDataFolderPath(Path liveDataStatusTxtFile) {
        Path mutatableMassSpecDataFolder = liveDataStatusTxtFile.getParent();

        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(liveDataStatusTxtFile.toFile()));
            do {
                line = bufferedReader.readLine();
            } while (!Objects.equals(line.split(",")[0], "Method"));
        } catch (IOException ignored) {
        }

        String[] methodParts = line.split("\\\\");
        String sampleFolder = methodParts[methodParts.length - 2].replace("\"", "");

        return Path.of(mutatableMassSpecDataFolder + File.separator + sampleFolder + File.separator + "LiveData");
    }

    public static File getFinishedFile(File methodFolder) {
        File liveDataStatusFile = new File(methodFolder, "LiveDataStatus.txt");

        if (!liveDataStatusFile.exists()) {
            return new File("");
        }

        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(liveDataStatusFile));

            do {
                line = bufferedReader.readLine();
            } while (!Objects.equals(line.split(",")[0], "Method"));
        } catch (IOException ignored) {
        }

        String[] methodParts = line.split("\\\\");
        String methodName = methodParts[methodParts.length - 2];
        String analysisName = methodParts[methodParts.length - 1].replace("\"", "");

        return new File(methodFolder + File.separator + methodName + File.separator + analysisName + ".TIMSDP");
    }

    private File getAnalysisTxtFile() {
        Path liveDataPath = Path.of(liveDataAnalysis.getDataFilePathString()).getParent();
        String analysisName = liveDataAnalysis.getAnalysisSampleName() + " " + liveDataAnalysis.getAnalysisFractionName();
        analysisName = analysisName + "-" + analysisNumber;
        if (new File(liveDataPath.resolve(analysisName + ".txt").toString()).exists()) {
            return liveDataPath.resolve(analysisName + ".txt").toFile();
        }
        return null;
    }

    public AnalysisInterface getLiveDataAnalysis() {
        return liveDataAnalysis;
    }

    public void setLiveDataAnalysis(AnalysisInterface liveDataAnalysis) {
        this.liveDataAnalysis = liveDataAnalysis;
    }

    private int[] extractBlockCycle(Path path) {
        Matcher m = FILE_PATTERN.matcher(path.getFileName().toString());
        if (m.matches()) {
            return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        }
        return null;
    }

    private boolean isNextExpected(int block, int cycle) {
        if (lastProcessedBlock == -1) return cycle == 1;
        if (block == lastProcessedBlock) return cycle == lastProcessedCycle + 1;
        if (block > lastProcessedBlock) return cycle == 1;
        return false;
    }

    public AnalysisInterface readLiveDataFile(Path filePath) {
        if (pendingFiles == null) {
            pendingFiles = new TreeSet<>(LiveDataEntryComparator.blockCycleComparator);
        }
        pendingFiles.add(filePath);
        AnalysisInterface result = null;
        while (!pendingFiles.isEmpty()) {
            Path next = pendingFiles.first();
            int[] blockCycle = extractBlockCycle(next);
            if (blockCycle == null || !isNextExpected(blockCycle[0], blockCycle[1])) {
                break;
            }
            pendingFiles.remove(next);
            result = processFile(next);
            if (result != null) {
                lastProcessedBlock = blockCycle[0];
                lastProcessedCycle = blockCycle[1];
            }
        }
        return result;
    }

    private AnalysisInterface processFile(Path filePath) {
        File liveDataFile = filePath.toFile();
        analysisNumber = liveDataFile.getName().split("-")[0];

        if (liveDataFile.exists() && liveDataFile.isFile()) {
            try {
                String[] lines = Files.readAllLines(filePath).toArray(new String[0]);
                for (String line : lines) {
                    readLiveDataLine(line);
                }
                if (initMetaData) {
                    setAnalysisHeader();
                    liveDataAnalysis.setDataFilePathString(filePath.getParent().toString());

                    // Import AnalysisMethod user function changes
                    liveDataAnalysis.setMethod(createAnalysisMethodFromCase1(massSpecExtractedData));
                    List<UserFunction> userFunctionModel = liveDataAnalysis.getMethod().getUserFunctionsModel();
                    for (UserFunction modelFunc : userFunctionModel) {
                        // Set isotopic ratios
                        if (modelFunc.isTreatAsIsotopicRatio()) {
                            liveDataAnalysis.getUserFunctions().stream()
                                    .filter(func -> func.getName().equals(modelFunc.getName()))
                                    .forEach(func -> func.setTreatAsIsotopicRatio(true));
                        }

                        // Add oxide corrected user functions to analysis
                        if (modelFunc.isOxideCorrected()) {
                            liveDataAnalysis.getUserFunctions().add(modelFunc);
                        }

                        // Get correction indices
                        if (modelFunc.getName().equals("270/267")) {
                            r270_267ColumnIndex = modelFunc.getColumnIndex();
                        }
                        if (modelFunc.getName().equals("265/267")) {
                            r265_267ColumnIndex = modelFunc.getColumnIndex();
                        }

                        liveDataAnalysis.getUserFunctions().stream()
                                .filter(func -> func.getName().equals(modelFunc.getName()))
                                .forEach(func -> func.setEtReduxName(modelFunc.getEtReduxName()));
                        liveDataAnalysis.getUserFunctions().stream()
                                .filter(func -> func.getName().equals(modelFunc.getName()))
                                .forEach(func -> func.setInvertedETReduxName(modelFunc.getInvertedETReduxName()));

                    }
                    initMetaData = false;
                }

                blockRecordLite = new MassSpecOutputBlockRecordLite(blockIndex, cycleData);
                if (r270_267ColumnIndex != -1 && r265_267ColumnIndex != -1) {
                    blockRecordLite = blockRecordLite.expandForUraniumOxideCorrection(r270_267ColumnIndex, r265_267ColumnIndex,
                            liveDataAnalysis.getParameters().getR18O_16O_OxideCorrection());
                }
                massSpecExtractedData.addBlockLiteRecord(blockRecordLite);

                // Add data to UF map (For use in plots)
                for (UserFunction userFunction : liveDataAnalysis.getUserFunctions()) {
                    SingleBlockRawDataLiteSetRecord singleBlockRawDataLiteSetRecord = AllBlockInitForDataLiteOne.prepareSingleBlockDataLiteCaseOne(
                            blockIndex,
                            massSpecExtractedData
                    );

                    boolean[] cyclesIncluded = singleBlockRawDataLiteSetRecord.assembleCyclesIncludedForUserFunction(userFunction);

                    // Preserve existing rejection state when refreshing live data
                    PlotBlockCyclesRecord existingRecord = userFunction.getMapBlockIdToBlockCyclesRecord().get(blockIndex);
                    if (existingRecord != null) {
                        boolean[] existingCyclesIncluded = existingRecord.cyclesIncluded();
                        int copyLen = Math.min(existingCyclesIncluded.length, cyclesIncluded.length);
                        System.arraycopy(existingCyclesIncluded, 0, cyclesIncluded, 0, copyLen);
                    }
                    // Recompute blockIncluded: block is included if any cycle is included
                    boolean blockIncluded = false;
                    for (boolean included : cyclesIncluded) {
                        if (included) {
                            blockIncluded = true;
                            break;
                        }
                    }

                    userFunction.getMapBlockIdToBlockCyclesRecord().put(blockIndex, BlockCyclesBuilder.initializeBlockCycles(
                            blockIndex,
                            blockIncluded,
                            true,
                            cyclesIncluded,
                            singleBlockRawDataLiteSetRecord.assembleCycleMeansForUserFunction(userFunction),
                            singleBlockRawDataLiteSetRecord.assembleCycleStdDevForUserFunction(),
                            new String[]{userFunction.getName()},
                            true,
                            userFunction.isTreatAsIsotopicRatio()).getBlockCyclesRecord()
                    );
                }

                return liveDataAnalysis;

            } catch (IOException e) {
                System.out.println("Error reading LiveData file: " + e.getMessage());
            } catch (TripoliException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private void readLiveDataLine(String dataLine) {
        String[] dataLineSplit = dataLine.split(",");

        switch (dataLineSplit[0]) {
            case "Version":
            case "Serial No":
            case "Sample No":
            case "Beam Interp":
            case "Baseline Corrected":
                break;
            case "Method":
                if (initMetaData) {
                    String analysisName = dataLineSplit[1].substring(dataLineSplit[1].lastIndexOf("\\") + 1, dataLineSplit[1].length() - 1);
                    liveDataAnalysis.setAnalysisName(analysisName + (" (Live Data)"));
                    liveDataAnalysis.setAnalysisSampleName(analysisName.split(" ")[0]);
                    liveDataAnalysis.setAnalysisFractionName(analysisName.split(" ")[1].split("-")[0]);
                }
                break;
            case "Acquire Date":
                if (initMetaData) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date;
                    try {
                        date = DateUtils.parseDate(dataLineSplit[1].trim(),
                                "dd/MM/yyyy HH:mm:ss",
                                "yyyy-MM-dd hh:mm:ss",
                                "yyyy-MM-dd h:mm:ss a",
                                "dd/MM-yyyy",
                                "E d MMMM yyyy hh:mm:ss",
                                "MM/dd/yyyy hh:mm:ss",
                                "MM/dd/yyyy h:mm:ss a",
                                "dd.MM.yyyy",
                                "dd.MM.yyyy hh:mm:ss",
                                "MM/dd/yyyy",
                                "yyyy-MM-dd",
                                "y/m/d");
                        liveDataAnalysis.setAnalysisStartTime(df.format(date));

                    } catch (ParseException ignored) {
                        liveDataAnalysis.setAnalysisStartTime("Unknown");
                    }
                }
                break;
            case "Functions":
                numOfFunctions = Integer.parseInt(dataLineSplit[1]);
                break;
            case "Cycle":
                cycleIndex = Integer.parseInt(dataLineSplit[1]);

                if (initMetaData) {
                    UserFunction userFunction = new UserFunction("Cycle", 0);
                    liveDataAnalysis.getUserFunctions().add(userFunction);
                    userFunction = new UserFunction("Time", 1);
                    liveDataAnalysis.getUserFunctions().add(userFunction);
                }

                if (cycleData == null || cycleData.length > cycleIndex) {
                    // Starting a new block, set the CPB and redo the header
                    if (cycleData != null && cyclesPerBlock == 0) {
                        cyclesPerBlock = cycleData.length;
                        setAnalysisHeader();
                    }
                    cycleData = new double[cycleIndex][numOfFunctions + 2];
                } else { // Copy old data to new array
                    double[][] expandedCycleData = new double[cycleIndex][numOfFunctions + 2];
                    for (int row = 0; row < cycleData.length; row++) {
                        System.arraycopy(cycleData[row], 0, expandedCycleData[row], 0, cycleData[row].length);
                    }
                    cycleData = expandedCycleData;

                }
                break;
            case "Block":
                blockIndex = Integer.parseInt(dataLineSplit[1]);
                break;
            default:
                try {
                    int columnIndex = Integer.parseInt(dataLineSplit[0]) + 1;
                    String userFunctionName = dataLineSplit[1].substring(1, dataLineSplit[1].length() - 1);
                    double userFunctionValue = Double.parseDouble(dataLineSplit[2]);
                    if (initMetaData) {
                        UserFunction userFunction = new UserFunction(userFunctionName, columnIndex);
                        liveDataAnalysis.getUserFunctions().add(userFunction);
                        String[] headersExpanded = Arrays.copyOf(massSpecExtractedData.getColumnHeaders(),
                                massSpecExtractedData.getColumnHeaders().length + 1);
                        headersExpanded[columnIndex] = userFunctionName;
                        massSpecExtractedData.setColumnHeaders(headersExpanded);
                    }
                    cycleData[cycleIndex - 1][columnIndex] = userFunctionValue;
                } catch (Exception ignore) {
                }
                cycleData[cycleIndex - 1][0] = cycleIndex;//Cycle
                cycleData[cycleIndex - 1][1] = cycleIndex;//Time - not present in file
        }
    }

    private List<String> readTxtHeaderFromFile(File analysisTxtFile) {
        if (analysisTxtFile != null) {
            try {
                BufferedReader br = Files.newBufferedReader(analysisTxtFile.toPath());
                List<String> headerData = new ArrayList<>();

                String line;
                while (!Objects.equals(line = br.readLine(), "")) {
                    if (line.contains(",")) {
                        headerData.add(line.split(",")[1]);
                    }
                }
                if (headerData.get(2).contains(".")) { // Strip off file extension
                    headerData.set(2, headerData.get(2).substring(0, headerData.get(2).lastIndexOf(".")));
                }
                return headerData;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void setAnalysisHeader() {
        MassSpecExtractedData.MassSpecExtractedHeader header;

        File analysisTxtFile = getAnalysisTxtFile();
        List<String> headerData = readTxtHeaderFromFile(analysisTxtFile);

        if (analysisTxtFile != null) {
            String fileName = headerData.get(1).split("\\.")[0];
            header = new MassSpecExtractedData.MassSpecExtractedHeader(
                    headerData.get(0),
                    fileName,
                    fileName.substring(0, fileName.lastIndexOf("-")),
                    headerData.get(2),
                    Boolean.parseBoolean(headerData.get(6)),
                    Boolean.parseBoolean(headerData.get(7)),
                    headerData.get(8).trim(),
                    cyclesPerBlock
            );
        } else {
            header = new MassSpecExtractedData.MassSpecExtractedHeader(
                    "Phoenix",
                    "LiveData",
                    "",
                    "Phoenix_Live_Data_Processing",
                    false,
                    false,
                    "",
                    cyclesPerBlock
            );
        }

        massSpecExtractedData.setHeader(header);
    }

    /**
     * Merges changes from LiveData analysis to finished analysis. Copies BlockIdToRawDataLiteOne included data,
     * and replaces UserFunctions in FinishedAnalysis with those from LiveDataAnalysis.
     *
     * @param finishedAnalysis analysis generated from finished timsdp file
     */
    public void mergeFinalFile(AnalysisInterface finishedAnalysis) {
        liveDataAnalysis.getMapOfBlockIdToRawDataLiteOne().forEach((blockID, blockRawData) -> {
            boolean[][] liveArray = blockRawData.blockRawDataLiteIncludedArray();
            boolean[][] finishedArray = finishedAnalysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).blockRawDataLiteIncludedArray();
            for (int row = 0; row < liveArray.length; row++) {
                System.arraycopy(liveArray[row], 0, finishedArray[row], 0, liveArray[row].length);
            }
        });

        List<UserFunction> liveUFs = liveDataAnalysis.getUserFunctions();
        List<UserFunction> finishedUFs = finishedAnalysis.getUserFunctions();
        finishedUFs.replaceAll(finishedUF ->
                liveUFs.stream()
                        .filter(liveUF -> liveUF.getName().equals(finishedUF.getName()))
                        .findFirst()
                        .orElse(finishedUF)
        );

    }
}
