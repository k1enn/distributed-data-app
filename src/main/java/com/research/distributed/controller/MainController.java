package com.research.distributed.controller;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.ValidationException;
import com.research.distributed.model.DeAn;
import com.research.distributed.model.NhomNC;
import com.research.distributed.service.CRUDService;
import com.research.distributed.service.QueryService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // Transparency Level Selection
    @FXML private RadioButton level1Radio;
    @FXML private RadioButton level2Radio;
    @FXML private ToggleGroup transparencyGroup;

    // Query 1: Projects with External Participants
    @FXML private TextField groupIdField;
    @FXML private Button query1Button;
    @FXML private TableView<DeAn> query1Table;
    @FXML private TableColumn<DeAn, String> query1ProjectIdCol;
    @FXML private TableColumn<DeAn, String> query1ProjectNameCol;
    @FXML private TableColumn<DeAn, String> query1GroupIdCol;

    // Query 2: Update Department
    @FXML private TextField updateGroupIdField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private Button query2Button;
    @FXML private Label query2Result;

    // Query 3: Projects Without Participants
    @FXML private Button query3Button;
    @FXML private TableView<DeAn> query3Table;
    @FXML private TableColumn<DeAn, String> query3ProjectIdCol;
    @FXML private TableColumn<DeAn, String> query3ProjectNameCol;
    @FXML private TableColumn<DeAn, String> query3GroupIdCol;

    private QueryService queryService;
    private CRUDService crudService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        queryService = new QueryService();
        crudService = new CRUDService();

        initializeTableColumns();
        initializeDepartmentCombo();

        logger.info("MainController initialized");
    }

    private void initializeTableColumns() {
        // Query 1 Table
        query1ProjectIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMaDa()));
        query1ProjectNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTenDa()));
        query1GroupIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMaHomnc()));

        // Query 3 Table
        query3ProjectIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMaDa()));
        query3ProjectNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTenDa()));
        query3GroupIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMaHomnc()));
    }

    private void initializeDepartmentCombo() {
        departmentCombo.setItems(FXCollections.observableArrayList("P1", "P2"));
        departmentCombo.getSelectionModel().selectFirst();
    }

    private TransparencyLevel getSelectedLevel() {
        return level1Radio.isSelected() ?
                TransparencyLevel.FRAGMENT_TRANSPARENCY :
                TransparencyLevel.LOCATION_TRANSPARENCY;
    }

    @FXML
    private void handleQuery1() {
        String groupId = groupIdField.getText().trim();
        if (groupId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Group ID");
            return;
        }

        TransparencyLevel level = getSelectedLevel();
        logger.info("Executing Query 1 with Group ID: {} at Level: {}", groupId, level);

        query1Button.setDisable(true);
        new Thread(() -> {
            try {
                List<DeAn> results;
                if (level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
                    results = queryService.getProjectsWithExternalParticipantsLevel1(groupId);
                } else {
                    results = queryService.getProjectsWithExternalParticipantsLevel2(groupId);
                }

                Platform.runLater(() -> {
                    query1Table.setItems(FXCollections.observableArrayList(results));
                    if (results.isEmpty()) {
                        showAlert(Alert.AlertType.INFORMATION, "No Results",
                                "No projects found with external participants for group: " + groupId);
                    }
                });
            } catch (DatabaseException e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Query Error", e.getDetailedMessage()));
            } finally {
                Platform.runLater(() -> query1Button.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void handleQuery2() {
        String groupId = updateGroupIdField.getText().trim();
        String newDepartment = departmentCombo.getValue();

        if (groupId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Group ID");
            return;
        }

        TransparencyLevel level = getSelectedLevel();
        logger.info("Executing Query 2: Update Group {} to Department {} at Level: {}",
                groupId, newDepartment, level);

        query2Button.setDisable(true);
        query2Result.setText("Processing...");

        new Thread(() -> {
            try {
                queryService.updateDepartment(groupId, newDepartment, level);
                Platform.runLater(() -> {
                    query2Result.setText("Successfully updated group " + groupId +
                            " to department " + newDepartment);
                    query2Result.setStyle("-fx-text-fill: green;");
                });
            } catch (DatabaseException | ValidationException e) {
                String message = e instanceof ValidationException ?
                        ((ValidationException) e).getDetailedMessage() :
                        ((DatabaseException) e).getDetailedMessage();
                Platform.runLater(() -> {
                    query2Result.setText("Error: " + e.getMessage());
                    query2Result.setStyle("-fx-text-fill: red;");
                    showAlert(Alert.AlertType.ERROR, "Update Error", message);
                });
            } finally {
                Platform.runLater(() -> query2Button.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void handleQuery3() {
        TransparencyLevel level = getSelectedLevel();
        logger.info("Executing Query 3 at Level: {}", level);

        query3Button.setDisable(true);
        new Thread(() -> {
            try {
                List<DeAn> results;
                if (level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
                    // For fragment transparency, query both fragments
                    results = queryService.getProjectsWithoutParticipantsLevel1("p1");
                    results.addAll(queryService.getProjectsWithoutParticipantsLevel1("p2"));
                } else {
                    results = queryService.getProjectsWithoutParticipantsLevel2();
                }

                Platform.runLater(() -> {
                    query3Table.setItems(FXCollections.observableArrayList(results));
                    if (results.isEmpty()) {
                        showAlert(Alert.AlertType.INFORMATION, "No Results",
                                "All projects have at least one participant");
                    }
                });
            } catch (DatabaseException e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Query Error", e.getDetailedMessage()));
            } finally {
                Platform.runLater(() -> query3Button.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void openGroupCRUD() {
        openCRUDWindow("NhomNC", "Research Groups");
    }

    @FXML
    private void openEmployeeCRUD() {
        openCRUDWindow("NhanVien", "Employees");
    }

    @FXML
    private void openProjectCRUD() {
        openCRUDWindow("DeAn", "Projects");
    }

    @FXML
    private void openParticipationCRUD() {
        openCRUDWindow("ThamGia", "Participations");
    }

    private void openCRUDWindow(String entityType, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CRUDView.fxml"));
            Parent root = loader.load();

            CRUDController controller = loader.getController();
            controller.setEntityType(entityType);
            controller.setTransparencyLevel(getSelectedLevel());

            Stage stage = new Stage();
            stage.setTitle("Manage " + title);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(query1Table.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/pastel-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(900);
            stage.setHeight(700);
            stage.show();

            logger.info("Opened CRUD window for: {}", entityType);
        } catch (IOException e) {
            logger.error("Error opening CRUD window: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open " + title + " management window");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/pastel-theme.css").toExternalForm());
        alert.showAndWait();
    }
}
