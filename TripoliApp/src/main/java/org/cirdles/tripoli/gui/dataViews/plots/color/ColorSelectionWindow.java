package org.cirdles.tripoli.gui.dataViews.plots.color;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.TripoliGUI;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.species.SpeciesColorSetting;
import org.cirdles.tripoli.species.SpeciesColors;
import org.cirdles.tripoli.utilities.DelegateActionSet;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.util.*;

public class ColorSelectionWindow {
    public static final String WINDOW_TITLE = "Color Customization";
    public static final double WINDOW_PREF_WIDTH = 334;
    public static final double BUTTON_PREF_HEIGHT = 35;
    public static final double TOOLBAR_BUTTON_HEIGHT = 25;
    public static final double TOOLBAR_BUTTON_WIDTH_DIVISOR = 2.39;

    public static final double TOOLBAR_BUTTON_FONT_SIZE = 11.15;
    private static ColorSelectionWindow instance;
    private final List<SpeciesRecordInterface> speciesRecordInterfaceList;
    private final Map<SpeciesRecordInterface, SpeciesColors> analysisMapOfSpeciesToColors;
    private final Map<SpeciesRecordInterface, SpeciesColors> sessionDefaultMapOfSpeciesToColors;
    private final Stack<SpeciesColorSetting> previousSpeciesColorSettingsStack;
    private final VBox root;
    private Stage stage;
    private ColorPicker colorPicker;
    private Label colorPickerLabel;// To change the text of the color picker
    private SpeciesColorRowSelectionRecord speciesColorRowSelectionRecord;
    private SpeciesColorPane[] speciesColorPanes;
    private Button undoButton;
    private Button saveAsSessionDefaultButton;
    private Button saveAsUserDefaultButton;
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
            SpeciesRecordInterface speciesRecordInterface = speciesRecordInterfaceList.get(index);
            SpeciesColors speciesColors =
                    analysisMapOfSpeciesToColors.remove(speciesRecordInterface);
            analysisMapOfSpeciesToColors.put(speciesRecordInterface,
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
            Map<SpeciesRecordInterface, SpeciesColors> analysisMapOfSpeciesToColors,
            List<SpeciesRecordInterface> species,
            Map<SpeciesRecordInterface, SpeciesColors> sessionDefaultMapOfSpeciesToColors,
            int indexOfFirstCheckedSpecies,
            Window owner,
            DelegateActionSet rebuildDelegateActionSet) {
        if (instance == null) {
            instance = new ColorSelectionWindow(
                    analysisMapOfSpeciesToColors,
                    species,
                    sessionDefaultMapOfSpeciesToColors,
                    indexOfFirstCheckedSpecies,
                    owner,
                    rebuildDelegateActionSet);
        }
        instance.centerOverOwner();
        return instance;
    }
    private ColorSelectionWindow(Map<SpeciesRecordInterface, SpeciesColors> analysisMapOfSpeciesToColors,
                                 List<SpeciesRecordInterface> species,
                                 Map<SpeciesRecordInterface, SpeciesColors> sessionDefaultMapOfSpeciesToColors,
                                 int indexOfFirstCheckedSpecies,
                                 Window owner,
                                 DelegateActionSet rebuildDelegateActionSet) {
        this.analysisMapOfSpeciesToColors = analysisMapOfSpeciesToColors;
        this.sessionDefaultMapOfSpeciesToColors = sessionDefaultMapOfSpeciesToColors;
        this.previousSpeciesColorSettingsStack = new Stack<>();
        this.speciesRecordInterfaceList = species;
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
                new SpeciesColorSetting(indexOfFirstCheckedSpecies,
                        analysisMapOfSpeciesToColors.get(speciesRecordInterfaceList.get(indexOfFirstCheckedSpecies))));
        speciesColorRowSelectionRecord.speciesColorRow().highlight();
        speciesColorRowSelectionRecord.speciesColorPane().highlight();
        this.root.getChildren().add(initColorPicker());
        Region topSpacerLeft = new Region();
        Region bottomSpacerLeft = new Region();
        Region topSpacerRight = new Region();
        Region bottomSpacerRight = new Region();
        HBox.setHgrow(topSpacerLeft, Priority.ALWAYS);
        HBox.setHgrow(bottomSpacerLeft, Priority.ALWAYS);
        HBox.setHgrow(topSpacerRight, Priority.ALWAYS);
        HBox.setHgrow(bottomSpacerRight, Priority.ALWAYS);
        ToolBar topToolBar = new ToolBar(
                topSpacerLeft,
                initSaveAsUserDefaultButton(),
                initSaveAsSessionDefaultButton(),
                topSpacerRight
        );
        ToolBar lowerToolBar = new ToolBar(
                bottomSpacerLeft,
                initUndoButton(),
                initResetToSessionDefaultsButton(),
                bottomSpacerRight
        );
        topToolBar.prefWidthProperty().bind(stage.widthProperty());
        topToolBar.setPadding(new Insets(10));
        lowerToolBar.prefWidthProperty().bind(stage.widthProperty());
        lowerToolBar.setPadding(new Insets(10));
        this.root.getChildren().add(topToolBar);
        this.root.getChildren().add(lowerToolBar);
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
                        speciesIndex, analysisMapOfSpeciesToColors.get(
                                speciesRecordInterfaceList.get(speciesIndex))));
    }


    private void resetToSessionDefault(){
        previousSpeciesColorSettingsStack.clear();
        undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        analysisMapOfSpeciesToColors.putAll(sessionDefaultMapOfSpeciesToColors);
        for (int speciesIndex = 0; speciesIndex < speciesColorPanes.length; ++speciesIndex) {
            SpeciesColors speciesColors = analysisMapOfSpeciesToColors.get(
                    speciesRecordInterfaceList.get(speciesIndex));
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
        if (!previousSpeciesColorSettingsStack.empty()){
            SpeciesColorSetting previousSpeciesColorSetting = previousSpeciesColorSettingsStack.pop();
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
            analysisMapOfSpeciesToColors.put(
                    speciesRecordInterfaceList.get(previousSpeciesColorSetting.index()),
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


    private void saveAsSessionDefault() {
        sessionDefaultMapOfSpeciesToColors.putAll(analysisMapOfSpeciesToColors);
    }

    private void saveAsUserDefault() {
        try{
            TripoliPersistentState.getExistingPersistentState().getMapOfSpeciesToColors().putAll(analysisMapOfSpeciesToColors);
            TripoliPersistentState.getExistingPersistentState().updateTripoliPersistentState();
        } catch (TripoliException ex) {
            TripoliMessageDialog.showWarningDialog(ex.getMessage(), TripoliGUI.primaryStage);
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

    /**
     * @return The initialized "Reset To Session Default" button
     */
    private Button initResetToSessionDefaultsButton() {
        Button resetButton = new Button("Reset To Session Defaults");
        resetButton.setFont(new Font(TOOLBAR_BUTTON_FONT_SIZE));
        resetButton.prefWidthProperty().bind(stage.widthProperty().divide(TOOLBAR_BUTTON_WIDTH_DIVISOR));
        resetButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        resetButton.setOnAction(resetChanges ->
            resetToSessionDefault());
        return resetButton;
    }

    private Button initUndoButton() {
        this.undoButton = new Button("Undo");
        undoButton.setFont(new Font(TOOLBAR_BUTTON_FONT_SIZE));
        undoButton.prefWidthProperty().bind(stage.widthProperty().divide(TOOLBAR_BUTTON_WIDTH_DIVISOR));
        undoButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        undoButton.setOnAction(undoLastChange -> {
            undo();
            undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
            undoLastChange.consume();
        });
        undoButton.setDisable(previousSpeciesColorSettingsStack.empty());
        return undoButton;
    }

    private Button initSaveAsSessionDefaultButton() {
        saveAsSessionDefaultButton = new Button("Save As Session Default");
        saveAsSessionDefaultButton.setFont(new Font(TOOLBAR_BUTTON_FONT_SIZE));
        saveAsSessionDefaultButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        saveAsSessionDefaultButton.prefWidthProperty().bind(stage.widthProperty().divide(TOOLBAR_BUTTON_WIDTH_DIVISOR));
        saveAsSessionDefaultButton.setOnAction((saveAsSessionDefaultAction) -> saveAsSessionDefault());
//        saveAsSessionDefaultButton.setDisable(previousSpeciesColorSettingsStack.empty());
        return saveAsSessionDefaultButton;
    }

    private Button initSaveAsUserDefaultButton() {
        saveAsUserDefaultButton = new Button("Save As User Default");
        saveAsUserDefaultButton.setFont(new Font(TOOLBAR_BUTTON_FONT_SIZE));
        saveAsUserDefaultButton.setPrefHeight(TOOLBAR_BUTTON_HEIGHT);
        saveAsUserDefaultButton.prefWidthProperty().bind(stage.widthProperty().divide(TOOLBAR_BUTTON_WIDTH_DIVISOR));
        saveAsUserDefaultButton.setOnAction((saveAsUserDefaultAction) -> saveAsUserDefault());
//        saveAsUserDefaultButton.setDisable(previousSpeciesColorSettingsStack.empty());
        return saveAsUserDefaultButton;
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
//            saveAsSessionDefaultButton.setDisable(previousSpeciesColorSettingsStack.empty());
            setColorPickerLabelText();
        });
        //  TODO: Set colorPickerLabel text when user cancels
        return this.colorPicker;
    }
    private void initSpeciesColorPanes(List<SpeciesRecordInterface> species) {
        speciesColorPanes = new SpeciesColorPane[species.size()];
        for (int i = 0; i < species.size(); ++i) {
            speciesColorPanes[i] =
                    new SpeciesColorPane(i,
                            species.get(i).prettyPrintShortForm(),
                            analysisMapOfSpeciesToColors.get(species.get(i)));
            speciesColorPanes[i].prefWidthProperty().bind(stage.widthProperty());
            root.getChildren().add(speciesColorPanes[i]);
        }
    }

    private void centerOverOwner() {
        Window owner = stage.getOwner();
        double ownerX = owner.getX();
        double ownerY = owner.getY();
        double ownerWidth = owner.getWidth();
        double ownerHeight = owner.getHeight();

        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        double centerX = ownerX + (ownerWidth - stageWidth) / 2;
        double centerY = ownerY + (ownerHeight - stageHeight) / 2 ;

        stage.setX(centerX);
        stage.setY(centerY);

        // If the window is not on screen...
        if (!Screen.getScreens().isEmpty() && Screen.getScreensForRectangle(
                new Rectangle2D(
                        stage.getX(),
                        stage.getY(),
                        stage.getWidth(),
                        stage.getHeight())).isEmpty()) {
            // ... then put it in the middle of the screen by default
            Screen primaryScreen = Screen.getPrimary();
            stage.setX(primaryScreen.getBounds().getMinX() + (primaryScreen.getBounds().getWidth() - stageWidth)/2);
            stage.setY(primaryScreen.getBounds().getMinY() + (primaryScreen.getBounds().getHeight() - stageHeight)/2);
        }

    }
    private void initStage(Window owner) {
        stage = new Stage();
        Scene scene = new Scene(this.root);
        stage.setWidth(WINDOW_PREF_WIDTH);
        stage.setScene(scene);
        stage.initOwner(owner);
        owner.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            stage.fireEvent(new WindowEvent(owner, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
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
            stage.show();
        }
        setColorPickerLabelText();
        centerOverOwner();
        stage.toFront();
    }
}
