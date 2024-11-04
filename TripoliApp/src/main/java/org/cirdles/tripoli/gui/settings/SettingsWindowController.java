package org.cirdles.tripoli.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.cirdles.tripoli.gui.settings.color.fxcomponents.SpeciesColorSelectionRow;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {


    @FXML
    private AnchorPane plotIntensitiesAnchorPane;
    @FXML
    private Button saveAnalysisSettingsButton;
    @FXML
    private Button saveAsSessionDefaultsButton;
    @FXML
    private Button saveAsUserDefaultsButton;
    @FXML
    private Button restoreSessionDefaultsButton;
    @FXML
    private Button restoreUserDefaultsButton;
    @FXML
    private ToolBar revertToolbar;
    @FXML
    private VBox vBoxRoot;
    @FXML
    private TabPane plotRatiosTabPane;
    @FXML
    private AnchorPane ratioColorSelectionAnchorPane;

    @FXML
    private AnchorPane plotIntensitiesAnchorPaneExp;

    @FXML
    private VBox plotIntensitiesVBox;

    @FXML
    private ToolBar saveDefaultsToolbar;

    @FXML
    private Tab oldIntensityPlotTab;


    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        oldIntensityPlotTab.getTabPane().getTabs().remove(oldIntensityPlotTab);
        // remove the above to see the old intensity plot implementation
    }

    public VBox getvBoxRoot() {
        return vBoxRoot;
    }

    public TabPane getPlotRatiosTabPane() {
        return plotRatiosTabPane;
    }

    public AnchorPane getRatioColorSelectionAnchorPane() {
        return ratioColorSelectionAnchorPane;
    }

    public AnchorPane getPlotIntensitiesAnchorPane() { return plotIntensitiesAnchorPane;}

    public ToolBar getSaveDefaultsToolbar() {
        return saveDefaultsToolbar;
    }

    public Button getSaveAnalysisSettingsButton() {
        return saveAnalysisSettingsButton;
    }

    public Button getSaveAsSessionDefaultsButton() {
        return saveAsSessionDefaultsButton;
    }

    public Button getSaveAsUserDefaultsButton() {
        return saveAsUserDefaultsButton;
    }

    public Button getRestoreSessionDefaultsButton() {
        return restoreSessionDefaultsButton;
    }

    public Button getRestoreUserDefaultsButton() {
        return restoreUserDefaultsButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public AnchorPane getPlotIntensitiesAnchorPaneExp() {
        return plotIntensitiesAnchorPaneExp;
    }

    public VBox getPlotIntensitiesVBox() {
        return plotIntensitiesVBox;
    }

}
