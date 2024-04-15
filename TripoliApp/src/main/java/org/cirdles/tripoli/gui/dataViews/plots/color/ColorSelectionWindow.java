package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.SpeciesColorSetting;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionInterface;
import org.cirdles.tripoli.utilities.DelegateActionSet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_DEFAULT_HEX_COLORS;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 335;
    public static final double BUTTON_PREF_HEIGHT = 35;
    public static final double TOOLBAR_PREF_HEIGHT = 25;
    public static final double TOOLBAR_BUTTON_HEIGHT = 18;
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
            int indexOfFirstCheckedSpecies,
            Window owner,
            DelegateActionSet rebuildDelegateActionSet) {
        if (instance == null) {
            instance = new ColorSelectionWindow(
                    mapOfSpeciesToColors,
                    previousSpeciesColorSettingsStack,
                    species,
                    indexOfFirstCheckedSpecies,
                    owner,
                    rebuildDelegateActionSet);
        }
        return instance;
    }
    private ColorSelectionWindow(Map<Integer, SpeciesColors> mapOfSpeciesToColors,
                                 Stack<SpeciesColorSetting> previousSpeciesColorSettingsStack,
                                 List<SpeciesRecordInterface> species,
                                 int indexOfFirstCheckedSpecies,
                                 Window owner,
                                 DelegateActionSet rebuildDelegateActionSet) {
        this.mapOfSpeciesToColors = mapOfSpeciesToColors;
        this.previousSpeciesColorSettingsStack = previousSpeciesColorSettingsStack;
        this.originalMapOfSpeciesToColors = new TreeMap<>();
        originalMapOfSpeciesToColors.putAll(mapOfSpeciesToColors);
        this.root = new VBox();
        this.rebuildDelegateActionSet = rebuildDelegateActionSet;
        initStage(owner);
        initSpeciesColorPanes(species);
        this.colorListener = new ColorListener(
                speciesColorPanes[indexOfFirstCheckedSpecies].
                        getMapOfPlotFlavorsToSpeciesColorRows().
                        get(DetectorPlotFlavor.values()[0]).getColorSplotch());
        speciesColorRowSelectionRecord = new SpeciesColorRowSelectionRecord(
                speciesColorPanes[indexOfFirstCheckedSpecies],
                speciesColorPanes[indexOfFirstCheckedSpecies].getMapOfPlotFlavorsToSpeciesColorRows().get(
                        DetectorPlotFlavor.values()[0]),
                new SpeciesColorSetting(indexOfFirstCheckedSpecies, mapOfSpeciesToColors.get(indexOfFirstCheckedSpecies)));
        speciesColorRowSelectionRecord.speciesColorRow().highlight();
        speciesColorRowSelectionRecord.speciesColorPane().highlight();
        this.root.getChildren().add(initColorPicker());
        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);
        ToolBar toolBar = new ToolBar(spacerLeft,initResetButton(), initUndoButton(), initAcceptButton(),spacerRight);
        toolBar.prefWidthProperty().bind(stage.widthProperty());
        toolBar.setPadding(new Insets(10));
        this.root.getChildren().add(toolBar);
        this.stage.setWidth(WINDOW_PREF_WIDTH);
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
                    TRIPOLI_DEFAULT_HEX_COLORS.get(speciesIndex * 4),
                    TRIPOLI_DEFAULT_HEX_COLORS.get(speciesIndex * 4 + 1),
                    TRIPOLI_DEFAULT_HEX_COLORS.get(speciesIndex * 4 + 2),
                    TRIPOLI_DEFAULT_HEX_COLORS.get(speciesIndex * 4 + 3)
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

    private void accept() {
        stage.getOnCloseRequest().handle(new WindowEvent(stage.getOwner(),WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
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
        resetButton.prefWidthProperty().bind(stage.widthProperty().divide(4));
        resetButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        resetButton.setOnAction(cancelChanges -> {
            resetColors();});
        return resetButton;
    }

    private Button initUndoButton() {
        this.undoButton = new Button("Undo");
        undoButton.prefWidthProperty().bind(stage.widthProperty().divide(4));
        undoButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        undoButton.setOnAction(undoLastChange -> {
            undo();
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        });
        undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        return undoButton;
    }

    private Button initAcceptButton() {
        Button acceptButton = new Button("Accept");
        acceptButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        acceptButton.prefWidthProperty().bind(stage.widthProperty().divide(4));
        acceptButton.setOnAction((acceptAction) -> accept());
        return acceptButton;
    }
    private ColorPicker initColorPicker() {
        this.colorPicker = new ColorPicker();
        this.colorPicker.prefWidthProperty().bind(stage.widthProperty());
        this.colorPicker.setPrefHeight(BUTTON_PREF_HEIGHT);
        this.colorPicker.valueProperty().setValue(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.getCustomColors().add(this.colorListener.colorSplotchReference.getColor());
        this.colorPicker.valueProperty().addListener(this.colorListener);
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
        stage.setX(owner.getX() - stage.getMinWidth());
        stage.initOwner(owner);

        owner.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            stage.fireEvent(new WindowEvent(owner, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        owner.xProperty().addListener(((observable, oldValue, newValue) -> {

        }));
        stage.setOnCloseRequest(closeRequest ->{
            instance = null;
            stage.close();
        });
        stage.setTitle(WINDOW_TITLE);
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
        if(!stage.isShowing()) {
            stage.setX(stage.getOwner().getScene().getX());
            stage.setY(stage.getOwner().getY());
            this.stage.getOwner().xProperty().addListener(((observable, oldValue, newValue) -> {
                Screen maxXScreen = Screen.getScreens().stream().reduce(
                        Screen.getPrimary(),
                        (screen1, screen2) ->
                                screen1.getBounds().getMaxX() > screen2.getBounds().getMaxX() ?
                                        screen1 : screen2);
                Screen minXScreen = Screen.getScreens().stream().reduce(Screen.getPrimary(),
                        (screen1, screen2) ->
                                screen1.getBounds().getMinX() < screen2.getBounds().getMinX() ?
                                        screen1 : screen2);
                if (maxXScreen.getBounds().getMaxX() >=
                        stage.getX() + stage.getWidth() + newValue.doubleValue() - oldValue.doubleValue() &&
                        minXScreen.getBounds().getMinX() <=
                                stage.getX() + newValue.doubleValue() - oldValue.doubleValue()) {
                    stage.setX(stage.getX() + newValue.doubleValue() - oldValue.doubleValue());
                }
            }));
            this.stage.getOwner().yProperty().addListener(((observable, oldValue, newValue) -> {
                Screen maxYScreen = Screen.getScreens().stream().reduce(
                        Screen.getPrimary(),
                        (screen1, screen2) ->
                                screen1.getBounds().getMaxY() > screen2.getBounds().getMaxY() ?
                                        screen1 : screen2
                );
                Screen minYScreen = Screen.getScreens().stream().reduce(
                        Screen.getPrimary(),
                        (screen1, screen2) ->
                                screen1.getBounds().getMinY() < screen2.getBounds().getMinY() ?
                                        screen1 : screen2
                );
                if (maxYScreen.getBounds().getMaxY() >=
                        stage.getY() + stage.getHeight() + newValue.doubleValue() - oldValue.doubleValue() &&
                        minYScreen.getBounds().getMinY() <=
                                stage.getY() + newValue.doubleValue() - oldValue.doubleValue()) {
                    stage.setY(stage.getY() + newValue.doubleValue() - oldValue.doubleValue());
                }
            }));
            stage.show();
        }
        setColorPickerLabelText();
        stage.toFront();
    }
}
