package org.cirdles.tripoli.gui.utilities.fileUtilities;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.EnsemblesStore;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.file.SessionFileUtilities;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.cirdles.tripoli.gui.TripoliGUIController.tripoliPersistentState;
import static org.cirdles.tripoli.utilities.file.FileNameFixer.fixFileName;

public enum FileHandlerUtil {
    ;

    public static File saveSessionFile(Session session, Window ownerWindow)
            throws IOException {

        File retVal = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Session '.tripoli' file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Tripoli Session files", "*.tripoli"));
        File initDirectory = new File(tripoliPersistentState.getMRUSessionFolderPath());
        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);
        fileChooser.setInitialFileName(fixFileName(session.getSessionName()) + ".tripoli");

        File sessionFileNew = fileChooser.showSaveDialog(ownerWindow);

        if (null != sessionFileNew) {
            Session.setSessionChanged(false);
            retVal = sessionFileNew;
            // capture tripoli session file name from file for session itself
            session.setSessionName(sessionFileNew.getName().substring(0, sessionFileNew.getName().lastIndexOf('.')));
            try {
                SessionFileUtilities.serializeTripoliSession(session, sessionFileNew.getCanonicalPath());
            } catch (IOException | TripoliException ex) {
                TripoliMessageDialog.showWarningDialog(ex.getMessage(), null);
            }
        }

        return retVal;
    }

    public static String selectSessionFile(Window ownerWindow)
            throws IOException {
        String retVal = "";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Session '.tripoli' file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Tripoli Session files", "*.tripoli"));
        File initDirectory = new File(tripoliPersistentState.getMRUSessionFolderPath());
        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);

        File sessionFileNew = fileChooser.showOpenDialog(ownerWindow);

        if (null != sessionFileNew) {
            retVal = sessionFileNew.getCanonicalPath();
            tripoliPersistentState.setMRUSessionFolderPath(sessionFileNew.getParent());
        }

        return retVal;
    }

    public static File selectDataFile(Window ownerWindow)
            throws TripoliException {
        File retVal = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Data text file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Data txt files", "*.txt"));
        File initDirectory = new File("");
        if (tripoliPersistentState.getMRUDataFileFolderPath() != null) {
            initDirectory = new File(tripoliPersistentState.getMRUDataFileFolderPath());
        }
        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);

        File dataFile = fileChooser.showOpenDialog(ownerWindow);

        if (null != dataFile) {
            if (dataFile.getName().toLowerCase(Locale.US).endsWith(".txt")) {
                retVal = dataFile;
                tripoliPersistentState.setMRUDataFile(dataFile);
                tripoliPersistentState.setMRUDataFileFolderPath(dataFile.getParent());
            } else {
                throw new TripoliException("Filename does not end with '.txt'");
            }
        }
        return retVal;
    }

    public static File selectMethodFile(Window ownerWindow)
            throws TripoliException, IOException {
        File retVal = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Analysis Method '.xml' file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Analysis Method '.xml' files", "*.txt,*.xml,*.TIMSAM"));
        File initDirectory = new File(tripoliPersistentState.getMRUMethodXMLFolderPath());
        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);

        File dataFile = fileChooser.showOpenDialog(ownerWindow);

        if (null != dataFile) {
            // <?xml version="1.0" standalone="yes"?>
            List<String> contentsByLine = new ArrayList<>(Files.readAllLines(Path.of(dataFile.toURI()), Charset.defaultCharset()));
            if (contentsByLine.get(0).startsWith("<?xml version=") && (contentsByLine.get(1).startsWith("<ANALYSIS_METHOD>"))) {
                retVal = dataFile;
                tripoliPersistentState.setMRUMethodXMLFolderPath(dataFile.getParent());
            } else {
                throw new TripoliException("File does not contain correct xml for a method specification.");
            }
        }
        return retVal;
    }

    public static File selectPeakShapeResourceFolderForBrowsing(Window ownerWindow) {
        File resourceFile;

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select PeakShapes Resource Folder");

        // Default folder location for now
        File defaultFolder = new File("TripoliResources");
        if (defaultFolder.isDirectory()) {
            chooser.setInitialDirectory(defaultFolder);
        } else {
            File userHome = new File("Tripoli");
            chooser.setInitialDirectory(userHome.isDirectory() ? userHome : null);
        }

        resourceFile = chooser.showDialog(ownerWindow);
        return resourceFile;
    }


    public static void saveEnsembleData(AnalysisInterface analysis, Window ownerWindow)
            throws IOException, TripoliException {

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Ensemble Reports Folder");
        File userHome = new File(File.separator + TripoliPersistentState.getExistingPersistentState().getMRUSessionFolderPath());
        dirChooser.setInitialDirectory(userHome.isDirectory() ? userHome : null);
        File directory = dirChooser.showDialog(ownerWindow);

        Map<Integer, List<EnsemblesStore.EnsembleRecord>> mapOfBlockIDtoEnsembles = analysis.getMapBlockIDToEnsembles();
        for (Integer blockID : mapOfBlockIDtoEnsembles.keySet()) {
            // Detroit 2023 printout ensembleRecordsList
            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList = mapOfBlockIDtoEnsembles.get(blockID);
            if (!ensembleRecordsList.isEmpty()) {
                Path path = Paths.get(directory + File.separator + "EnsemblesForBlock_" + blockID + ".csv");
                OutputStream stream = Files.newOutputStream(path);
                stream.write(ensembleRecordsList.get(0).prettyPrintHeaderAsCSV("Index", analysis.getAnalysisMethod().getIsotopicRatiosList()).getBytes());
                for (int i = 0; i < ensembleRecordsList.size(); i++) {
                    stream.write(ensembleRecordsList.get(i).prettyPrintAsCSV().getBytes());
                }

                stream.close();
            }
        }
    }

    public static void saveAnalysisReport(AnalysisInterface analysis, Window ownerWindow)
            throws IOException, TripoliException {

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Reports Folder");
        File userHome = new File(File.separator + TripoliPersistentState.getExistingPersistentState().getMRUSessionFolderPath());
        dirChooser.setInitialDirectory(userHome.isDirectory() ? userHome : null);
        File directory = dirChooser.showDialog(ownerWindow);

        Path path = Paths.get(directory + File.separator + "AnalysisReport" + ".csv");
        OutputStream stream = Files.newOutputStream(path);
        stream.write(((Analysis) analysis).produceReportTemplateOne().getBytes());
        stream.close();
    }
}