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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;
import org.cirdles.tripoli.reports.Report;
import org.cirdles.tripoli.reports.ReportCategory;
import org.cirdles.tripoli.reports.ReportColumn;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    public Label methodNameLabel;
    @FXML
    public Label unsavedChangesLabel;
    @FXML
    private ListView<ReportCategory> categoryListView;
    @FXML
    private ListView<ReportColumn> columnListView;

    private static Stage reportBuilderStage;

    private Report currentReport;
    private Report initalReport;

    private ObservableList<ReportCategory> categories;
    private ObservableList<ReportColumn> columns;
    private List<AnalysisInterface> listOfAnalyses;

    final String DROPBARSTYLEDOWN = "-fx-border-color: dodgerblue; -fx-border-width: 0 0 2 0;";
    final String DROPBARSTYLEUP = "-fx-border-color: dodgerblue; -fx-border-width: 2 0 0 0;";

    public ReportBuilderController() {
        listOfAnalyses = new ArrayList<>();
    }

    public static void loadReportBuilder(Report report){
        if (reportBuilderStage != null && reportBuilderStage.isShowing()) {
            reportBuilderStage.close();
        }

        try {
            FXMLLoader loader = new FXMLLoader(ReportBuilderController.class.getResource("/org/cirdles/tripoli/gui/reports/ReportBuilder.fxml"));
            Parent root = loader.load();
            ReportBuilderController controller = loader.getController();

            reportBuilderStage = new Stage();
            reportBuilderStage.setTitle("Report Builder");
            reportBuilderStage.setScene(new Scene(root));

            reportBuilderStage.setX(primaryStage.getX() + (primaryStage.getWidth() - 875) / 2);
            reportBuilderStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 650) / 2);

            controller.setStage(reportBuilderStage);
            controller.setCurrentReport(report);

            reportBuilderStage.setOnHidden(e -> reportBuilderStage = null);

            reportBuilderStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        reportBuilderStage = stage;
        reportBuilderStage.setOnCloseRequest(event -> {
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
        deleteButton.setDisable(false);
        categoryListView.setDisable(false);
        columnListView.setDisable(false);
        generateButton.setDisable(false);
        createCategoryButton.setDisable(false);
        categoryTextField.setDisable(false);
        columnAccordion.setDisable(false);
        reportNameTextField.setDisable(false);
        methodNameLabel.setText(currentReport.getMethodName());
        unsavedChangesLabel.setVisible(false);
        reportBuilderStage.setTitle("Report Builder - " + currentReport.getReportName());
        reportNameTextField.setText(currentReport.getReportName());

        // Remove editing for the full report
        if(currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())){
            createCategoryButton.setDisable(true);
            categoryTextField.setDisable(true);
            deleteButton.setDisable(true);
            restoreButton.setDisable(true);
            saveButton.setDisable(true);
            columnAccordion.setDisable(true);
            reportNameTextField.setDisable(true);
        }

        initializeListViews();
    }

    @SuppressWarnings("unchecked")
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
                            setCursor(Cursor.DISAPPEAR);
                            Tooltip tooltip = new Tooltip("This category cannot be moved.");
                            tooltip.setShowDelay(Duration.seconds(0.5));
                            setTooltip(tooltip);
                            fixedCategoryCell.set(this);
                        } else if(currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
                            setCursor(Cursor.DISAPPEAR);
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
                    db.setDragView(cell.snapshot(null, null));
                    event.consume();
                }

            });

            // Accept drag over the cell
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() == cell.getListView() // Ensure same ListView
                        && event.getDragboard().hasString()) {
                    int targetIndex = cell.getIndex();
                    int draggedIndex = sourceCell.getIndex();

                    if (draggedIndex < targetIndex || targetIndex == 0) {
                        cell.setStyle(DROPBARSTYLEDOWN);
                    } else {
                        cell.setStyle(DROPBARSTYLEUP);
                    }
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragExited(e -> cell.setStyle(""));

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
                            dropIndex = 1;
                        }
                        categories.remove(draggedItem);
                        categories.add(dropIndex, draggedItem);
                        currentReport.updateCategoryPosition(draggedItem, dropIndex);
                        handleTrackingChanges();
                        categoryListView.getSelectionModel().select(dropIndex);
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
                    columnDetailsTextArea.setText(formatColumnDetails(columns.get(0)));
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
                    db.setDragView(cell.snapshot(null, null));
                    event.consume();
                }
            });

            // Accept drag over the cell
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() != categoryListView
                        && event.getDragboard().hasString()) {
                    int targetIndex = cell.getIndex();
                    List<ReportColumn> draggedItems = new ArrayList<>();
                    int draggedIndex = Integer.MAX_VALUE;
                    if (sourceCell.getListView().getUserData() instanceof ReportColumn) {
                        draggedItems.add((ReportColumn) sourceCell.getListView().getUserData());
                        draggedIndex = draggedItems.get(0).getPositionIndex();
                    }

                    if (draggedIndex < targetIndex  || targetIndex == 0) {
                        cell.setStyle(DROPBARSTYLEDOWN);
                    } else {
                        cell.setStyle(DROPBARSTYLEUP);
                    }
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            cell.setOnDragExited(e -> cell.setStyle(""));

            // Handle drop
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()
                        && event.getGestureSource() instanceof ListCell<?> sourceCell
                        && sourceCell.getListView() != categoryListView) {
                    List<ReportColumn> draggedItems = new ArrayList<>();
                    if (sourceCell.getListView().getUserData() instanceof ArrayList<?>) {
                        draggedItems = (List<ReportColumn>) sourceCell.getListView().getUserData();
                    } else {
                        draggedItems.add((ReportColumn) sourceCell.getListView().getUserData());
                    }

                    int dropIndex = cell.getIndex();
                    if (sourceCell.getListView() == columnListView){
                        dropIndex = Math.min(dropIndex, columns.size()-1);
                    } else {
                        dropIndex = Math.min(dropIndex, columns.size());
                    }

                    if (fixedColumnCell.get() != null
                            && categoryListView.getSelectionModel().isSelected(0)
                            && dropIndex <= fixedColumnCell.get().getIndex()) {
                        dropIndex = 1;
                    }

                    if (sourceCell.getListView() != columnListView
                            && draggedItems != null) {
                        int insertPos = dropIndex;
                        for (ReportColumn col : draggedItems) {
                            ReportColumn draggedItemCopy = new ReportColumn(col);
                            categoryListView.getSelectionModel()
                                    .getSelectedItem()
                                    .insertColumnAtPosition(draggedItemCopy, insertPos);
                            columns.add(insertPos, draggedItemCopy);
                            insertPos++;
                            }
                    } else if (draggedItems != null){ // Internal move can only be 1 item
                        columns.remove(draggedItems.get(0));
                        columns.add(dropIndex, draggedItems.get(0));
                        categoryListView.getSelectionModel().getSelectedItem().updateColumnPosition(draggedItems.get(0), dropIndex);
                    }
                    handleTrackingChanges();
                    columnListView.getSelectionModel().select(dropIndex);
                    success = true;
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

            if (db.hasString()
                    && event.getGestureSource() instanceof ListCell<?> sourceCell
                    && sourceCell.getListView() != categoryListView) {
                List<ReportColumn> draggedItems = new ArrayList<>();
                if (sourceCell.getListView().getUserData() instanceof ArrayList<?>) {
                    draggedItems = (List<ReportColumn>) sourceCell.getListView().getUserData();
                } else {
                    draggedItems.add((ReportColumn) sourceCell.getListView().getUserData());
                }

                if (draggedItems != null) {
                    int index = 0;
                    for (ReportColumn col : draggedItems) {
                        ReportColumn draggedItemCopy = new ReportColumn(col);
                        categoryListView.getSelectionModel().getSelectedItem().insertColumnAtPosition(draggedItemCopy, 0);
                        columns.add(index, draggedItemCopy);
                        index++;
                    }
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
        ReportColumn analysisNameColumn = categories.get(0).getColumns().stream().toList().get(0);
        StringBuilder result = new StringBuilder();

        if (column.isUserFunction()){
            result.append(String.format("%-35s %-20s %-10s %-10s%n", analysisNameColumn.getColumnName(), column.getColumnName()+" Mean", "StdDev", "Variance"));
        } else {
            result.append(String.format("%-35s %-45s%n", analysisNameColumn.getColumnName(), column.getColumnName()));
        }

        listOfAnalyses.stream()
                .filter(Analysis.class::isInstance)
                .map(Analysis.class::cast)
                .filter(analysis -> analysis.getMethod().getMethodName().equals(currentReport.getMethodName())) // Filter by method name
                .forEach(analysis -> {
                    AllBlockInitForDataLiteOne.initBlockModels(analysis); // Init values
                    if (column.isUserFunction()){
                        String[] ufString = column.retrieveData(analysis).split(",");
                        if (ufString.length == 1) {ufString = new String[]{ufString[0], "", ""};} // Handle UF error
                        result.append(String.format("%-35s %-20s %-10s %-10s%n", analysisNameColumn.retrieveData(analysis), ufString[0], ufString[1], ufString[2]) );
                    } else {
                        result.append(String.format("%-35s %-45s%n", analysisNameColumn.retrieveData(analysis), column.retrieveData(analysis)));
                    }
                });

        return result.toString();
    }

    private void populateAccordion() {
        Report accReport = Report.createFullReport("", analysis.getMethod().getMethodName(), analysis.getUserFunctions());
        Object[] repoCat = accReport.getCategories().toArray();
        ReportCategory customExpressionsCat = repoCat[3] instanceof ReportCategory ? (ReportCategory) repoCat[3] : null;
        /**
         * Temporary Data inserted for future custom expressions
         */
        if (customExpressionsCat != null) {
            customExpressionsCat.addColumn(new ReportColumn("Alpha", 0, null));
            customExpressionsCat.addColumn(new ReportColumn("Beta", 1, null));
            customExpressionsCat.addColumn(new ReportColumn("Gamma", 2, null));
        }

        for (ReportCategory cat : accReport.getCategories()) {
            ListView<ReportColumn> accColumnlv = new ListView<>();
            accColumnlv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            accColumnlv.getItems().addAll(cat.getColumns());

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
                                ObservableList<ReportColumn> selectedItems = accColumnlv.getSelectionModel().getSelectedItems();

                                if (!selectedItems.isEmpty()) {

                                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                                ClipboardContent content = new ClipboardContent();
                                content.putString(selectedItems.stream()
                                        .map(ReportColumn::getColumnName)
                                        .collect(Collectors.joining(",")));
                                db.setContent(content);

                                accColumnlv.setUserData(new ArrayList<>(selectedItems));
                                db.setDragView(this.snapshot(null, null));
                                event.consume();
                                }
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
        String analysisMethodName = analysis.getMethod().getMethodName();
            if (event.getSource() == newFullButton){ // Generate full report template
                setCurrentReport(Report.createFullReport(analysisMethodName.replaceAll(" ", "_"), analysisMethodName, analysis.getUserFunctions()));
            } else { // Generate Blank Report Template
                setCurrentReport(Report.createBlankReport(analysisMethodName.replaceAll(" ", "_"), analysisMethodName));

            }
        }
    }

    public void saveOnAction() throws TripoliException {
        boolean proceed;
        reportNameTextField.setText(reportNameTextField.getText().replaceAll("\\*", ""));
        String reportName = reportNameTextField.getText();
        if (reportName.isEmpty()){
            TripoliMessageDialog.showWarningDialog("Report must have a name", reportBuilderStage);
        } else if (currentReport.FIXED_REPORT_NAME.equals(reportName)) {
            TripoliMessageDialog.showWarningDialog("Report name: " + currentReport.FIXED_REPORT_NAME + " is restricted", reportBuilderStage);
        } else if (currentReport.getTripoliReportFile(reportName).exists()) {
            proceed = TripoliMessageDialog.showOverwriteDialog(currentReport.getTripoliReportFile(), reportBuilderStage);
            if (proceed) {
                currentReport.setReportName(reportName);
                currentReport.serializeReport();
                initalReport = new Report(currentReport); // Reset saved state
                handleTrackingChanges();
            }
        } else {
            currentReport.setReportName(reportName);
            currentReport.serializeReport();
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

    public void deleteOnAction() {
        if (!currentReport.getTripoliReportFile().exists()){
            TripoliMessageDialog.showWarningDialog("Report does not exist!", reportBuilderStage);
            return;
        }
        boolean proceed = TripoliMessageDialog.showChoiceDialog("Are you sure? \n Delete Report: "+ currentReport.getReportName() + "?", reportBuilderStage);
        if (proceed && !currentReport.deleteReport()){
            TripoliMessageDialog.showWarningDialog("Error has occurred! Report not deleted.", reportBuilderStage);
        }
        reportBuilderStage.close();
    }

    public void generateOnAction() {
        File reportCSVFile = Report.getReportCSVFile(listOfAnalyses, tripoliSession.getSessionName());

        String proceed = TripoliMessageDialog.showSavedAsDialog(reportCSVFile, reportBuilderStage);

        if (proceed != null && proceed.equals("Save and Open")){
            try {
                reportCSVFile = currentReport.generateCSVFile(listOfAnalyses, tripoliSession.getSessionName());
                Desktop.getDesktop().open(reportCSVFile);
            } catch (IOException e) {
            }
        } else if (proceed.equals("Save")){
            currentReport.generateCSVFile(listOfAnalyses, tripoliSession.getSessionName());
        }
    }


    public void createCategoryOnAction() {
        String categoryName = categoryTextField.getText();

        // Category cannot already exist
        if (categoryName.trim().isEmpty()) {
            TripoliMessageDialog.showWarningDialog("Category must have name!", reportBuilderStage);
            categoryTextField.setText("");
        } else if (categories.stream().anyMatch(t -> t.getCategoryName().equalsIgnoreCase(categoryName))) {
            TripoliMessageDialog.showWarningDialog("Category already exists!", reportBuilderStage);
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
        if (currentReport.FIXED_REPORT_NAME.equals(currentReport.getReportName())) {
            reportNameTextField.setText(currentReport.getReportName() + " (Not Editable)");
            reportBuilderStage.setTitle("Report Builder - " + currentReport.getReportName() + " (Not Editable)");
        } else if (!currentReport.equals(initalReport)) {
            unsavedChangesLabel.setVisible(true);
            restoreButton.setDisable(false);
        } else {
            unsavedChangesLabel.setVisible(false);
            restoreButton.setDisable(true);
        }

    }
    private boolean proceedWithUnsavedDialog(){
        if (unsavedChangesLabel.isVisible()) {
            return TripoliMessageDialog.showChoiceDialog("Unsaved changes exist! Are you sure?", reportBuilderStage);
        }
        return true;
    }
}


