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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.gui.utilities.fileUtilities.FileHandlerUtil;
import org.cirdles.tripoli.reports.Report;
import org.cirdles.tripoli.reports.ReportCategory;
import org.cirdles.tripoli.reports.ReportColumn;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ReportBuilderController {

    @FXML
    public Button createCategoryButton;
    @FXML
    public TextField categoryTextField;
    @FXML
    public Button newButton;
    @FXML
    public Button copyButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button restoreButton;
    @FXML
    public Button renameButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button exportButton;
    @FXML
    public Button importButton;
    @FXML
    private ListView<ReportCategory> categoryListView;
    @FXML
    private ListView<ReportColumn> columnListView;
    private Stage reportStage;


    private Report currentReport;
    private Report initalReport;

    private ObservableList<ReportCategory> categories;
    private ObservableList<ReportColumn> columns;

    boolean unsavedChanges;

    public ReportBuilderController() {
    }

    public void setStage(Stage stage) {
        reportStage = stage;
        reportStage.setOnCloseRequest(event -> {
            if (!proceedWithUnsavedDialog()) { event.consume(); }
        });

    }

    public void setCurrentReport(Report currentReport) {
        this.currentReport = currentReport;
        this.initalReport = new Report(currentReport);
        saveButton.setDisable(false);
        restoreButton.setDisable(false);
        renameButton.setDisable(false);
        // Cannot rename or delete the default report
        if(!"Default Report".equals(currentReport.getReportName())){
            deleteButton.setDisable(false);
        }
        exportButton.setDisable(false);
        initializeListViews();
        reportStage.setTitle("Report Builder - " + currentReport.getReportName());
    }

    private void initializeListViews() {
        categories = FXCollections.observableArrayList(currentReport.getCategories());
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
                        setGraphic(new Text(item.getCategoryName()));
                        handleVisible(this, false);

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
                        currentReport.updateCategoryPosition(draggedItem, dropIndex);
                        handleTrackingChanges();

                        success = true;
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });
            // Add a mouse click listener to handle double-click
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    handleVisible(cell, true);
                }
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
                    setGraphic(empty || item == null ? null : new Text(item.getColumnName()));
                    handleVisible(this,false);
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
                        handleTrackingChanges();

                        success = true;
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });
            // Add a mouse click listener to handle double-click
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    handleVisible(cell, true);
                }
            });

            return cell;
        });
    }
    // <<<---------------------------------------------- Column End

    /**
     * Handle all visiblity calls for the ListViews and Report classes
     * @param cell ListCell to be adjusted
     * @param toggle Whether to toggle visibility
     * @param <T> Must be ReportCategory or ReportColumn type
     */
    private <T> void handleVisible(ListCell<T> cell, Boolean toggle) {
        if (cell.getItem() != null) {
            Object item = cell.getItem();
            boolean isVisible = (item instanceof ReportColumn) ? ((ReportColumn) item).isVisible() : ((ReportCategory) item).isVisible();

            boolean newVisibility = toggle != isVisible;
            Color textColor = newVisibility ? Color.BLACK : Color.GRAY;
            boolean strikethrough = !newVisibility;

            setItemVisibilityAndStyle(cell, item, newVisibility, textColor, strikethrough);
            handleTrackingChanges();
        }
    }

    private void setItemVisibilityAndStyle(ListCell<?> cell, Object item, boolean visible, Color textColorStyle, boolean strikethrough) {
        if (item instanceof ReportColumn) {
            ((ReportColumn) item).setVisible(visible);
        } else if (item instanceof ReportCategory) {
            ((ReportCategory) item).setVisible(visible);
        }

        if (cell.getGraphic() instanceof Text) {
            Text text = new Text(((Text) cell.getGraphic()).getText());
            text.setFill(textColorStyle);
            text.setStrikethrough(strikethrough);

            cell.setGraphic(text);
        }

    }


    public void newOnAction() {

        //report.reset();
    }

    public void copyOnAction() {
    }

    public void saveOnAction() throws TripoliException {
        if (currentReport.getReportName() == null){
            TripoliMessageDialog.showWarningDialog("Report must have a name", reportStage);
        } else if (Objects.equals(currentReport.getReportName(), "Default Report")){
            TripoliMessageDialog.showWarningDialog("Report name: 'Default Report' is restricted", reportStage);
        } else {
            TripoliMessageDialog.showSavedAsDialog(currentReport.serializeReport(), reportStage);
            initalReport = new Report(currentReport); // Reset saved state
            handleTrackingChanges();
        }
    }

    public void restoreOnAction() {
        if (proceedWithUnsavedDialog()) {
            currentReport = new Report(initalReport);
            initializeListViews();
        }
    }

    public void renameOnAction() {
        TextInputDialog dialog = new TextInputDialog(currentReport.getReportName());
        dialog.setTitle("Rename Report");
        dialog.setHeaderText("Current Report: '" + currentReport.getReportName()+"'");
        dialog.setContentText("Enter new report name: ");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                currentReport.setReportName(newName.trim());
                handleTrackingChanges();
            }
        });
    }

    public void deleteOnAction() {
        if (!currentReport.getTripoliReportFile().exists()){
            TripoliMessageDialog.showWarningDialog("Report does not exist!", reportStage);
            return;
        }
        if (TripoliMessageDialog.showChoiceDialog("Are you sure? \n Delete Report: "+ currentReport.getReportName() + "?", reportStage)){
            if (currentReport.deleteReport()){
                TripoliMessageDialog.showInfoDialog("Report Deleted!", reportStage);
            }
        }
    }

    public void exportOnAction() {
        // for future implementation
    }

    public void importOnAction() throws TripoliException {
        if (!proceedWithUnsavedDialog()) { return; }

        File reportFile = FileHandlerUtil.importReportFile(reportStage);
        if (reportFile == null) { return; }

        Report newReport;
        try {
            newReport = (Report) TripoliSerializer.getSerializedObjectFromFile(reportFile.getAbsolutePath(), true);
        } catch (TripoliException e) {
            TripoliMessageDialog.showWarningDialog("Not a valid .trf file!", reportStage);
            return;
        }
        if (!newReport.getMethodName().equals(currentReport.getMethodName())){
            if (TripoliMessageDialog.showChoiceDialog("Cannot open report of different Method right now. Would you like to save report to your local directory?", reportStage)){
                newReport.serializeReport();
            }
        } else {
            currentReport = newReport;
            initalReport = new Report(newReport);
            initializeListViews();
            handleTrackingChanges();
        }
    }

    public void createCategoryOnAction(ActionEvent actionEvent) {
        String categoryName = categoryTextField.getText();
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            ReportCategory newCategory = new ReportCategory(categoryName, categories.size());
            categories.add(newCategory);
            currentReport.addCategory(newCategory);
            handleTrackingChanges();
        }
    }

    private void handleTrackingChanges() {
        if (!currentReport.equals(initalReport)) {
            unsavedChanges = true;
            reportStage.setTitle("Report Builder - " + currentReport.getReportName() + "*");
        } else {
            unsavedChanges = false;
            reportStage.setTitle("Report Builder - " + currentReport.getReportName());
        }
    }
    private boolean proceedWithUnsavedDialog(){
        if (unsavedChanges){
            return TripoliMessageDialog.showChoiceDialog("Unsaved changes exist! Are you sure?", reportStage);
        }
        return true;
    }
}


