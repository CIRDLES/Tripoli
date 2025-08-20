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

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;

import java.io.File;
import java.util.Arrays;
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
        setContentText((null == message) ? "Unknown error ..." : message);
        setHeaderText(headerText);
        initStyle(StageStyle.DECORATED);
        int countOfNewLines = 1;
        if (null != message) {
            for (int i = 0; i < message.length(); i++) {
                countOfNewLines = countOfNewLines + (('\n' == message.charAt(i)) ? 1 : 0);
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
        alert.setOnCloseRequest(event -> {alert.close();});
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
        alert.setOnCloseRequest(event -> {alert.close();});
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

    public static MassSpectrometerContextEnum showMassSpecChoiceDialog(String message, Window owner) {
        Alert alert = new TripoliMessageDialog(
                Alert.AlertType.CONFIRMATION,
                message,
                "Supported MassSpec types:", owner);
        alert.getButtonTypes().setAll(ButtonType.APPLY);

        // Create ListView
        ListView<MassSpectrometerContextEnum> listView = new ListView<>();
        listView.getItems().addAll(MassSpectrometerContextEnum.values());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Show friendly names in the ListView
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MassSpectrometerContextEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // Preselect first item
        listView.getSelectionModel().selectFirst();

        // Add ListView into dialog content
        alert.getDialogPane().setContent(listView);

        // Resize based on listView preferred size
        listView.setPrefHeight(Math.min(listView.getItems().size(), 7) * 28 + 10);
        alert.getDialogPane().setPrefHeight(listView.getPrefHeight() + 150);

        // Show dialog and return result
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            return listView.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    public static boolean showSessionDiffDialog(String diffMessage, Window owner) {
        Alert alert = new TripoliMessageDialog(
                Alert.AlertType.CONFIRMATION,
                "",
                "Tripoli informs you:", owner);

        // Header message (above the text area)
        Label instructionLabel = new Label(
                "The Custom Expressions in this Session differ from those saved for the Method. \n" +
                        "Would you like to replace the Custom Expressions with the saved defaults?"
        );
        instructionLabel.setWrapText(true);
        instructionLabel.setMinHeight(40);

        // Monospaced text area for the formatted diff
        TextArea textArea = new TextArea(diffMessage);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced"));
        textArea.setPrefColumnCount(80);
        textArea.setPrefRowCount(20);

        // Combine label and text area in a vertical layout
        VBox content = new VBox(20);
        content.getChildren().addAll(instructionLabel, textArea);

        // Set VBox as the dialog content
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefSize(800, 400);

        alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.OK);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean showOverwriteReportDialog(String name, Window owner) {
        try{
            Alert dialog = new TripoliMessageDialog(AlertType.CONFIRMATION,
                    "Report : " + name + " already exists.  Do you want to overwrite it?",
                     "Overwrite Report?",
                    owner);
            dialog.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
            dialog.setOnCloseRequest(event -> {dialog.close();});
            Optional<ButtonType> result = dialog.showAndWait();

            return (result.get() == ButtonType.YES);
        } catch (Exception ignored){
            return false;
        }
    }

    public static String showSavedAsDialog(File file, Window owner) {
        if (null == file) {
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
            ButtonType saveAndOpenButton = new ButtonType("Save and Open", ButtonBar.ButtonData.OK_DONE);
            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getButtonTypes().setAll(ButtonType.CANCEL, saveButton, saveAndOpenButton);
            dialog.setOnCloseRequest(event -> {dialog.close();});

            return dialog.showAndWait().get().getText();

        }
        return null;
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