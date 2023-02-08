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
package org.cirdles.tripoli.gui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author James Bowring
 */
public class TripoliMessageDialog extends Alert {

    public TripoliMessageDialog() {
        super(null);
    }

    //http://stackoverflow.com/questions/26341152/controlsfx-dialogs-deprecated-for-what/32618003#32618003
    public TripoliMessageDialog(AlertType alertType, String message, String headerText, Window owner) {
        super(alertType);
        initOwner(owner);
        setTitle("Tripoli Alert");
        setContentText((message == null) ? "Unknown error ..." : message);
        setHeaderText(headerText);
        initStyle(StageStyle.DECORATED);
        int countOfNewLines = 1;
        if (message != null) {
            for (int i = 0; i < message.length(); i++) {
                countOfNewLines = countOfNewLines + ((message.charAt(i) == '\n') ? 1 : 0);
            }
        }
        getDialogPane().setPrefSize(750, 150 + countOfNewLines * 20);
        getDialogPane().setStyle(getDialogPane().getStyle() + ";-fx-font-family: SansSerif Bold;-fx-font-size: 15");
    }

    /**
     * @param message
     * @param owner
     */
    public static void showWarningDialog(String message, Window owner) {
        Alert alert = new TripoliMessageDialog(AlertType.WARNING, message, "Tripoli warns you:", owner);
        alert.showAndWait();
    }

    /**
     * @param message
     * @param owner
     */
    public static void showInfoDialog(String message, Window owner) {
        Alert alert = new TripoliMessageDialog(
                AlertType.INFORMATION,
                message,
                "Tripoli informs you:", owner);
        alert.showAndWait();
    }

    public static boolean showChoiceDialog(String message, Window owner) {
        Alert alert = new TripoliMessageDialog(
                Alert.AlertType.CONFIRMATION,
                message,
                "Tripoli informs you:", owner);
        alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.OK);
        Optional<ButtonType> result = alert.showAndWait();

        return (result.get() == ButtonType.OK);
    }

    public static void showSavedAsDialog(File file, Window owner) {
        if (file == null) {
            Alert dialog = new TripoliMessageDialog(AlertType.WARNING,
                    "Path is null!",
                    "Check permissions ...",
                    owner);
            dialog.showAndWait();
        } else {
            Alert dialog = new TripoliMessageDialog(AlertType.CONFIRMATION,
                    showLongfilePath(file.getAbsolutePath()),
                    (file.isDirectory() ? "Files saved in:" : "File saved as:"),
                    owner);
            ButtonType openButton = new ButtonType((file.isDirectory() ? "Open Directory" : "Open File"), ButtonBar.ButtonData.APPLY);
            dialog.getButtonTypes().setAll(openButton, ButtonType.OK);

            dialog.showAndWait().ifPresent(action -> {
                if (action == openButton) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                    }
                }
            });
        }
    }

    public static String showLongfilePath(String path) {
        String retVal = "";
        String fileSeparatorPattern = Pattern.quote(File.separator);
        String[] pathParts = path.split(fileSeparatorPattern);
        for (int i = 0; i < pathParts.length; i++) {
            retVal += pathParts[i] + (i < (pathParts.length - 1) ? File.separator : "") + "\n";
            for (int j = 0; j < i; j++) {
                retVal += "  ";
            }
        }

        return retVal;
    }

}