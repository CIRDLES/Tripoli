package org.cirdles.tripoli.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.net.URL;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_ANALYSIS_YELLOW;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.convertColorToHex;

public class AnalysesManagerController implements Initializable {

    public static AnalysisInterface anaylysis;
    @FXML
    private GridPane analysisManagerGridPane;
    @FXML
    private TextField analysisNameTextField;
    @FXML
    private TextField sampleTextField;
    @FXML
    private TextField sampleDescriptionTextField;
    @FXML
    private TextField dataFilePathName;
    @FXML
    private TextField analystNameTextField;
    @FXML
    private TextField labNameTextField;

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        analysisManagerGridPane.setStyle("-fx-background-color: " + convertColorToHex(TRIPOLI_ANALYSIS_YELLOW));

        populateAnalysisManagerGridPane();
    }

    private void populateAnalysisManagerGridPane() {
        analysisNameTextField.setEditable(false);
        analysisNameTextField.setText(anaylysis.getAnalysisName());

        sampleTextField.setEditable(false);
        sampleTextField.setText(anaylysis.getAnalysisSample().getSampleName());

        sampleDescriptionTextField.setEditable(false);
        sampleDescriptionTextField.setText(anaylysis.getAnalysisSampleDescription());
    }
}