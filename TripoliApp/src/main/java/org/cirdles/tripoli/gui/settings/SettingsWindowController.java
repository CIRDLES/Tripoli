package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import org.cirdles.tripoli.gui.dataViews.plots.color.fxcomponents.ColorPickerSplotch;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {

    @FXML
    private VBox settingsWindowVBoxRoot;
    @FXML
    private HBox twoSigmaHBox;
    @FXML
    private StackPane twoSigmaStackPane;
    @FXML
    private HBox oneSigmaHBox;
    @FXML
    private StackPane oneSigmaStackPane;
    @FXML
    private HBox stdErrorHBox;
    @FXML
    private StackPane stdErrorStackPane;
    @FXML
    private HBox meanHBox;
    @FXML
    private StackPane meanStackPane;

    private ColorPickerSplotch twoSigmaSplotch;
    private ColorPickerSplotch oneSigmaSplotch;
    private ColorPickerSplotch stdErrorSplotch;
    private ColorPickerSplotch meanSplotch;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        twoSigmaSplotch = new ColorPickerSplotch();
        twoSigmaSplotch.getColorPicker().setValue(OGTRIPOLI_TWOSIGMA);
        twoSigmaSplotch.setPrefWidth(twoSigmaStackPane.getPrefWidth());
        twoSigmaSplotch.setPrefHeight(twoSigmaStackPane.getPrefHeight());
        twoSigmaHBox.getChildren().remove(twoSigmaStackPane);
        twoSigmaStackPane = twoSigmaSplotch;
        twoSigmaHBox.getChildren().add(twoSigmaSplotch);
        oneSigmaSplotch = new ColorPickerSplotch();
        oneSigmaSplotch.getColorPicker().setValue(OGTRIPOLI_ONESIGMA);
        oneSigmaSplotch.setPrefWidth(oneSigmaStackPane.getPrefWidth());
        oneSigmaSplotch.setPrefHeight(oneSigmaStackPane.getPrefHeight());
        oneSigmaHBox.getChildren().remove(oneSigmaStackPane);
        oneSigmaHBox.getChildren().add(oneSigmaSplotch);
        stdErrorSplotch = new ColorPickerSplotch();
        stdErrorSplotch.getColorPicker().setValue(OGTRIPOLI_TWOSTDERR);
        stdErrorSplotch.setPrefWidth(stdErrorStackPane.getPrefWidth());
        stdErrorSplotch.setPrefHeight(stdErrorStackPane.getPrefHeight());
        stdErrorHBox.getChildren().remove(stdErrorStackPane);
        stdErrorHBox.getChildren().add(stdErrorSplotch);
        meanSplotch = new ColorPickerSplotch();
        meanSplotch.getColorPicker().setValue(OGTRIPOLI_MEAN);
        meanSplotch.setPrefWidth(meanStackPane.getPrefWidth());
        meanSplotch.setPrefHeight(meanStackPane.getPrefHeight());
        meanHBox.getChildren().remove(meanStackPane);
        meanHBox.getChildren().add(meanSplotch);
    }

    public ColorPickerSplotch getTwoSigmaSplotch() {
        return twoSigmaSplotch;
    }

    public ColorPickerSplotch getOneSigmaSplotch() {
        return oneSigmaSplotch;
    }

    public ColorPickerSplotch getStdErrorSplotch() {
        return stdErrorSplotch;
    }

    public ColorPickerSplotch getMeanSplotch() {
        return meanSplotch;
    }

    public VBox getSettingsWindowVBoxRoot() {
        return settingsWindowVBoxRoot;
    }
}
