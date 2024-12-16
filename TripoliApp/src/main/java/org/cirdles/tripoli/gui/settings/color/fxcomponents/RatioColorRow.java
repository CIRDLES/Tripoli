package org.cirdles.tripoli.gui.settings.color.fxcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import static org.cirdles.tripoli.constants.TripoliConstants.RatiosPlotColorFlavor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.settings.plots.RatiosColors;

public class RatioColorRow extends HBox {
    private static final double TITLE_WIDTH = 260;
    private static final double PADDING = 81;
    private static final double COLOR_WIDTH = 140;


    private final RatiosPlotColorFlavor plotColorFlavor;
    private final ObjectProperty<Color> colorObjectProperty;


    public RatioColorRow (
            RatiosColors ratiosColors,
            RatiosPlotColorFlavor plotColorFlavor
    ) {
        Label title = new Label(plotColorFlavor.getName());
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        title.setPrefWidth(TITLE_WIDTH);
        title.setAlignment(Pos.CENTER);
        getChildren().add(title);

        Region spacer = new Region();
        spacer.setPrefWidth(PADDING);
        getChildren().add(spacer);


        this.colorObjectProperty = new SimpleObjectProperty<>(
                Color.web(ratiosColors.get(plotColorFlavor))
        );
        this.plotColorFlavor = plotColorFlavor;
        ColorPickerSplotch colorPickerSplotch = new ColorPickerSplotch();
        colorPickerSplotch.colorProperty().set(colorObjectProperty.get());
        colorPickerSplotch.colorProperty().bindBidirectional(colorObjectProperty);
        colorPickerSplotch.setPrefWidth(COLOR_WIDTH);
        colorPickerSplotch.prefHeightProperty().bind(prefHeightProperty());
        getChildren().add(colorPickerSplotch);

    }

    public RatiosPlotColorFlavor getPlotColorFlavor() {
        return plotColorFlavor;
    }

    public Color getColor() {
        return colorObjectProperty.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return colorObjectProperty;
    }
}
