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
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.reports.Report;
import org.cirdles.tripoli.reports.ReportCategory;
import org.cirdles.tripoli.reports.ReportColumn;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.cirdles.tripoli.gui.AnalysisManagerController.analysis;
import static org.cirdles.tripoli.gui.SessionManagerController.tripoliSession;
import static org.cirdles.tripoli.gui.TripoliGUI.primaryStage;

public class ReportBuilderController {

    @FXML
    public Button createCategoryButton;
    @FXML
    public TextField categoryTextField;
    @FXML
    public Button saveButton;
    @FXML
    public Button restoreButton;
    @FXML
    public Button renameButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button generateButton;
    @FXML
    public MenuItem newBlankButton;
    @FXML
    public MenuItem newFullButton;
    @FXML
    public Accordion columnAccordion;
    @FXML
    public TextArea columnDetailsTextArea;
    @FXML
    public TextField reportNameTextField;
    @FXML
    public TextField methodNameTextField;
    @FXML
    private ListView<ReportCategory> categoryListView;
    @FXML
    private ListView<ReportColumn> columnListView;

    private Stage reportStage;

    private Report currentReport;
    private Report initalReport;

    private ObservableList<ReportCategory> categories;
    private ObservableList<ReportColumn> columns;
    private List<AnalysisInterface> listOfAnalyses;

    boolean unsavedChanges;

    public ReportBuilderController() {
        listOfAnalyses = new ArrayList<>();
    }

    public static void loadReportBuilder(Report report){
        try {
            FXMLLoader loader = new FXMLLoader(ReportBuilderController.class.getResource("/org/cirdles/tripoli/gui/reports/ReportBuilder.fxml"));
            Parent root = loader.load();
            ReportBuilderController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Report Builder");
            stage.setScene(new Scene(root));
            stage.setX(primaryStage.getX() + (primaryStage.getWidth() - 875) / 2);
            stage.setY(primaryStage.getY() + (primaryStage.getHeight() - 650) / 2);
            controller.setStage(stage);
            controller.setCurrentReport(report);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        reportStage = stage;
        reportStage.setOnCloseRequest(event -> {
            if (!proceedWithUnsavedDialog()) { event.consume(); }
        });
        populateAccordion();
        listOfAnalyses.addAll(tripoliSession.getMapOfAnalyses().values());
    }

    public void setCurrentReport(Report currentReport) {
        this.currentReport = currentReport;
        this.initalReport = new Report(currentReport);
        saveButton.setDisable(false);
        restoreButton.setDisable(false);
        renameButton.setDisable(false);
        deleteButton.setDisable(false);
        categoryListView.setDisable(false);
        columnListView.setDisable(false);
        generateButton.setDisable(false);
        createCategoryButton.setDisable(false);
        categoryTextField.setDisable(false);
        columnAccordion.setDisable(false);

        // Remove editing for the full report
        if(currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())){
            createCategoryButton.setDisable(true);
            categoryTextField.setDisable(true);
            deleteButton.setDisable(true);
            renameButton.setDisable(true);
            restoreButton.setDisable(true);
            saveButton.setDisable(true);
            columnAccordion.setDisable(true);
        }
        initializeListViews();
        reportStage.setTitle("Report Builder - " + currentReport.getReportName());
        reportNameTextField.setText(currentReport.getReportName());
        methodNameTextField.setText(currentReport.getMethodName());
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
                        setGraphic(null);
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
                        } else if(currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                            setCursor(Cursor.DISAPPEAR); // Todo: make this a x or something
                            Tooltip tooltip = new Tooltip("Editing of " + currentReport.FIXED_REPORT_NAME + " is not allowed");
                            tooltip.setShowDelay(Duration.seconds(0.1));
                            setTooltip(tooltip);
                        } else { // Other category
                            setCursor(Cursor.CLOSED_HAND);
                            setTooltip(null);
                        }
                    }
                }
            };
            // Start drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()
                        && !Objects.equals(cell.getItem().getCategoryName(), cell.getItem().FIXED_CATEGORY_NAME)
                        && !currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
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
                if (event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() == cell.getListView() // Ensure same ListView
                        && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Handle drop
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()
                        && event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() == cell.getListView()) {
                    ReportCategory draggedItem = (ReportCategory) categoryListView.getUserData();

                    if (draggedItem != null) {
                        int dropIndex = cell.getIndex();
                        dropIndex = Math.min(dropIndex, categories.size()-1);
                        if (fixedCategoryCell.get() != null && dropIndex <= fixedCategoryCell.get().getIndex()) {
                            event.consume();
                            return;
                        }
                        categories.remove(draggedItem);
                        categories.add(dropIndex, draggedItem);
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
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !cell.isEmpty()
                        && !cell.getItem().FIXED_CATEGORY_NAME.equals(cell.getItem().getCategoryName())
                        && !currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                    handleVisible(cell, true);
                }
            });

                ContextMenu contextMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("Remove Category");
                removeItem.setOnAction(event -> {
                    ReportCategory item = cell.getItem();
                    if (item != null) {
                        categories.remove(item);
                        currentReport.removeCategory(item);
                        handleTrackingChanges();
                    }
                });
                contextMenu.getItems().add(removeItem);
                cell.setContextMenu(contextMenu);

                cell.setOnContextMenuRequested(event -> {
                    if (!cell.isEmpty()
                            && cell.getItem() != null
                            && !cell.getItem().FIXED_CATEGORY_NAME.equals(cell.getItem().getCategoryName())) {
                        contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                    } else {
                        contextMenu.hide();
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

                if (!columns.isEmpty()) {
                    columnListView.getSelectionModel().selectFirst();
                }
            }
        });

        // Select the first category if available
        if (!categories.isEmpty()) {
            categoryListView.getSelectionModel().selectFirst();
        }
        // <<<---------------------------------------------- Category End

        // Report Column ListView ------------------------------------------------->>>
        final AtomicReference<ListCell<ReportColumn>> fixedColumnCell = new AtomicReference<>();
        columnListView.setCellFactory(lv -> {
            ListCell<ReportColumn> cell = new ListCell<>() {

                @Override
                protected void updateItem(ReportColumn item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setCursor(Cursor.DEFAULT);
                    setTooltip(null);
                } else {
                    setGraphic(new Text(item.getColumnName()));
                    handleVisible(this, false);

                    if (Objects.equals(item.getColumnName(), item.FIXED_COLUMN_NAME)) {
                        setCursor(Cursor.DISAPPEAR); // Prevent movement
                        Tooltip tooltip = new Tooltip("This Column cannot be moved.");
                        tooltip.setShowDelay(Duration.seconds(0.5));
                        setTooltip(tooltip);
                        fixedColumnCell.set(this);
                    } else if (Objects.equals(currentReport.getReportName(), currentReport.FIXED_REPORT_NAME)) {
                        setCursor(Cursor.DISAPPEAR);
                        Tooltip tooltip = new Tooltip("Editing of " + currentReport.FIXED_REPORT_NAME + " is not allowed");
                        tooltip.setShowDelay(Duration.seconds(0.1));
                        setTooltip(tooltip);
                    } else {
                        setCursor(Cursor.CLOSED_HAND);
                        setTooltip(null);
                    }
                }
                }
            };

            // Start drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()
                        && !cell.getItem().FIXED_COLUMN_NAME.equals(cell.getItem().getColumnName())
                        && !currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem().getColumnName());
                    db.setContent(content);

                    columnListView.setUserData(cell.getItem());
                    event.consume();
                }
            });

            // Accept drag over the cell
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() != categoryListView
                        && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // Handle drop
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()
                        && event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() != categoryListView) {
                    ReportColumn draggedItem = (ReportColumn) sourceCell.getListView().getUserData();

                    if (draggedItem != null) {
                        int dropIndex = cell.getIndex();
                        if (sourceCell.getListView() == columnListView){
                            dropIndex = Math.min(dropIndex, columns.size()-1);
                        } else {
                            dropIndex = Math.min(dropIndex, columns.size());
                        }

                        if (fixedColumnCell.get() != null
                                && categoryListView.getSelectionModel().isSelected(0)
                                && dropIndex <= fixedColumnCell.get().getIndex()) {
                            event.consume();
                            return;
                        }

                        if (sourceCell.getListView() != columnListView){
                            ReportColumn draggedItemCopy = new ReportColumn(draggedItem);
                            categoryListView.getSelectionModel().getSelectedItem().insertColumnAtPosition(draggedItemCopy, dropIndex);
                            columns.add(dropIndex, draggedItemCopy);
                        } else{
                            columns.remove(draggedItem);
                            columns.add(dropIndex, draggedItem);
                            categoryListView.getSelectionModel().getSelectedItem().updateColumnPosition(draggedItem, dropIndex);
                        }
                        handleTrackingChanges();

                        success = true;
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });
            // Add a mouse click listener to handle double-click
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                        && !cell.isEmpty()
                        && !cell.getItem().FIXED_COLUMN_NAME.equals(cell.getItem().getColumnName())
                        && !currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                    handleVisible(cell, true);
                }
            });

            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeItem = new MenuItem("Remove Column");
            removeItem.setOnAction(event -> {
                ReportColumn item = cell.getItem();
                if (item != null) {
                    columns.remove(item);
                    categoryListView.getSelectionModel().getSelectedItem().removeColumn(item);
                    handleTrackingChanges();
                }
            });
            contextMenu.getItems().add(removeItem);
            cell.setContextMenu(contextMenu);

            cell.setOnContextMenuRequested(event -> {
                if (!cell.isEmpty()
                        && cell.getItem() != null
                        && !cell.getItem().FIXED_COLUMN_NAME.equals(cell.getItem().getColumnName())) {
                    contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                }else {
                    contextMenu.hide();
                }
            });

            return cell;
        });
        // Enable drag-over for the entire ListView (even if empty)
        columnListView.setOnDragOver(event -> {
            if (event.getGestureSource() != columnListView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Handle drop events on the empty ListView
        columnListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && event.getGestureSource() instanceof ListCell<?> sourceCell) {
                ReportColumn draggedItem = (ReportColumn) sourceCell.getListView().getUserData();

                if (draggedItem != null) {
                    // Add the dragged item at the first position if the list is empty
                    ReportColumn draggedItemCopy = new ReportColumn(draggedItem);
                    categoryListView.getSelectionModel().getSelectedItem().insertColumnAtPosition(draggedItemCopy, 0);
                    columns.add(0, draggedItem);
                    handleTrackingChanges();
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        columnListView.getSelectionModel().selectedItemProperty().addListener((obs, oldColumn, newColumn) -> {
            if (newColumn != null) {
                // Update details based on the selected column
                columnDetailsTextArea.setText(formatColumnDetails(newColumn));
            }
        });
    }
    // <<<---------------------------------------------- Column End

    private String formatColumnDetails(ReportColumn column) {
        StringBuilder result = new StringBuilder(column.getColumnName() + "\n");

        listOfAnalyses.stream()
                .filter(Analysis.class::isInstance)
                .map(Analysis.class::cast)
                .filter(analysis -> analysis.getAnalysisMethod().getMethodName().equals(currentReport.getMethodName())) // Filter by method name
                .forEach(analysis -> {
                    AllBlockInitForDataLiteOne.initBlockModels(analysis); // Init values
                    result.append(column.retrieveData(analysis)).append("\n");
                });

        return result.toString();
    }

    private void populateAccordion() {
        Report accReport = Report.createFullReport("", analysis.getAnalysisMethod().getMethodName(), analysis.getUserFunctions());
        Set<ReportCategory> repoCat  = accReport.getCategories();

        /**
         * Temporary Data inserted for future custom expressions
         */
        Set<ReportColumn> custCol = new TreeSet<>();
        custCol.add(new ReportColumn("Alpha", 0, null));
        custCol.add(new ReportColumn("Beta", 1, null));
        custCol.add(new ReportColumn("Gamma", 2, null));
        repoCat.add(new ReportCategory("Custom Expressions", custCol, 3));

        for (ReportCategory cat : accReport.getCategories()) {
            ListView<ReportColumn> accColumnlv = new ListView<>();
            accColumnlv.getItems().addAll(cat.getColumns()); // Add columns to the ListView

            // Enable drag detection for moving columns
            accColumnlv.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(ReportColumn col, boolean empty) {
                    super.updateItem(col, empty);
                    if (empty || col == null) {
                        setText(null);
                    } else {
                        setText(col.getColumnName());
                        col.FIXED_COLUMN_NAME = "";

                        // Enable drag
                        setOnDragDetected(event -> {
                            if (!currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                                ClipboardContent content = new ClipboardContent();
                                content.putString(col.getColumnName());
                                db.setContent(content);

                                accColumnlv.setUserData(col);
                                event.consume();
                            }
                        });
                    }
                }
            });
            TitledPane accCat = new TitledPane(cat.getCategoryName(), accColumnlv);
            columnAccordion.getPanes().add(accCat);
        }

        if (!columnAccordion.getPanes().isEmpty()) {
            columnAccordion.setExpandedPane(columnAccordion.getPanes().get(0));
        }
    }
    /**
     * Handle all visibility calls for the ListViews and Report classes
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

    public void newOnAction(ActionEvent event) {
        boolean proceed = proceedWithUnsavedDialog();
        if (proceed) {
            TextInputDialog dialog = new TextInputDialog("Enter Report Name");
            dialog.setTitle("New Report");
            dialog.setHeaderText("Create a new Report");
            dialog.setContentText("Enter new report name: ");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                if (newName.trim().equals(currentReport.FIXED_REPORT_NAME)){
                    TripoliMessageDialog.showWarningDialog("Report name: " + currentReport.FIXED_REPORT_NAME + " is restricted", reportStage);
                } else if (!newName.trim().isEmpty()) {
                    if (event.getSource() == newFullButton){ // Generate full report template
                        setCurrentReport(Report.createFullReport(newName, currentReport.getMethodName(), analysis.getUserFunctions()));
                    } else { // Generate Blank Report Template
                        setCurrentReport(Report.createBlankReport(newName.trim(), currentReport.getMethodName()));

                    }
                }
            });
        }
    }

    public void saveOnAction() throws TripoliException {
        if (currentReport.getReportName() == null){
            TripoliMessageDialog.showWarningDialog("Report must have a name", reportStage);
        } else if (currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
            TripoliMessageDialog.showWarningDialog("Report name: " + currentReport.FIXED_REPORT_NAME + " is restricted", reportStage);
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
        boolean proceed = TripoliMessageDialog.showChoiceDialog("Are you sure? \n Delete Report: "+ currentReport.getReportName() + "?", reportStage);
        if (proceed && currentReport.deleteReport()){
            TripoliMessageDialog.showInfoDialog("Report Deleted!", reportStage);
        }
        reportStage.close();
    }

    public void generateOnAction() {
        File csvFile = currentReport.generateCSVFile(listOfAnalyses);
        if (csvFile != null){
            TripoliMessageDialog.showSavedAsDialog(csvFile, reportStage);
        } else {
            TripoliMessageDialog.showWarningDialog("Something went wrong and the report could not be generated", reportStage);
        }
    }


    public void createCategoryOnAction() {
        String categoryName = categoryTextField.getText();

        // Category cannot already exist
        if (categoryName.trim().isEmpty()) {
            TripoliMessageDialog.showWarningDialog("Category must have name!", reportStage);
            categoryTextField.setText("");
        } else if (categories.stream().anyMatch(t -> t.getCategoryName().equalsIgnoreCase(categoryName))) {
            TripoliMessageDialog.showWarningDialog("Category already exists!", reportStage);
            categoryTextField.setText("");
        }else {
            ReportCategory newCategory = new ReportCategory(categoryName, categories.size());
            currentReport.addCategory(newCategory);
            handleTrackingChanges();
            categoryTextField.setText("");
            initializeListViews();
        }
    }

    private void handleTrackingChanges() {
        if (!currentReport.equals(initalReport)) {
            unsavedChanges = true;
            restoreButton.setDisable(false);
            reportStage.setTitle("Report Builder - " + currentReport.getReportName() + "*");
            reportNameTextField.setText(currentReport.getReportName() + "*");
        } else {
            unsavedChanges = false;
            restoreButton.setDisable(true);
            reportStage.setTitle("Report Builder - " + currentReport.getReportName());
            reportNameTextField.setText(currentReport.getReportName());
        }
    }
    private boolean proceedWithUnsavedDialog(){
        if (unsavedChanges){
            return TripoliMessageDialog.showChoiceDialog("Unsaved changes exist! Are you sure?", reportStage);
        }
        return true;
    }
}


