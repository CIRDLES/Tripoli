package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RatioColorSelectionPane extends ScrollPane implements Initializable {

    @FXML
    private VBox vBox;
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

    public RatioColorSelectionPane(DelegateActionSet delegateActionSet, AnalysisInterface analysis) {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RatioColorSelectionPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getOneSigmaSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
        getOneSigmaSplotch().setHexColorSetter(analysis::setOneSigmaHexColorString);
        getOneSigmaSplotch().getColorPicker().setValue(Color.web(analysis.getOneSigmaHexColorString()));
        getTwoSigmaSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
        getTwoSigmaSplotch().setHexColorSetter(analysis::setTwoSigmaHexColorString);
        getTwoSigmaSplotch().getColorPicker().setValue(Color.web(analysis.getTwoSigmaHexColorString()));
        getStdErrorSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
        getStdErrorSplotch().setHexColorSetter(analysis::setTwoStandardErrorHexColorString);
        getStdErrorSplotch().getColorPicker().setValue(Color.web(analysis.getTwoStandardErrorHexColorString()));
        getMeanSplotch().getDelegateActionSet().addDelegateActions(delegateActionSet);
        getMeanSplotch().setHexColorSetter(analysis::setMeanHexColorString);
        getMeanSplotch().getColorPicker().setValue(Color.web(analysis.getMeanHexColorString()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        twoSigmaSplotch = new ColorPickerSplotch();
        twoSigmaSplotch.getColorPicker().setValue(OGTRIPOLI_TWOSIGMA);
        twoSigmaSplotch.setPrefWidth(twoSigmaStackPane.getPrefWidth());
        twoSigmaSplotch.setPrefHeight(twoSigmaStackPane.getPrefHeight());
        twoSigmaHBox.getChildren().remove(twoSigmaStackPane);
        twoSigmaStackPane = twoSigmaSplotch;
        twoSigmaHBox.getChildren().add(twoSigmaHBox.getChildren().size() - 1, twoSigmaSplotch);
        oneSigmaSplotch = new ColorPickerSplotch();
        oneSigmaSplotch.getColorPicker().setValue(OGTRIPOLI_ONESIGMA);
        oneSigmaSplotch.setPrefWidth(oneSigmaStackPane.getPrefWidth());
        oneSigmaSplotch.setPrefHeight(oneSigmaStackPane.getPrefHeight());
        oneSigmaHBox.getChildren().remove(oneSigmaStackPane);
        oneSigmaHBox.getChildren().add(oneSigmaHBox.getChildren().size() - 1,oneSigmaSplotch);
        stdErrorSplotch = new ColorPickerSplotch();
        stdErrorSplotch.getColorPicker().setValue(OGTRIPOLI_TWOSTDERR);
        stdErrorSplotch.setPrefWidth(stdErrorStackPane.getPrefWidth());
        stdErrorSplotch.setPrefHeight(stdErrorStackPane.getPrefHeight());
        stdErrorHBox.getChildren().remove(stdErrorStackPane);
        stdErrorHBox.getChildren().add(stdErrorHBox.getChildren().size() - 1, stdErrorSplotch);
        meanSplotch = new ColorPickerSplotch();
        meanSplotch.getColorPicker().setValue(OGTRIPOLI_MEAN);
        meanSplotch.setPrefWidth(meanStackPane.getPrefWidth());
        meanSplotch.setPrefHeight(meanStackPane.getPrefHeight());
        meanHBox.getChildren().remove(meanStackPane);
        meanHBox.getChildren().add(meanHBox.getChildren().size() - 1, meanSplotch);
//        HBox.setHgrow(twoSigmaSplotch, Priority.ALWAYS);
//        HBox.setHgrow(oneSigmaSplotch, Priority.ALWAYS);
//        HBox.setHgrow(stdErrorSplotch, Priority.ALWAYS);
//        HBox.setHgrow(meanSplotch, Priority.ALWAYS);
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

    public VBox getVBoxRoot() {
        return vBox;
    }
}
