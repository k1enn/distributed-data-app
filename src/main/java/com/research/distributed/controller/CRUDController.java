package com.research.distributed.controller;

import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.ValidationException;
import com.research.distributed.model.DeAn;
import com.research.distributed.model.NhanVien;
import com.research.distributed.model.NhomNC;
import com.research.distributed.model.ThamGia;
import com.research.distributed.service.CRUDService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CRUDController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(CRUDController.class);

    @FXML private Label titleLabel;
    @FXML private TableView<Object> dataTable;
    @FXML private VBox formContainer;
    @FXML private Button refreshButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private CRUDService crudService;
    private String entityType;
    private TransparencyLevel transparencyLevel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        crudService = new CRUDService();
        transparencyLevel = TransparencyLevel.LOCATION_TRANSPARENCY;

        dataTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> updateButtonStates(newSel != null));

        logger.info("CRUDController initialized");
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
        titleLabel.setText(getEntityTitle());
        setupTableColumns();
        refreshData();
    }

    public void setTransparencyLevel(TransparencyLevel level) {
        this.transparencyLevel = level;
    }

    private String getEntityTitle() {
        switch (entityType) {
            case "NhomNC": return "Research Groups Management";
            case "NhanVien": return "Employees Management";
            case "DeAn": return "Projects Management";
            case "ThamGia": return "Participations Management";
            default: return "Data Management";
        }
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        dataTable.getColumns().clear();

        switch (entityType) {
            case "NhomNC":
                TableColumn<Object, String> groupIdCol = new TableColumn<>("Group ID");
                groupIdCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhomNC) data.getValue()).getMaHomnc()));
                groupIdCol.setPrefWidth(120);

                TableColumn<Object, String> groupNameCol = new TableColumn<>("Group Name");
                groupNameCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhomNC) data.getValue()).getTenNhomnc()));
                groupNameCol.setPrefWidth(250);

                TableColumn<Object, String> deptCol = new TableColumn<>("Department");
                deptCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhomNC) data.getValue()).getTenPhong()));
                deptCol.setPrefWidth(100);

                dataTable.getColumns().addAll(groupIdCol, groupNameCol, deptCol);
                break;

            case "NhanVien":
                TableColumn<Object, String> empIdCol = new TableColumn<>("Employee ID");
                empIdCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhanVien) data.getValue()).getMaNv()));
                empIdCol.setPrefWidth(120);

                TableColumn<Object, String> nameCol = new TableColumn<>("Full Name");
                nameCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhanVien) data.getValue()).getHoTen()));
                nameCol.setPrefWidth(250);

                TableColumn<Object, String> empGroupCol = new TableColumn<>("Group ID");
                empGroupCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((NhanVien) data.getValue()).getMaHomnc()));
                empGroupCol.setPrefWidth(120);

                dataTable.getColumns().addAll(empIdCol, nameCol, empGroupCol);
                break;

            case "DeAn":
                TableColumn<Object, String> projIdCol = new TableColumn<>("Project ID");
                projIdCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((DeAn) data.getValue()).getMaDa()));
                projIdCol.setPrefWidth(120);

                TableColumn<Object, String> projNameCol = new TableColumn<>("Project Name");
                projNameCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((DeAn) data.getValue()).getTenDa()));
                projNameCol.setPrefWidth(300);

                TableColumn<Object, String> projGroupCol = new TableColumn<>("Group ID");
                projGroupCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((DeAn) data.getValue()).getMaHomnc()));
                projGroupCol.setPrefWidth(120);

                dataTable.getColumns().addAll(projIdCol, projNameCol, projGroupCol);
                break;

            case "ThamGia":
                TableColumn<Object, String> partEmpCol = new TableColumn<>("Employee ID");
                partEmpCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((ThamGia) data.getValue()).getMaNv()));
                partEmpCol.setPrefWidth(150);

                TableColumn<Object, String> partProjCol = new TableColumn<>("Project ID");
                partProjCol.setCellValueFactory(data ->
                        new SimpleStringProperty(((ThamGia) data.getValue()).getMaDa()));
                partProjCol.setPrefWidth(150);

                TableColumn<Object, String> dateCol = new TableColumn<>("Join Date");
                dateCol.setCellValueFactory(data -> {
                    ThamGia tg = (ThamGia) data.getValue();
                    return new SimpleStringProperty(
                            tg.getNgayThamGia() != null ? tg.getNgayThamGia().toString() : "N/A");
                });
                dateCol.setPrefWidth(150);

                dataTable.getColumns().addAll(partEmpCol, partProjCol, dateCol);
                break;
        }
    }

    private void updateButtonStates(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    @FXML
    private void handleAdd() {
        showEntityDialog(null);
    }

    @FXML
    private void handleEdit() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEntityDialog(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Record");
        confirm.setContentText("Are you sure you want to delete this record?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteEntity(selected);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void refreshData() {
        refreshButton.setDisable(true);
        new Thread(() -> {
            try {
                List<?> data = loadData();
                Platform.runLater(() -> {
                    dataTable.setItems(FXCollections.observableArrayList((List<Object>) data));
                    updateButtonStates(false);
                });
            } catch (DatabaseException e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Load Error", e.getDetailedMessage()));
            } finally {
                Platform.runLater(() -> refreshButton.setDisable(false));
            }
        }).start();
    }

    private List<?> loadData() throws DatabaseException {
        switch (entityType) {
            case "NhomNC": return crudService.getAllNhomNC(transparencyLevel);
            case "NhanVien": return crudService.getAllNhanVien(transparencyLevel);
            case "DeAn": return crudService.getAllDeAn(transparencyLevel);
            case "ThamGia": return crudService.getAllThamGia(transparencyLevel);
            default: throw new DatabaseException("Unknown entity type: " + entityType);
        }
    }

    private void showEntityDialog(Object entity) {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(entity == null ? "Add " + entityType : "Edit " + entityType);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        switch (entityType) {
            case "NhomNC":
                setupNhomNCForm(grid, (NhomNC) entity, dialog);
                break;
            case "NhanVien":
                setupNhanVienForm(grid, (NhanVien) entity, dialog);
                break;
            case "DeAn":
                setupDeAnForm(grid, (DeAn) entity, dialog);
                break;
            case "ThamGia":
                setupThamGiaForm(grid, (ThamGia) entity, dialog);
                break;
        }

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void setupNhomNCForm(GridPane grid, NhomNC existing, Dialog<Object> dialog) {
        TextField idField = new TextField(existing != null ? existing.getMaHomnc() : "");
        idField.setPromptText("e.g., NC01");
        idField.setDisable(existing != null);

        TextField nameField = new TextField(existing != null ? existing.getTenNhomnc() : "");
        nameField.setPromptText("Research Group Name");

        ComboBox<String> deptCombo = new ComboBox<>(FXCollections.observableArrayList("P1", "P2"));
        deptCombo.setValue(existing != null ? existing.getTenPhong() : "P1");

        grid.add(new Label("Group ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Group Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(deptCombo, 1, 2);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                NhomNC nhomNC = new NhomNC(
                        idField.getText().trim(),
                        nameField.getText().trim(),
                        deptCombo.getValue()
                );
                saveEntity(nhomNC, existing == null);
            }
            return null;
        });
    }

    private void setupNhanVienForm(GridPane grid, NhanVien existing, Dialog<Object> dialog) {
        TextField idField = new TextField(existing != null ? existing.getMaNv() : "");
        idField.setPromptText("e.g., NV01");
        idField.setDisable(existing != null);

        TextField nameField = new TextField(existing != null ? existing.getHoTen() : "");
        nameField.setPromptText("Full Name");

        TextField groupField = new TextField(existing != null ? existing.getMaHomnc() : "");
        groupField.setPromptText("e.g., NC01");

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Group ID:"), 0, 2);
        grid.add(groupField, 1, 2);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                NhanVien nhanVien = new NhanVien(
                        idField.getText().trim(),
                        nameField.getText().trim(),
                        groupField.getText().trim()
                );
                saveEntity(nhanVien, existing == null);
            }
            return null;
        });
    }

    private void setupDeAnForm(GridPane grid, DeAn existing, Dialog<Object> dialog) {
        TextField idField = new TextField(existing != null ? existing.getMaDa() : "");
        idField.setPromptText("e.g., DA01");
        idField.setDisable(existing != null);

        TextField nameField = new TextField(existing != null ? existing.getTenDa() : "");
        nameField.setPromptText("Project Name");

        TextField groupField = new TextField(existing != null ? existing.getMaHomnc() : "");
        groupField.setPromptText("e.g., NC01");

        grid.add(new Label("Project ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Project Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Group ID:"), 0, 2);
        grid.add(groupField, 1, 2);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                DeAn deAn = new DeAn(
                        idField.getText().trim(),
                        nameField.getText().trim(),
                        groupField.getText().trim()
                );
                saveEntity(deAn, existing == null);
            }
            return null;
        });
    }

    private void setupThamGiaForm(GridPane grid, ThamGia existing, Dialog<Object> dialog) {
        TextField empField = new TextField(existing != null ? existing.getMaNv() : "");
        empField.setPromptText("e.g., NV01");
        empField.setDisable(existing != null);

        TextField projField = new TextField(existing != null ? existing.getMaDa() : "");
        projField.setPromptText("e.g., DA01");
        projField.setDisable(existing != null);

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(empField, 1, 0);
        grid.add(new Label("Project ID:"), 0, 1);
        grid.add(projField, 1, 1);

        if (existing != null) {
            // Disable OK button for edit - ThamGia doesn't support update
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            grid.add(new Label("(Participation records cannot be edited)"), 0, 2, 2, 1);
        }

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && existing == null) {
                ThamGia thamGia = new ThamGia(
                        empField.getText().trim(),
                        projField.getText().trim()
                );
                saveEntity(thamGia, true);
            }
            return null;
        });
    }

    private void saveEntity(Object entity, boolean isNew) {
        new Thread(() -> {
            try {
                if (entity instanceof NhomNC) {
                    if (isNew) crudService.createNhomNC((NhomNC) entity);
                    else crudService.updateNhomNC((NhomNC) entity);
                } else if (entity instanceof NhanVien) {
                    if (isNew) crudService.createNhanVien((NhanVien) entity);
                    else crudService.updateNhanVien((NhanVien) entity);
                } else if (entity instanceof DeAn) {
                    if (isNew) crudService.createDeAn((DeAn) entity);
                    else crudService.updateDeAn((DeAn) entity);
                } else if (entity instanceof ThamGia) {
                    if (isNew) crudService.createThamGia((ThamGia) entity);
                }

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Record " + (isNew ? "created" : "updated") + " successfully");
                    refreshData();
                });
            } catch (DatabaseException | ValidationException e) {
                String message = e instanceof ValidationException ?
                        ((ValidationException) e).getDetailedMessage() :
                        ((DatabaseException) e).getDetailedMessage();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Save Error", message));
            }
        }).start();
    }

    private void deleteEntity(Object entity) {
        new Thread(() -> {
            try {
                if (entity instanceof NhomNC) {
                    crudService.deleteNhomNC(((NhomNC) entity).getMaHomnc());
                } else if (entity instanceof NhanVien) {
                    crudService.deleteNhanVien(((NhanVien) entity).getMaNv());
                } else if (entity instanceof DeAn) {
                    crudService.deleteDeAn(((DeAn) entity).getMaDa());
                } else if (entity instanceof ThamGia) {
                    ThamGia tg = (ThamGia) entity;
                    crudService.deleteThamGia(tg.getMaNv(), tg.getMaDa());
                }

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record deleted successfully");
                    refreshData();
                });
            } catch (DatabaseException | ValidationException e) {
                String message = e instanceof ValidationException ?
                        ((ValidationException) e).getDetailedMessage() :
                        ((DatabaseException) e).getDetailedMessage();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Delete Error", message));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
