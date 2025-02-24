/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui.reports;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.cirdles.tripoli.reports.Report;
import org.cirdles.tripoli.reports.ReportCategory;
import org.cirdles.tripoli.reports.ReportDetails;

public class ReportBuilderController {

    @FXML
    private static VBox reportContainer;

    private static Report report;

    public static void setReport(Report report) {
        ReportBuilderController.report = report;
        generateUI();
    }
    public static void setContainer(VBox container) { reportContainer = container; }

    private static void generateUI() {
        if (report != null && report.getCategories() != null) {
            for (ReportCategory category : report.getCategories()) {

                // Category header
                Label categoryLabel = new Label(category.getCategoryName());
                categoryLabel.setStyle("-fx-font-weight: bold");
                reportContainer.getChildren().add(categoryLabel);

                GridPane gridpane = new GridPane();
                gridpane.setHgap(10);
                gridpane.setVgap(5);

                // Column Header
                int index = 0;
                for (ReportDetails columnDetail : category.getColumns()) {
                    Label columnName = new Label(columnDetail.getColumnName());
                    Label columnDetails = new Label(columnDetail.getColumnDetails());
                    gridpane.add(columnName, index, 0);
                    gridpane.add(columnDetails, index++, 1);
                }
                reportContainer.getChildren().add(gridpane);
            }
        }
    }

}
