package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.SpeciesColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 335;
    private static ColorSelectionWindow instance;

    private Map<Integer, SpeciesColors> mapOfSpeciesToColors;

    private VBox root;
    private Stage stage;
    private Scene scene;
    private ColorPicker colorPicker;
    private Button okButton;
    private Button cancelButton;
    private SpeciesColorPane[] speciesColorPanes;

    private class colorListener implements ChangeListener<Color> {

        @Override
        public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {

        }
    }
    public static ColorSelectionWindow colorSelectionWindowRequest(
            Map<Integer, SpeciesColors> mapOfSpeciesToColors,
            List<SpeciesRecordInterface> species,
            Window owner) {
        if (instance == null) {
            instance = new ColorSelectionWindow(mapOfSpeciesToColors,species,owner);
        }
        return instance;
    }
    private ColorSelectionWindow(Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                                List<SpeciesRecordInterface> species,
                                Window owner) {
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.root = new VBox();
        initStage(owner);
        initSpeciesColorPanes(species);
        colorPicker = new ColorPicker();
        colorPicker.prefWidthProperty().bind(stage.widthProperty());
        for (SpeciesColorPane pane : speciesColorPanes) {
            colorPicker.getCustomColors().addAll(
                    Arrays.stream(
                            pane.getSpeciesColorRows()).map(x -> { return x.getColor();}).toList());
        }
        root.getChildren().add(colorPicker);
    }


    private void initSpeciesColorPanes(List<SpeciesRecordInterface> species) {
        speciesColorPanes = new SpeciesColorPane[species.size()];
        for (int i = 0; i < species.size(); ++i) {
            speciesColorPanes[i] =
                    new SpeciesColorPane(i,
                            species.get(i).prettyPrintShortForm(),
                            mapOfSpeciesToColors.get(i));
            speciesColorPanes[i].prefWidthProperty().bind(stage.widthProperty());
            root.getChildren().add(speciesColorPanes[i]);
        }
    }
    private void initStage(Window owner) {
        scene = new Scene(this.root);
        stage = new Stage();
        stage.setWidth(WINDOW_PREF_WIDTH);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.setTitle(WINDOW_TITLE);
        stage.setOnCloseRequest(closeRequest ->{
            instance = null;
        });
    }

    public void show() {
        stage.show();
        stage.toFront();
    }
}
