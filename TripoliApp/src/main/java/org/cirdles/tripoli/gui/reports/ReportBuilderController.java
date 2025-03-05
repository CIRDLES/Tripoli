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


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.cirdles.tripoli.reports.Report;
import org.cirdles.tripoli.reports.ReportCategory;
import org.cirdles.tripoli.reports.ReportColumn;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ReportBuilderController {

    @FXML
    private VBox reportContainer;
    @FXML
    private ListView<ReportCategory> categoryListView;

    @FXML
    private ListView<ReportColumn> columnListView;

    private Report report;

    private ObservableList<ReportCategory> categories;
    private ObservableList<ReportColumn> columns;

    public ReportBuilderController() {
    }

    public void setReport(Report report) {
        this.report = report;
        initializeListViews();
    }

    private void initializeListViews() {
        categories = FXCollections.observableArrayList(report.getCategories());
        categoryListView.setItems(categories);

        // Report Category ListView ----------------------------------------------------->>>
        categoryListView.setCellFactory(lv -> {
            // Save fixed category for future reference
            final AtomicReference<ListCell<ReportCategory>> fixedCategoryCell = new AtomicReference<>();
            ListCell<ReportCategory> cell = new ListCell<>() {
                @Override
                protected void updateItem(ReportCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { // Empty Cell
                        setText(null);
                        setCursor(Cursor.DEFAULT);
                        setTooltip(null);
                    } else {
                        setText(item.getCategoryName());

                        // Check if the current item is the fixed category
                        if (Objects.equals(item.getCategoryName(), item.FIXED_CATEGORY_NAME)) {
                            setCursor(Cursor.DISAPPEAR); // Todo: make this a x or something
                            Tooltip tooltip = new Tooltip("This category cannot be moved.");
                            tooltip.setShowDelay(Duration.seconds(0.5));
                            setTooltip(tooltip);
                            fixedCategoryCell.set(this);
                        } else { // Other category
                            setCursor(Cursor.CLOSED_HAND);
                            setTooltip(null);
                        }
                    }
                }
            };

            // Start drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty() && !Objects.equals(cell.getItem().getCategoryName(), cell.getItem().FIXED_CATEGORY_NAME)) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem().getCategoryName());
                    db.setContent(content);

                    categoryListView.setUserData(cell.getItem()); // Store dragged item
                    event.consume();
                }

            });

            // Accept drag over the cell
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Handle drop
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ReportCategory draggedItem = (ReportCategory) categoryListView.getUserData();

                    if (draggedItem != null) {
                        int dropIndex = cell.getIndex();
                        if (fixedCategoryCell.get() != null && dropIndex <= fixedCategoryCell.get().getIndex()) {
                            event.consume();
                            return;
                        }
                        categoryListView.getItems().remove(draggedItem);
                        categoryListView.getItems().add(dropIndex, draggedItem);
                        report.updateCategoryPosition(draggedItem, dropIndex);

                        success = true;
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });

        // Add listener to update columns when a category is selected
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Set columns based on the selected category
                columns = FXCollections.observableArrayList(newSelection.getColumns());
                columnListView.setItems(columns);
            }
        });

        // Select the first category if available
        if (!categories.isEmpty()) {
            categoryListView.getSelectionModel().selectFirst();
        }
        // <<<---------------------------------------------- Category End

        // Report Column ListView ------------------------------------------------->>>
        columnListView.setCellFactory(lv -> {
            ListCell<ReportColumn> cell = new ListCell<>() {
                @Override
                protected void updateItem(ReportColumn item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getColumnName());
                }
            };

            // Start drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem().getColumnName());
                    db.setContent(content);

                    columnListView.setUserData(cell.getItem()); // Store dragged item
                    event.consume();
                }
            });

            // Accept drag over the cell
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Handle drop
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ReportColumn draggedItem = (ReportColumn) columnListView.getUserData();

                    if (draggedItem != null) {
                        columnListView.getItems().remove(draggedItem);
                        int dropIndex = cell.getIndex();
                        columnListView.getItems().add(dropIndex, draggedItem);
                        categoryListView.getSelectionModel().getSelectedItem().updateColumnPosition(draggedItem, dropIndex);

                        success = true;
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });
    }
    // <<<---------------------------------------------- Column End

}


