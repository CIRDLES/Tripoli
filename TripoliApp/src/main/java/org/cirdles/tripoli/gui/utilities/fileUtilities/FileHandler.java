package org.cirdles.tripoli.gui.utilities.fileUtilities;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class FileHandler {

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
