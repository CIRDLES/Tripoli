package org.cirdles.tripoli.gui.settings.color.fxcomponents;


import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class SpeciesIntensityColorSelectionScrollPane extends ScrollPane {

    private final GridPane gridPane;

    public SpeciesIntensityColorSelectionScrollPane() {
        // Initialize the GridPane
        gridPane = new GridPane();

        // Add column headers
        addHeaders();

        // Example of adding a species row with ColorPickerSplotch
        addSpeciesRow("204Pb", new ColorPickerSplotch(), new ColorPickerSplotch(), new ColorPickerSplotch(), new ColorPickerSplotch());

        // Set the content of the ScrollPane
        this.setContent(gridPane);
        this.setFitToWidth(true); // Makes the ScrollPane width match its content
    }

    // Method to add column headers
    private void addHeaders() {
        gridPane.add(new Label("Species"), 0, 0);
        gridPane.add(new Label("Faraday Data"), 1, 0);
        gridPane.add(new Label("PM Data"), 2, 0);
        gridPane.add(new Label("Faraday Model"), 3, 0);
        gridPane.add(new Label("PM Model"), 4, 0);

        // Ensure consistent column widths
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20); // Adjust width percentage as needed
        gridPane.getColumnConstraints().addAll(col1, col1, col1, col1, col1);
    }

    // Method to add a species row using ColorPickerSplotch
    private void addSpeciesRow(String species, ColorPickerSplotch faradayData, ColorPickerSplotch pmData, ColorPickerSplotch faradayModel, ColorPickerSplotch pmModel) {
        int row = gridPane.getRowCount(); // Get the current row count to add the new row

        // Add the species label
        gridPane.add(new Label(species), 0, row);

        // Add ColorPickerSplotch elements for each data type
        gridPane.add(faradayData, 1, row);
        gridPane.add(pmData, 2, row);
        gridPane.add(faradayModel, 3, row);
        gridPane.add(pmModel, 4, row);
    }
}

