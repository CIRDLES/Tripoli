package org.cirdles.tripoli.gui.utilities.fileUtilities;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.file.SessionFileUtilities;

import java.io.File;
import java.io.IOException;

public class FileHandlerUtil {

    public static File saveSessionFile(Session session, Window ownerWindow)
            throws IOException {

        File retVal = null;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Session '.tripoli' file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Tripoli Session files", "*.tripoli"));
//        File initDirectory = new File(squidPersistentState.getMRUProjectFolderPath());
//        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);
        fileChooser.setInitialFileName(session.getSessionName() + ".tripoli");

        File sessionFileNew = fileChooser.showSaveDialog(ownerWindow);

        if (sessionFileNew != null) {
            Session.setSessionChanged(false);
            retVal = sessionFileNew;
            // capture tripoli session file name from file for session itself
            session.setSessionName(sessionFileNew.getName().substring(0, sessionFileNew.getName().lastIndexOf(".")));
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
//        File initDirectory = new File(squidPersistentState.getMRUProjectFolderPath());
//        fileChooser.setInitialDirectory(initDirectory.exists() ? initDirectory : null);

        File sessionFileNew = fileChooser.showOpenDialog(ownerWindow);

        if (sessionFileNew != null) {
            retVal = sessionFileNew.getCanonicalPath();
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
}