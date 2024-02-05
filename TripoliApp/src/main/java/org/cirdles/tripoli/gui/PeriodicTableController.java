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

package org.cirdles.tripoli.gui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.cirdles.tripoli.elements.ElementRecord;
import org.cirdles.tripoli.elements.ElementsFactory;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.expressions.species.nuclides.NuclideRecord;
import org.cirdles.tripoli.expressions.species.nuclides.NuclidesFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_LOGO_SANS_TEXT_URL;

/**
 * @author James F. Bowring
 */
public class PeriodicTableController {
    public static final Stage primaryLocalStage = new Stage();
    public static Window periodicTableWindow;
    private static String buttonStyle;

    @FXML
    public GridPane periodicTableGridPane;
    @FXML
    private TextArea elementDetailsTextBox;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

        ObservableList<Node> buttonList = periodicTableGridPane.getChildren();

        for (Node button : buttonList) {
            if (button instanceof Button) {
                int row = GridPane.getRowIndex(button);
                int col = GridPane.getColumnIndex(button);

                List<ElementRecord> targetElementList = ElementsFactory.periodicTableElementsList
                        .stream()
                        .filter(element -> ((element.tripoliCol() == col) && (element.tripoliRow() == row))).toList();

                if (!targetElementList.isEmpty()) {
                    ((Button) button).setText(targetElementList.get(0).symbol());
                    button.setUserData(targetElementList.get(0));
                }
            }
        }

    }

    public void launch() {
        if (!primaryLocalStage.isShowing()) {
            primaryLocalStage.getIcons().add(new Image(TRIPOLI_LOGO_SANS_TEXT_URL));
            primaryLocalStage.setTitle("Tripoli Periodic Table");

            primaryLocalStage.setOnCloseRequest((WindowEvent e) -> {
                primaryLocalStage.hide();
                primaryLocalStage.setScene(null);
                e.consume();
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("elements/PeriodicTable.fxml"));

            try {
                Scene scene = new Scene(loader.load());
                primaryLocalStage.setScene(scene);
                primaryLocalStage.setResizable(false);
                primaryLocalStage.requestFocus();
            } catch (IOException iOException) {
                //TODO: add warning
            }
            periodicTableWindow = primaryLocalStage.getScene().getWindow();

            primaryLocalStage.show();
        }
        primaryLocalStage.requestFocus();
        // center on app window
        periodicTableWindow.setX(TripoliGUI.primaryStage.getX() + (TripoliGUI.primaryStage.getWidth() - primaryLocalStage.getWidth()) / 2);
        periodicTableWindow.setY(TripoliGUI.primaryStage.getY() + (TripoliGUI.primaryStage.getHeight() - primaryLocalStage.getHeight()) / 2);

    }

    public void buttonPress(MouseEvent mouseEvent) {
        Button button = ((Button) mouseEvent.getSource());
        buttonStyle = button.getStyle();
        button.setStyle("-fx-background-color: WHEAT");
        List<SpeciesRecordInterface> nuclides = NuclidesFactory.nuclidesListByElementMap.get(((ElementRecord) button.getUserData()).symbol());
        StringBuilder stringBuilder = new StringBuilder();
        for (SpeciesRecordInterface species : nuclides) {
            stringBuilder.append("\t").append(((NuclideRecord) species).prettyPrintLongForm()).append("\n");
        }
        elementDetailsTextBox.setText(
                ((ElementRecord) button.getUserData()).elementName()
                        + "\n" + stringBuilder);
    }

    public void buttonRelease(MouseEvent mouseEvent) {
        ((Button) mouseEvent.getSource()).setStyle(buttonStyle);
    }

    public void buttonEnter(MouseEvent mouseEvent) {
        Tooltip ratioToolTip = new Tooltip("Info goes here ...");
        ((Button) mouseEvent.getSource()).setTooltip(ratioToolTip);
    }
}