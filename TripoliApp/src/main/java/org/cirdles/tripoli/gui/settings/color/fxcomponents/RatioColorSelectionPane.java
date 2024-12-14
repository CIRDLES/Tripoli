package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.constants.ConstantsTripoliApp;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.settings.plots.RatiosColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import static org.cirdles.tripoli.constants.TripoliConstants.RatiosPlotColorFlavor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

public class RatioColorSelectionPane extends ScrollPane implements Initializable {

    @FXML
    private VBox vBox;

    private ObjectProperty<RatiosColors> ratioColorsProperty;
    private List<RatioColorRow> ratioColorRowList;


    public RatioColorSelectionPane(DelegateActionSet delegateActionSet, AnalysisInterface analysis) {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RatioColorSelectionPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();// TODO: Logging
        }
        this.ratioColorsProperty = new SimpleObjectProperty<>(analysis.getRatioColors());
        ratioColorRowList = new ArrayList<>();
        if (analysis.getRatioColors() != null) {
            for (RatiosPlotColorFlavor flavor : RatiosPlotColorFlavor.values()){
                RatioColorRow ratioColorRow = new RatioColorRow(analysis.getRatioColors(), flavor);
                ratioColorRow.setPrefHeight(30);
                Region region = new Region();
                region.setPrefHeight(35);
                vBox.getChildren().addAll(region, ratioColorRow);
                ratioColorRowList.add(ratioColorRow);
                ratioColorRow.colorProperty().addListener(((observable, oldValue, newValue) -> {
                    ratioColorsProperty.set(ratioColorsProperty.get().altered(
                            ratioColorRow.getPlotColorFlavor(),
                            ConstantsTripoliApp.convertColorToHex(newValue)
                    ));
                    analysis.setRatioColors(ratioColorsProperty.get());
                    delegateActionSet.executeDelegateActions();
                }));
            }
        }
    }

    public void updateRatioColorsProperty(RatiosColors ratiosColors) {
        this.ratioColorsProperty.set(ratiosColors);
        for (RatioColorRow ratioColorRow : ratioColorRowList) {
            ratioColorRow.colorProperty().set(
                    Color.web(
                            ratioColorsProperty.get().get(ratioColorRow.getPlotColorFlavor())
                    )
            );
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


    public VBox getVBoxRoot() {
        return vBox;
    }
}
