package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionInterface;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 335;
    public static final double BUTTON_PREF_HEIGHT = 35;
    private static ColorSelectionWindow instance;
    private final Map<Integer, SpeciesColors> mapOfSpeciesToColors;
    private final Map<Integer, SpeciesColors> originalMapOfSpeciesToColors;
    private final VBox root;
    private Stage stage;
    private ColorPicker colorPicker;
    private Label colorPickerLabel;// To change the text of the color picker
    private SpeciesColorSelectionRecord speciesColorSelectionRecord;
    private SpeciesColorPane[] speciesColorPanes;
    private final ColorListener colorListener;
    private final DelegateActionInterface rebuildPlotDelegateAction;



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
            setColorPickerLabelText();
            rebuildPlotDelegateAction.act();
        }
        public void setColorSplotch(ColorSplotch colorSplotch) {
            this.colorSplotchReference = colorSplotch;
        }

    }
    public static ColorSelectionWindow colorSelectionWindowRequest(
            Map<Integer, SpeciesColors> mapOfSpeciesToColors,
            List<SpeciesRecordInterface> species,
            Window owner,
            DelegateActionInterface rebuildPlotDelegateAction) {
        if (instance == null) {
            instance = new ColorSelectionWindow(mapOfSpeciesToColors, species, owner,rebuildPlotDelegateAction);
        }
        return instance;
    }
    private ColorSelectionWindow(Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                                 List<SpeciesRecordInterface> species,
                                 Window owner, DelegateActionInterface rebuildPlotDelegateAction) {
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.originalMapOfSpeciesToColors = new TreeMap<>();
        originalMapOfSpeciesToColors.putAll(mapOfSpeciesToColors);
        this.root = new VBox();
        this.rebuildPlotDelegateAction = rebuildPlotDelegateAction;
        initStage(owner);
        initSpeciesColorPanes(species);
        this.colorListener = new ColorListener(
                speciesColorPanes[0].
                        getMapOfPlotFlavorsToSpeciesColorRows().
                        get(DetectorPlotFlavor.values()[0]).getColorSplotch());
        speciesColorSelectionRecord = new SpeciesColorSelectionRecord(
                speciesColorPanes[0],
                speciesColorPanes[0].getMapOfPlotFlavorsToSpeciesColorRows().get(
                        DetectorPlotFlavor.values()[0]));
        speciesColorSelectionRecord.speciesColorRow().highlight();
        speciesColorSelectionRecord.speciesColorPane().highlight();
        this.root.getChildren().add(initColorPicker());
        root.getChildren().add(initAcceptButton());
        root.getChildren().add(initCancelButton());

    }

    private void makeSelection(int speciesIndex, DetectorPlotFlavor plotFlavor) {
        speciesColorSelectionRecord.speciesColorPane().removeHighlight();
        speciesColorSelectionRecord.speciesColorRow().removeHighlight();
        SpeciesColorPane selectedPane = speciesColorPanes[speciesIndex];
        SpeciesColorRow selectedRow = selectedPane.getMapOfPlotFlavorsToSpeciesColorRows().get(plotFlavor);
        selectedPane.highlight();
        selectedRow.highlight();
        speciesColorSelectionRecord = new SpeciesColorSelectionRecord(selectedPane, selectedRow);
    }

    private void cancel(){
        mapOfSpeciesToColors.clear();
        mapOfSpeciesToColors.putAll(originalMapOfSpeciesToColors);
        rebuildPlotDelegateAction.act();
        stage.close();
        instance = null;
    }

    private void accept(){
        this.stage.close();
        this.rebuildPlotDelegateAction.act();
        instance = null;
    }

    private void setColorPickerLabelText() {
        if(colorPickerLabel == null) {
            for(Node child : colorPicker.getChildrenUnmodifiable()) {
                if(child instanceof Label colorPickerLabelReference) {
                    this.colorPickerLabel = colorPickerLabelReference;
                }
            }
        }
        if(colorPickerLabel != null) {
            colorPickerLabel.setText(String.format("%s Color for %s Species",
                    this.colorListener.colorSplotchReference.getPlotFlavor().getName(),
                    speciesColorPanes[this.colorListener.colorSplotchReference.getIndex()]
                            .getTitle().getText()));
        }
    }

    private Button initCancelButton() {
        Button cancelButton = new Button("Cancel");
        cancelButton.prefWidthProperty().bind(stage.widthProperty());
        cancelButton.setPrefHeight(BUTTON_PREF_HEIGHT);
        cancelButton.setOnAction(cancelChanges -> {cancel();});
        return cancelButton;
    }

    private Button initAcceptButton() {
        Button okButton = new Button("Accept Changes");
        okButton.prefWidthProperty().bind(stage.widthProperty());
        okButton.setPrefHeight(BUTTON_PREF_HEIGHT);
        okButton.setOnAction(acceptChanges -> {accept();});
        return okButton;
    }
    private ColorPicker initColorPicker() {
        this.colorPicker = new ColorPicker();
        this.colorPicker.prefWidthProperty().bind(stage.widthProperty());
        this.colorPicker.setPrefHeight(BUTTON_PREF_HEIGHT);
        this.colorPicker.valueProperty().setValue(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.getCustomColors().add(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.valueProperty().addListener(this.colorListener);
        return this.colorPicker;
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
        Scene scene = new Scene(this.root);
        stage = new Stage();
        stage.setWidth(WINDOW_PREF_WIDTH);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.setTitle(WINDOW_TITLE);
        stage.setOnCloseRequest(closeRequest ->{
            instance = null;
            cancel();
        });
        this.stage.setResizable(false);
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, click -> {
            if (click.getTarget() instanceof FlavoredIndexedLabel flavoredIndexedLabel) {
                int index = flavoredIndexedLabel.getIndex();
                DetectorPlotFlavor plotFlavor = flavoredIndexedLabel.getPlotFlavor();
                SpeciesColorPane speciesColorPane = speciesColorPanes[index];
                ColorSplotch colorSplotch =
                        speciesColorPane.getMapOfPlotFlavorsToSpeciesColorRows().get(plotFlavor).getColorSplotch();
                colorPicker.valueProperty().removeListener(colorListener);
                colorListener.setColorSplotch(colorSplotch);
                makeSelection(index, flavoredIndexedLabel.getPlotFlavor());
                colorPicker.valueProperty().setValue(colorSplotch.getColor());
                if (! colorPicker.getCustomColors().contains(colorSplotch.getColor())) {
                    colorPicker.getCustomColors().add(colorSplotch.getColor());
                }
                setColorPickerLabelText();
                colorPicker.valueProperty().addListener(colorListener);
                click.consume();
            }
        });
    }

    public void show() {
        stage.show();
        setColorPickerLabelText();
        stage.toFront();
    }
}
