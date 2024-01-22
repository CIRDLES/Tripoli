package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.species.SpeciesColors;

public class SpeciesColorPane extends Pane {

    private static final double TITLE_FONT_SIZE = 19;

    private String speciesName;
    private SpeciesColors speciesColors;
    private ColorPicker colorPicker;

    public SpeciesColorPane(String speciesName, SpeciesColors speciesColors) {
        super();
        this.speciesName = speciesName;
        this.speciesColors = speciesColors;
        this.colorPicker = new ColorPicker(Color.web(speciesColors.faradayHexColor()));
        VBox vBox = initializeAndAddVbox();
        vBox.getChildren().add(initializeAndAddHbox());
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
//                System.out.println(event.getSource());
//                System.out.println(event.getPickResult());
//                System.out.println(event);
                System.out.println(((Node) event.getTarget()).getParent());
                event.consume();
            }
        });
    }

    private HBox initializeAndAddHbox() {
        HBox hBox = new HBox();
        VBox vBox = new VBox();
        hBox.getChildren().add(vBox);
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_POINT,
                speciesColors.faradayHexColor()
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_POINT,
                speciesColors.pmHexColor()
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.FARADAY_MODEL,
                speciesColors.faradayModelHexColor()
        ));
        vBox.getChildren().add(new ColorRow(
                TripoliConstants.DetectorPlotFlavor.PM_MODEL,
                speciesColors.pmModelHexColor()
        ));
        hBox.getChildren().add(colorPicker);
        colorPicker.setPrefHeight(ColorRow.ROW_HEIGHT * 4);
        colorPicker.setStyle("-fx-text-fill:"  + TripoliConstants.DetectorPlotFlavor.FARADAY_POINT);
        return hBox;
    }

    private VBox initializeAndAddVbox() {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Text title = new Text(speciesName);
        title.setFont(Font.font(TITLE_FONT_SIZE));
        vBox.getChildren().add(title);
        this.getChildren().add(vBox);
        return vBox;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public SpeciesColors getSpeciesColors() {
        return speciesColors;
    }

    public void setSpeciesColors(SpeciesColors speciesColors) {
        this.speciesColors = speciesColors;
    }
}
