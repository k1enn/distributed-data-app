package com.k1en.controller;

import com.k1en.database.DatabaseManager;
import com.k1en.model.NhomNC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class MainController {
    @FXML private TextField txtMaNhom;
    @FXML private TextField txtTenNhom;
    @FXML private TextField txtTenPhong;
    @FXML private TableView<NhomNC> tableView;
    @FXML private TableColumn<NhomNC, String> colMaNhom;
    @FXML private TableColumn<NhomNC, String> colTenNhom;
    @FXML private TableColumn<NhomNC, String> colTenPhong;
    @FXML private TextArea txtResults;

    private DatabaseManager dbManager;
    private ObservableList<NhomNC> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            dbManager = new DatabaseManager();
            setupTableView();
            loadData();
        } catch (SQLException e) {
            showAlert("Connection Error", e.getMessage());
        }
    }

    private void setupTableView() {
        colMaNhom.setCellValueFactory(new PropertyValueFactory<>("maNhom"));
        colTenNhom.setCellValueFactory(new PropertyValueFactory<>("tenNhom"));
        colTenPhong.setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        tableView.setItems(data);
    }

    @FXML
    private void handleAdd() {
        try {
            String maNhom = txtMaNhom.getText();
            String tenNhom = txtTenNhom.getText();
            String tenPhong = txtTenPhong.getText();

            // Determine which server based on tenPhong
            Connection conn = tenPhong.equals("P1") ?
                    dbManager.getConnection1() : dbManager.getConnection2();

            String sql = "INSERT INTO nhomnc (manhom, tennhom, tenphong) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhom);
                ps.setString(2, tenNhom);
                ps.setString(3, tenPhong);
                ps.executeUpdate();
                showAlert("Success", "Record added successfully");
                loadData();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            dbManager.updateTenPhong(txtMaNhom.getText());
            showAlert("Success", "Record updated successfully");
            loadData();
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        // Implementation for delete with fragment awareness
        try {
            String maNhom = txtMaNhom.getText();

            // Try both servers
            String sql = "DELETE FROM nhomnc WHERE manhom = ?";
            try (PreparedStatement ps1 = dbManager.getConnection1().prepareStatement(sql);
                 PreparedStatement ps2 = dbManager.getConnection2().prepareStatement(sql)) {
                ps1.setString(1, maNhom);
                ps2.setString(1, maNhom);
                ps1.executeUpdate();
                ps2.executeUpdate();
                showAlert("Success", "Record deleted");
                loadData();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleQuery1() {
        // Câu 1: Projects with external participants
        try {
            var results = dbManager.executeQueryFragmentTransparency(txtMaNhom.getText());
            StringBuilder sb = new StringBuilder("Results:\n");
            for (String[] row : results) {
                sb.append("Project ID: ").append(row[0])
                        .append(", Name: ").append(row[1]).append("\n");
            }
            txtResults.setText(sb.toString());
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void loadData() {
        data.clear();
        try {
            // Load from both servers
            loadFromServer(dbManager.getConnection1());
            loadFromServer(dbManager.getConnection2());
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void loadFromServer(Connection conn) throws SQLException {
        String sql = "SELECT manhom, tennhom, tenphong FROM nhomnc";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new NhomNC(
                        rs.getString("manhom"),
                        rs.getString("tennhom"),
                        rs.getString("tenphong")
                ));
            }
        }
    }

    private void clearFields() {
        txtMaNhom.clear();
        txtTenNhom.clear();
        txtTenPhong.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

