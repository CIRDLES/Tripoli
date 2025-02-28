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


import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
            // Create the master TableView
            TableView<Report> parentTable = new TableView<>();
            parentTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            boolean firstCat  = true;
            // Create a single row with multiple category columns
            for (ReportCategory category : report.getCategories()) {
                TableColumn<Report, ReportCategory> categoryCol = new TableColumn<>(category.getCategoryName());
                categoryCol.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: red;");
                if (firstCat) {
                    categoryCol.setReorderable(false);
                    firstCat = false;
                }

                for (ReportDetails column : category.getColumns()) {
                    TableColumn<Report, String> detailsCol = new TableColumn<>(column.getColumnName());

                    detailsCol.setCellValueFactory(data -> {
                        ReportCategory matchingCategory = report.getCategories()
                                .stream()
                                .filter(cat -> cat.getCategoryName().equals(category.getCategoryName()))
                                .findFirst()
                                .orElse(null);

                        if (matchingCategory != null) {
                            ReportDetails matchingDetail = matchingCategory.getColumns()
                                    .stream()
                                    .filter(detail -> detail.getColumnName().equals(column.getColumnName()))
                                    .findFirst()
                                    .orElse(null);

                            return new SimpleStringProperty(matchingDetail != null ? matchingDetail.getColumnValue() : "");
                        }
                        return new SimpleStringProperty("");
                    });

                    categoryCol.getColumns().add(detailsCol);
                }

                parentTable.getColumns().add(categoryCol);
            }

            // Add a single row for the entire report
            parentTable.getItems().add(report);

            // Wrap the TableView in a ScrollPane
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(parentTable);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            reportContainer.getChildren().add(scrollPane);
        }
    }


}
