package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.ActorInterface;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 335;
    private static ColorSelectionWindow instance;

    private Map<Integer, SpeciesColors> mapOfSpeciesToColors;

    private Map<Integer, SpeciesColors> originalMapOfSpeciesToColors;
    private VBox root;
    private Stage stage;
    private Scene scene;
    private ColorPicker colorPicker;
    private Button okButton;
    private Button cancelButton;
    private SpeciesColorPane[] speciesColorPanes;
    private ColorListener colorListener;
    private ActorInterface actor;

    private class ColorListener implements ChangeListener<Color> {

        private ColorSplotch colorSplotchReference;

        public ColorListener(ColorSplotch colorSplotchReference) {
            this.colorSplotchReference = colorSplotchReference;
        }

        @Override
        public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
            DetectorPlotFlavor plotFlavor = colorSplotchReference.getPlotFlavor();
            int index = colorSplotchReference.getIndex();
            SpeciesColors speciesColors = mapOfSpeciesToColors.get(index);
            mapOfSpeciesToColors.remove(index);
            mapOfSpeciesToColors.put(index,
                    speciesColors.altered(plotFlavor,
                            String.format("#%02x%02x%02x",
                                    (int) (newValue.getRed() * 255),
                                    (int) (newValue.getGreen() * 255),
                                    (int) (newValue.getBlue() * 255))));
            colorSplotchReference.setColor(newValue);
            actor.act();
        }
        public void setColorSplotch(ColorSplotch colorSplotch) {
            this.colorSplotchReference = colorSplotch;
        }

        public ColorSplotch getColorSplotchReference() {
            return colorSplotchReference;
        }
    }
    public static ColorSelectionWindow colorSelectionWindowRequest(
            Map<Integer, SpeciesColors> mapOfSpeciesToColors,
            List<SpeciesRecordInterface> species,
            Window owner,
            ActorInterface actor) {
        if (instance == null) {
            instance = new ColorSelectionWindow(mapOfSpeciesToColors, species, owner,actor);
        }
        return instance;
    }
    private ColorSelectionWindow(Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                                 List<SpeciesRecordInterface> species,
                                 Window owner, ActorInterface actor) {
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.originalMapOfSpeciesToColors = new TreeMap<>();
        originalMapOfSpeciesToColors.putAll(mapOfSpeciesToColors);
        this.root = new VBox();
        this.actor = actor;
        initStage(owner);
        initSpeciesColorPanes(species);
        this.colorListener = new ColorListener(speciesColorPanes[0].getSpeciesColorRows()[0].getColorSplotch());
        colorPicker = new ColorPicker();
        colorPicker.prefWidthProperty().bind(stage.widthProperty());
        colorPicker.valueProperty().setValue(this.colorListener.colorSplotchReference.getColor());
        colorPicker.valueProperty().addListener(this.colorListener);
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
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, click -> {
            if(click.getTarget() instanceof ColorSplotch) {
                colorPicker.valueProperty().removeListener(colorListener);
                colorListener.setColorSplotch((ColorSplotch) click.getTarget());
                colorPicker.setValue(colorListener.colorSplotchReference.getColor());
                colorPicker.valueProperty().addListener(colorListener);
            }
        });
    }

    public void show() {
        stage.show();
        stage.toFront();
    }
}
