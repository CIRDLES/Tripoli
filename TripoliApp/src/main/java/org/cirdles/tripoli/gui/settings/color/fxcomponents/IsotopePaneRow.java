package org.cirdles.tripoli.gui.settings.color.fxcomponents;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class IsotopePaneRow extends HBox {

    private static final double TITLE_WIDTH = 124;
    private static final double PADDING = 10;
    private static final double COLOR_WIDTH = 90;
    // The padding is
    private final Label title;
    private final ColorPickerSplotch faradayData;
    private final ColorPickerSplotch pmData;
    private final ColorPickerSplotch faradayModel;
    private final ColorPickerSplotch pmModel;

    public IsotopePaneRow(String speciesName) {
        // Initialize all components
        this.title = new Label(speciesName);
        this.faradayData = new ColorPickerSplotch();
        this.pmData = new ColorPickerSplotch();
        this.faradayModel = new ColorPickerSplotch();
        this.pmModel = new ColorPickerSplotch();

        // Set some padding and spacing for the row
        this.setSpacing(10); // Adjust as needed
        this.setPadding(new Insets(5, 5, 5, 5));

        // Create flexible Region spacers
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        Region spacer3 = new Region();
        Region spacer4 = new Region();

        // Ensure spacers expand to fill available space
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox.setHgrow(spacer4, Priority.ALWAYS);

        // Add components and spacers to the HBox
        this.getChildren().addAll(
                title,
                spacer1,
                faradayData,
                pmData,
                spacer2,
                faradayModel,
                pmModel
        );

        // Bind the width of ColorPickerSplotch components to fit proportionally
        this.getChildren().forEach(item -> {
            if (item instanceof ColorPickerSplotch) {
                ColorPickerSplotch splotch = (ColorPickerSplotch) item;
                splotch.prefWidthProperty().bind(this.widthProperty().multiply(0.15)); // 15% width for each splotch
                splotch.prefHeightProperty().bind(this.heightProperty().multiply(1)); // Full height
            }
        });

        // Bind the title width to take up more space as needed
        title.prefWidthProperty().bind(this.widthProperty().multiply(0.25)); // 25% width for the title
    }
}
