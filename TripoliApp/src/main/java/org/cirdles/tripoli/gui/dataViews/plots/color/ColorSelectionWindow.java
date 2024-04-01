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
import org.cirdles.tripoli.species.SpeciesColorSetting;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionInterface;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_DEFAULT_HEX_COLORS;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 335;
    public static final double BUTTON_PREF_HEIGHT = 35;
    private static ColorSelectionWindow instance;
    private final Map<Integer, SpeciesColors> mapOfSpeciesToColors;
    private final Stack<SpeciesColorSetting> previousSpeciesColorSettingsStack;
    private final Map<Integer, SpeciesColors> originalMapOfSpeciesToColors;
    private final VBox root;
    private Stage stage;
    private ColorPicker colorPicker;
    private Label colorPickerLabel;// To change the text of the color picker
    private SpeciesColorRowSelectionRecord speciesColorRowSelectionRecord;
    private SpeciesColorPane[] speciesColorPanes;
    private Button undoButton;
    private final ColorListener colorListener;

    private final DelegateActionSet rebuildDelegateActionSet;



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
            rebuildDelegateActionSet.executeDelegateActions();
        }
        public void setColorSplotch(ColorSplotch colorSplotch) {
            this.colorSplotchReference = colorSplotch;
        }

    }
    public static ColorSelectionWindow colorSelectionWindowRequest(
            Map<Integer, SpeciesColors> mapOfSpeciesToColors,
            Stack<SpeciesColorSetting> previousSpeciesColorSettingsStack,
            List<SpeciesRecordInterface> species,
            Window owner,
            DelegateActionSet rebuildDelegateActionSet) {
        if (instance == null) {
            instance = new ColorSelectionWindow(
                    mapOfSpeciesToColors,
                    previousSpeciesColorSettingsStack,
                    species,
                    owner,
                    rebuildDelegateActionSet);
        }
        return instance;
    }
    private ColorSelectionWindow(Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                                 Stack<SpeciesColorSetting> previousSpeciesColorSettingsStack,
                                 List<SpeciesRecordInterface> species,
                                 Window owner,
//                                 DelegateActionInterface rebuildPlotDelegateAction,
                                 DelegateActionSet rebuildDelegateActionSet) {
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.previousSpeciesColorSettingsStack = previousSpeciesColorSettingsStack;
        this.originalMapOfSpeciesToColors = new TreeMap<>();
        originalMapOfSpeciesToColors.putAll(mapOfSpeciesToColors);
        this.root = new VBox();
//        this.rebuildPlotDelegateAction = rebuildPlotDelegateAction;
        this.rebuildDelegateActionSet = rebuildDelegateActionSet;
        initStage(owner);
        initSpeciesColorPanes(species);
        this.colorListener = new ColorListener(
                speciesColorPanes[0].
                        getMapOfPlotFlavorsToSpeciesColorRows().
                        get(DetectorPlotFlavor.values()[0]).getColorSplotch());
        speciesColorRowSelectionRecord = new SpeciesColorRowSelectionRecord(
                speciesColorPanes[0],
                speciesColorPanes[0].getMapOfPlotFlavorsToSpeciesColorRows().get(
                        DetectorPlotFlavor.values()[0]),
                new SpeciesColorSetting(0, mapOfSpeciesToColors.get(0)));
        speciesColorRowSelectionRecord.speciesColorRow().highlight();
        speciesColorRowSelectionRecord.speciesColorPane().highlight();
        this.root.getChildren().add(initColorPicker());
        root.getChildren().add(initUndoButton());
        root.getChildren().add(initResetButton());

    }

    private void makeSelection(int speciesIndex, DetectorPlotFlavor plotFlavor) {
        speciesColorRowSelectionRecord.speciesColorPane().removeHighlight();
        speciesColorRowSelectionRecord.speciesColorRow().removeHighlight();
        SpeciesColorPane selectedPane = speciesColorPanes[speciesIndex];
        SpeciesColorRow selectedRow = selectedPane.getMapOfPlotFlavorsToSpeciesColorRows().get(plotFlavor);
        selectedPane.highlight();
        selectedRow.highlight();
        speciesColorRowSelectionRecord = new SpeciesColorRowSelectionRecord(
                selectedPane,
                selectedRow,
                new SpeciesColorSetting(
                        speciesIndex, mapOfSpeciesToColors.get(speciesIndex)));
    }

    private void resetColors(){
        // TODO: Change this into reset to
        int numberOfSpecies = this.speciesColorPanes.length;
        // Redraw the plot with new colors

        mapOfSpeciesToColors.clear();
        previousSpeciesColorSettingsStack.clear();
        undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        for (int speciesIndex = 0; speciesIndex < numberOfSpecies ; ++speciesIndex){
            SpeciesColors speciesColors = new SpeciesColors(
                    TRIPOLI_DEFAULT_HEX_COLORS[speciesIndex * 4],
                    TRIPOLI_DEFAULT_HEX_COLORS[speciesIndex * 4 + 1],
                    TRIPOLI_DEFAULT_HEX_COLORS[speciesIndex * 4 + 2],
                    TRIPOLI_DEFAULT_HEX_COLORS[speciesIndex * 4 + 3]
            );
            mapOfSpeciesToColors.put(speciesIndex, speciesColors);
            SpeciesColorPane pane = speciesColorPanes[speciesIndex];
            for(DetectorPlotFlavor plotFlavor: DetectorPlotFlavor.values()) {
                pane.getMapOfPlotFlavorsToSpeciesColorRows().get(plotFlavor).setColor(
                        Color.web(speciesColors.get(plotFlavor)));
            }
        }
        colorPicker.setValue(speciesColorRowSelectionRecord.speciesColorRow().getColor());
        rebuildDelegateActionSet.executeDelegateActions();
    }

    private void undo(){
        // TODO: Change this into undo
        if (!previousSpeciesColorSettingsStack.empty()){
            SpeciesColorSetting previousSpeciesColorSetting = previousSpeciesColorSettingsStack.pop();
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
            mapOfSpeciesToColors.put(
                    previousSpeciesColorSetting.index(),
                    previousSpeciesColorSetting.speciesColors());
            SpeciesColorPane speciesColorPane = speciesColorPanes[previousSpeciesColorSetting.index()];
            for(DetectorPlotFlavor plotFlavor: DetectorPlotFlavor.values()) {
                speciesColorPane.getMapOfPlotFlavorsToSpeciesColorRows().
                        get(plotFlavor).
                        setColor(Color.web(
                                previousSpeciesColorSetting.speciesColors().get(plotFlavor)));
            }
            colorPicker.setValue(speciesColorRowSelectionRecord.speciesColorRow().getColor());
            rebuildDelegateActionSet.executeDelegateActions();
        }
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

    private Button initResetButton() {
        Button resetButton = new Button("Reset");
        resetButton.prefWidthProperty().bind(stage.widthProperty());
        resetButton.setPrefHeight(BUTTON_PREF_HEIGHT);
        resetButton.setOnAction(cancelChanges -> {
            resetColors();});
        return resetButton;
    }

    private Button initUndoButton() {
        this.undoButton = new Button("Undo");
        undoButton.prefWidthProperty().bind(stage.widthProperty());
        undoButton.setPrefHeight(BUTTON_PREF_HEIGHT);
        undoButton.setOnAction(undoLastChange -> {
            undo();
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        });
        undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        return undoButton;
    }
    private ColorPicker initColorPicker() {
        this.colorPicker = new ColorPicker();
        this.colorPicker.prefWidthProperty().bind(stage.widthProperty());
        this.colorPicker.setPrefHeight(BUTTON_PREF_HEIGHT);
        this.colorPicker.valueProperty().setValue(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.getCustomColors().add(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.valueProperty().addListener(this.colorListener);
        // TODO: Set up the action to store the color change
        this.colorPicker.setOnAction(action -> {
            previousSpeciesColorSettingsStack.push(speciesColorRowSelectionRecord.speciesColorSetting());
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        });
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
