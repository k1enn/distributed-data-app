package com.k1en;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.k1en.database.DatabaseManager;
import com.k1en.model.NhomNC;
import com.k1en.model.NhanVien;
import com.k1en.model.DeAn;
import com.k1en.model.ThamGia;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainApp extends Application {

    private DatabaseManager dbManager;

    // TableViews for all 4 tables
    private TableView<NhomNC> tableNhomNC;
    private TableView<NhanVien> tableNhanVien;
    private TableView<DeAn> tableDeAn;
    private TableView<ThamGia> tableThamGia;

    // Input fields
    private TextField txtMaNhom, txtTenNhom, txtTenPhong;
    private TextField txtMaNV, txtHoTen, txtMaNhomNV;
    private TextField txtMaDA, txtTenDA, txtMaNhomDA;
    private TextField txtMaNVTG, txtMADATG;

    private TextArea txtResults;
    private ComboBox<String> cmbTransparencyLevel;
    private TabPane tabPane;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Distributed Database Manager");

        // Initialize database connection
        try {
            dbManager = new DatabaseManager();
        } catch (SQLException e) {
            showAlert("Database Connection Error", e.getMessage());
            return;
        }

        // Create main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Query section with transparency controls
        VBox topSection = createQuerySection();
        root.setTop(topSection);

        // Center: TabPane with all 4 tables
        tabPane = createTablesTabPane();
        root.setCenter(tabPane);

        // Bottom: Query results
        txtResults = new TextArea();
        txtResults.setPrefHeight(120);
        txtResults.setEditable(false);
        txtResults.setPromptText("Query results will appear here...");
        root.setBottom(txtResults);

        // Create scene
        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> closeDatabase());
        primaryStage.show();

        // Load initial data for all tables
        loadAllData();
    }

    private VBox createQuerySection() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: #f8f9fd; -fx-border-color: #f8f9fd; -fx-border-width: 2;");

        Label title = new Label("DISTRIBUTED DATABASE QUERIES");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Query input fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label lblMaNhom = new Label("Mã Nhóm (for queries):");
        txtMaNhom = new TextField();
        txtMaNhom.setPromptText("NC01");

        Label lblTenPhong = new Label("Tên Phòng (for Query 2):");
        txtTenPhong = new TextField();
        txtTenPhong.setPromptText("P2");

        Label lblTransparency = new Label("Transparency Level:");
        cmbTransparencyLevel = new ComboBox<>();
        cmbTransparencyLevel.getItems().addAll("Level 1: Fragmentation Transparency", "Level 2: Location Transparency");
        cmbTransparencyLevel.setValue("Level 1: Fragmentation Transparency");
        cmbTransparencyLevel.setPrefWidth(300);

        grid.add(lblMaNhom, 0, 0);
        grid.add(txtMaNhom, 1, 0);
        grid.add(lblTenPhong, 0, 1);
        grid.add(txtTenPhong, 1, 1);
        grid.add(lblTransparency, 0, 2);
        grid.add(cmbTransparencyLevel, 1, 2);

        // Query buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnQuery1 = new Button("Cau 1: Nhom Nghien Cuu 1");
        btnQuery1.setOnAction(e -> handleQuery1());
        btnQuery1.setStyle("-fx-background-color: #f8f9fd; -fx-text-fill: black; -fx-font-weight: bold;");
        btnQuery1.setPrefWidth(220);

        Button btnQuery2 = new Button("Cau 2: Doi ten phong");
        btnQuery2.setOnAction(e -> handleQuery2());
        btnQuery2.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: black; -fx-font-weight: bold;");
        btnQuery2.setPrefWidth(220);

        Button btnQuery3 = new Button("Cau 3: De an ko ai them gia");
        btnQuery3.setOnAction(e -> handleQuery3());
        btnQuery3.setStyle("-fx-background-color: #8d8d8d; -fx-text-fill: white; -fx-font-weight: bold;");
        btnQuery3.setPrefWidth(250);

        Button btnRefreshAll = new Button("Refresh All Tables");
        btnRefreshAll.setOnAction(e -> loadAllData());
        btnRefreshAll.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        buttonBox.getChildren().addAll(btnQuery1, btnQuery2, btnQuery3, btnRefreshAll);

        vbox.getChildren().addAll(title, grid, buttonBox);
        return vbox;
    }

    private TabPane createTablesTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: NhomNC (Research Groups)
        Tab tabNhomNC = new Tab("Nhóm NC (Research Groups)");
        tabNhomNC.setContent(createNhomNCTab());

        // Tab 2: NhanVien (Employees)
        Tab tabNhanVien = new Tab("Nhân Viên (Employees)");
        tabNhanVien.setContent(createNhanVienTab());

        // Tab 3: DeAn (Projects)
        Tab tabDeAn = new Tab("Đề Án (Projects)");
        tabDeAn.setContent(createDeAnTab());

        // Tab 4: ThamGia (Participation)
        Tab tabThamGia = new Tab("Tham Gia (Participation)");
        tabThamGia.setContent(createThamGiaTab());

        tabPane.getTabs().addAll(tabNhomNC, tabNhanVien, tabDeAn, tabThamGia);
        return tabPane;
    }

    private TableView<NhomNC> createTableView() {
        TableView<NhomNC> table = new TableView<>();

        TableColumn<NhomNC, String> colMaNhom = new TableColumn<>("Mã Nhóm");
        colMaNhom.setCellValueFactory(cellData -> cellData.getValue().maNhomProperty());
        colMaNhom.setPrefWidth(150);

        TableColumn<NhomNC, String> colTenNhom = new TableColumn<>("Tên Nhóm");
        colTenNhom.setCellValueFactory(cellData -> cellData.getValue().tenNhomProperty());
        colTenNhom.setPrefWidth(300);

        TableColumn<NhomNC, String> colTenPhong = new TableColumn<>("Tên Phòng");
        colTenPhong.setCellValueFactory(cellData -> cellData.getValue().tenPhongProperty());
        colTenPhong.setPrefWidth(150);

        table.getColumns().addAll(colMaNhom, colTenNhom, colTenPhong);
        return table;
    }

    /**
     * Handle Add operation - Insert new research group
     */
    private void handleAdd() {
        PreparedStatement ps = null;
        try {
            String maNhom = txtMaNhom.getText().trim();
            String tenNhom = txtTenNhom.getText().trim();
            String tenPhong = txtTenPhong.getText().trim();

            // Comprehensive validation
            if (maNhom.isEmpty() || tenNhom.isEmpty() || tenPhong.isEmpty()) {
                showAlert("Validation Error", "All fields are required!\n\nMã Nhóm: Research Group Code\nTên Nhóm: Research Group Name\nTên Phòng: Room Name (P1 or P2)");
                return;
            }

            if (!tenPhong.equals("P1") && !tenPhong.equals("P2")) {
                showAlert("Validation Error", "Tên Phòng must be either 'P1' or 'P2'");
                return;
            }

            // Validate maNhom format (you can customize this)
            if (maNhom.length() > 10) {
                showAlert("Validation Error", "Mã Nhóm must be 10 characters or less");
                return;
            }

            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            // Determine which server based on tenPhong (Fragment Transparency)
            Connection conn = tenPhong.equals("P1") ?
                    dbManager.getConnection1() : dbManager.getConnection2();

            // Check if record already exists
            String checkSql = "SELECT COUNT(*) FROM nhomnc WHERE manhom = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, maNhom);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Duplicate Error", "Research group with code '" + maNhom + "' already exists in " + tenPhong);
                        return;
                    }
                }
            }

            // Insert the record
            String sql = "INSERT INTO nhomnc (manhom, tennhom, tenphong) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, maNhom);
            ps.setString(2, tenNhom);
            ps.setString(3, tenPhong);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Record added successfully to " + tenPhong + "!\n\nMã Nhóm: " + maNhom);
                loadAllData();
                clearFields();
                txtResults.setText("Add operation successful:\nResearch group '" + maNhom + "' added to " + tenPhong);
            } else {
                showAlert("Error", "Failed to add record. No rows affected.");
            }
        } catch (SQLException e) {
            String errorMsg = "Database error during add operation:\n" + e.getMessage();
            showAlert("Database Error", errorMsg);
            txtResults.setText(errorMsg);
            e.printStackTrace();
        } catch (Exception e) {
            String errorMsg = "Unexpected error: " + e.getMessage();
            showAlert("Error", errorMsg);
            txtResults.setText(errorMsg);
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handle Update operation - Update existing research group
     * This uses the general update function that can update any field
     */
    private void handleUpdate() {
        try {
            String maNhom = txtMaNhom.getText().trim();
            String tenNhom = txtTenNhom.getText().trim();
            String tenPhong = txtTenPhong.getText().trim();

            // Validation
            if (maNhom.isEmpty()) {
                showAlert("Validation Error", "Mã Nhóm is required to identify the record to update");
                return;
            }

            if (tenNhom.isEmpty() && tenPhong.isEmpty()) {
                showAlert("Validation Error", "Please enter at least one field to update:\n- Tên Nhóm (Research Group Name)\n- Tên Phòng (Room Name: P1 or P2)");
                return;
            }

            if (!tenPhong.isEmpty() && !tenPhong.equals("P1") && !tenPhong.equals("P2")) {
                showAlert("Validation Error", "Tên Phòng must be either 'P1' or 'P2'");
                return;
            }

            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Update");
            confirmAlert.setHeaderText("Update Research Group");
            confirmAlert.setContentText("Update record with Mã Nhóm: " + maNhom + "?");

            var result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            int totalUpdated = 0;
            StringBuilder updateFields = new StringBuilder();

            // Build dynamic UPDATE query
            StringBuilder sqlBuilder = new StringBuilder("UPDATE nhomnc SET ");
            List<String> updates = new ArrayList<>();

            if (!tenNhom.isEmpty()) {
                updates.add("tennhom = ?");
                updateFields.append("Tên Nhóm -> ").append(tenNhom).append("\n");
            }
            if (!tenPhong.isEmpty()) {
                updates.add("tenphong = ?");
                updateFields.append("Tên Phòng -> ").append(tenPhong).append("\n");
            }

            sqlBuilder.append(String.join(", ", updates));
            sqlBuilder.append(" WHERE manhom = ?");

            String sql = sqlBuilder.toString();

            // Try updating on both servers (Location Transparency)
            for (Connection conn : new Connection[]{dbManager.getConnection1(), dbManager.getConnection2()}) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    if (!tenNhom.isEmpty()) {
                        ps.setString(paramIndex++, tenNhom);
                    }
                    if (!tenPhong.isEmpty()) {
                        ps.setString(paramIndex++, tenPhong);
                    }
                    ps.setString(paramIndex, maNhom);

                    totalUpdated += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Update error on one server: " + e.getMessage());
                }
            }

            if (totalUpdated > 0) {
                showAlert("Success", "Record updated successfully!\n\nUpdated " + totalUpdated + " record(s)");
                txtResults.setText("Update operation successful:\n" + updateFields.toString());
                loadAllData();
            } else {
                showAlert("Not Found", "No record found with Mã Nhóm: " + maNhom);
                txtResults.setText("Update failed: Record not found");
            }

        } catch (Exception e) {
            String errorMsg = "Error during update: " + e.getMessage();
            showAlert("Error", errorMsg);
            txtResults.setText(errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Handle Delete operation - Delete research group from distributed database
     */
    private void handleDelete() {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            String maNhom = txtMaNhom.getText().trim();

            // Validation
            if (maNhom.isEmpty()) {
                showAlert("Validation Error", "Please enter Mã Nhóm (Research Group Code) to delete");
                return;
            }

            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Research Group");
            confirmAlert.setContentText("Are you sure you want to delete research group: " + maNhom + "?\n\nThis action cannot be undone.");
            confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            var result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.YES) {
                txtResults.setText("Delete operation cancelled by user.");
                return;
            }

            String sql = "DELETE FROM nhomnc WHERE manhom = ?";
            int totalDeleted = 0;

            // Try deleting from Server 1 (P1)
            try {
                ps1 = dbManager.getConnection1().prepareStatement(sql);
                ps1.setString(1, maNhom);
                int rows1 = ps1.executeUpdate();
                totalDeleted += rows1;
                if (rows1 > 0) {
                    System.out.println("Deleted " + rows1 + " record(s) from Server 1 (P1)");
                }
            } catch (SQLException e) {
                System.err.println("Error deleting from Server 1: " + e.getMessage());
            }

            // Try deleting from Server 2 (P2)
            try {
                ps2 = dbManager.getConnection2().prepareStatement(sql);
                ps2.setString(1, maNhom);
                int rows2 = ps2.executeUpdate();
                totalDeleted += rows2;
                if (rows2 > 0) {
                    System.out.println("Deleted " + rows2 + " record(s) from Server 2 (P2)");
                }
            } catch (SQLException e) {
                System.err.println("Error deleting from Server 2: " + e.getMessage());
            }

            if (totalDeleted > 0) {
                showAlert("Success", "Record deleted successfully!\n\nDeleted " + totalDeleted + " record(s) from distributed database.");
                txtResults.setText("Delete operation successful:\nResearch group '" + maNhom + "' has been removed.");
                loadAllData();
                clearFields();
            } else {
                showAlert("Not Found", "No record found with Mã Nhóm: " + maNhom);
                txtResults.setText("Delete failed: Record not found in any server.");
            }

        } catch (Exception e) {
            String errorMsg = "Error during delete operation: " + e.getMessage();
            showAlert("Error", errorMsg);
            txtResults.setText(errorMsg);
            e.printStackTrace();
        } finally {
            // Clean up resources
            if (ps1 != null) {
                try {
                    ps1.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement 1: " + e.getMessage());
                }
            }
            if (ps2 != null) {
                try {
                    ps2.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement 2: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Câu 1: Query projects with external participants
     * Demonstrates both Level 1 (Fragmentation Transparency) and Level 2 (Location Transparency)
     */
    private void handleQuery1() {
        try {
            String maNhom = txtMaNhom.getText().trim();
            if (maNhom.isEmpty()) {
                showAlert("Validation Error", "Please enter Mã Nhóm (Research Group Code) to execute Query 1");
                return;
            }

            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            boolean isLevel1 = cmbTransparencyLevel.getValue().startsWith("Level 1");
            List<String[]> results;
            StringBuilder sb = new StringBuilder();

            if (isLevel1) {
                // LEVEL 1: Fragmentation Transparency
                // User queries global schema, doesn't know about fragments
                sb.append("=== Câu 1: LEVEL 1 - FRAGMENTATION TRANSPARENCY ===\n");
                sb.append("User Query: SELECT projects from DEAN where manhom='").append(maNhom).append("' AND has external participants\n");
                sb.append("System Action: Automatically query all fragments (user doesn't know they exist)\n");
                sb.append("=================================================\n\n");

                results = dbManager.query1_Level1_FragmentationTransparency(maNhom);
            } else {
                // LEVEL 2: Location Transparency
                // User knows fragment names but not their physical locations
                sb.append("=== Câu 1: LEVEL 2 - LOCATION TRANSPARENCY ===\n");
                sb.append("User Query: Query fragments DEAN_P1 and DEAN_P2 for manhom='").append(maNhom).append("'\n");
                sb.append("System Action: Map DEAN_P1→Server1, DEAN_P2→Server2 (user doesn't know locations)\n");
                sb.append("===============================================\n\n");

                List<String> fragments = Arrays.asList("DEAN_P1", "DEAN_P2");
                results = dbManager.query1_Level2_LocationTransparency(maNhom, fragments);
            }

            if (results.isEmpty()) {
                sb.append("No projects found with external participants from other research groups.");
            } else {
                sb.append("Found ").append(results.size()).append(" project(s):\n\n");
                int count = 1;
                for (String[] row : results) {
                    sb.append(count++).append(". Project ID: ").append(row[0])
                            .append("\n   Project Name: ").append(row[1]).append("\n\n");
                }
            }
            txtResults.setText(sb.toString());
            showAlert("Query Success", "Query executed successfully at " + (isLevel1 ? "Level 1" : "Level 2") + ". Found " + results.size() + " result(s).");
        } catch (IllegalArgumentException e) {
            showAlert("Validation Error", e.getMessage());
            txtResults.setText("Query 1 failed: " + e.getMessage());
        } catch (SQLException e) {
            showAlert("Database Error", "Query 1 execution error: " + e.getMessage());
            txtResults.setText("Query 1 failed due to database error:\n" + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
            txtResults.setText("Query 1 failed: " + e.getMessage());
        }
    }

    /**
     * Câu 2: Update room name based on user input
     * Demonstrates both Level 1 (Fragmentation Transparency) and Level 2 (Location Transparency)
     */
    private void handleQuery2() {
        try {
            // Get values from input fields
            String maNhom = txtMaNhom.getText().trim();
            String newTenPhong = txtTenPhong.getText().trim();

            // Validation - both fields are required
            if (maNhom.isEmpty()) {
                showAlert("Validation Error", "Please enter Mã Nhóm (Research Group Code) to update.\n\nExample: NC01");
                return;
            }

            if (newTenPhong.isEmpty()) {
                showAlert("Validation Error", "Please enter new Tên Phòng (Room Name) in the Tên Phòng field.\n\nExample: P2");
                return;
            }

            // Validate new room name is P1 or P2
            if (!newTenPhong.equals("P1") && !newTenPhong.equals("P2")) {
                showAlert("Validation Error", "Tên Phòng must be either 'P1' or 'P2'.\n\nYou entered: " + newTenPhong);
                return;
            }

            final String finalMaNhom = maNhom;
            final String finalNewTenPhong = newTenPhong;

            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            // Confirmation dialog
            boolean isLevel1 = cmbTransparencyLevel.getValue().startsWith("Level 1");
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Update - " + (isLevel1 ? "Level 1" : "Level 2"));
            confirmAlert.setHeaderText("Update Room Name");
            confirmAlert.setContentText("Update TenPhong from 'P1' to '" + newTenPhong + "' for research group '" + maNhom + "'?\n\n" +
                    (isLevel1 ? "Level 1: System will search all fragments automatically" :
                            "Level 2: System will update specified fragments (NHOMNC_P1, NHOMNC_P2)"));

            var result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int rowsUpdated;
                StringBuilder sb = new StringBuilder();

                if (isLevel1) {
                    // LEVEL 1: Fragmentation Transparency
                    sb.append("=== Câu 2: LEVEL 1 - FRAGMENTATION TRANSPARENCY ===\n");
                    sb.append("User Query: UPDATE NHOMNC SET tenphong='").append(newTenPhong)
                            .append("' WHERE manhom='").append(maNhom).append("' AND tenphong='P1'\n");
                    sb.append("System Action: Search all fragments automatically\n");
                    sb.append("=================================================\n\n");

                    rowsUpdated = dbManager.query2_Level1_FragmentationTransparency(maNhom, newTenPhong);
                } else {
                    // LEVEL 2: Location Transparency
                    sb.append("=== Câu 2: LEVEL 2 - LOCATION TRANSPARENCY ===\n");
                    sb.append("User Query: UPDATE fragments NHOMNC_P1, NHOMNC_P2 SET tenphong='").append(newTenPhong)
                            .append("' WHERE manhom='").append(maNhom).append("' AND tenphong='P1'\n");
                    sb.append("System Action: Map NHOMNC_P1→Server1, NHOMNC_P2→Server2 (user doesn't know locations)\n");
                    sb.append("===============================================\n\n");

                    List<String> fragments = Arrays.asList("NHOMNC_P1", "NHOMNC_P2");
                    rowsUpdated = dbManager.query2_Level2_LocationTransparency(maNhom, newTenPhong, fragments);
                }

                sb.append("Successfully updated ").append(rowsUpdated).append(" record(s).\n");
                sb.append("Changed TenPhong from 'P1' to '").append(newTenPhong).append("' for Mã Nhóm '").append(maNhom).append("'");

                txtResults.setText(sb.toString());
                showAlert("Update Success", "Updated " + rowsUpdated + " record(s) successfully!\n\n" +
                        "Research Group: " + maNhom + "\n" +
                        "New Room: " + newTenPhong + "\n" +
                        "Transparency: " + (isLevel1 ? "Level 1" : "Level 2"));

                // Refresh table to show changes
                loadAllData();
                clearFields();
            }
        } catch (IllegalArgumentException e) {
            showAlert("Validation Error", e.getMessage());
            txtResults.setText("=== Câu 2 Validation Error ===\n" + e.getMessage());
        } catch (SQLException e) {
            String errorMsg = "Database error during update:\n" + e.getMessage();
            showAlert("Database Error", errorMsg);
            txtResults.setText("=== Câu 2 Database Error ===\n" + errorMsg);
        } catch (Exception e) {
            String errorMsg = "Unexpected error: " + e.getMessage();
            showAlert("Error", errorMsg);
            txtResults.setText("=== Câu 2 Error ===\n" + errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Câu 3: Get projects without any employee participation
     * Demonstrates both Level 1 (Fragmentation Transparency) and Level 2 (Location Transparency)
     */
    private void handleQuery3() {
        try {
            // Check database connection health
            if (!dbManager.isConnectionHealthy()) {
                showAlert("Connection Error", "Database connection is not healthy. Please restart the application.");
                return;
            }

            boolean isLevel1 = cmbTransparencyLevel.getValue().startsWith("Level 1");
            List<String[]> results;
            StringBuilder sb = new StringBuilder();

            if (isLevel1) {
                // LEVEL 1: Fragmentation Transparency
                sb.append("=== Câu 3: LEVEL 1 - FRAGMENTATION TRANSPARENCY ===\n");
                sb.append("User Query: SELECT projects from DEAN WHERE NOT EXISTS participation\n");
                sb.append("System Action: Automatically query all fragments\n");
                sb.append("=================================================\n\n");

                results = dbManager.query3_Level1_FragmentationTransparency();
            } else {
                // LEVEL 2: Location Transparency
                sb.append("=== Câu 3: LEVEL 2 - LOCATION TRANSPARENCY ===\n");
                sb.append("User Query: Query fragments DEAN_P1 and DEAN_P2 for projects without participants\n");
                sb.append("System Action: Map fragments to servers (user doesn't know locations)\n");
                sb.append("===============================================\n\n");

                List<String> fragments = Arrays.asList("DEAN_P1", "DEAN_P2");
                results = dbManager.query3_Level2_LocationTransparency(fragments);
            }

            if (results.isEmpty()) {
                sb.append("All projects have at least one employee participant.\n");
                sb.append("No projects found without participants.");
            } else {
                sb.append("Found ").append(results.size()).append(" project(s) without participants:\n\n");
                int count = 1;
                for (String[] row : results) {
                    sb.append(count++).append(". Project ID: ").append(row[0])
                            .append("\n   Project Name: ").append(row[1]).append("\n\n");
                }
            }
            txtResults.setText(sb.toString());
            showAlert("Query Success", "Query executed successfully at " + (isLevel1 ? "Level 1" : "Level 2") + ". Found " + results.size() + " result(s).");
        } catch (SQLException e) {
            showAlert("Database Error", "Query 3 execution error: " + e.getMessage());
            txtResults.setText("Query 3 failed due to database error:\n" + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
            txtResults.setText("Query 3 failed: " + e.getMessage());
        }
    }

    // ==================== TAB CREATION METHODS ====================

    private VBox createNhomNCTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Create table
        tableNhomNC = new TableView<>();
        TableColumn<NhomNC, String> colMaNhom = new TableColumn<>("Mã Nhóm");
        colMaNhom.setCellValueFactory(cellData -> cellData.getValue().maNhomProperty());
        TableColumn<NhomNC, String> colTenNhom = new TableColumn<>("Tên Nhóm");
        colTenNhom.setCellValueFactory(cellData -> cellData.getValue().tenNhomProperty());
        TableColumn<NhomNC, String> colTenPhong = new TableColumn<>("Tên Phòng");
        colTenPhong.setCellValueFactory(cellData -> cellData.getValue().tenPhongProperty());
        tableNhomNC.getColumns().addAll(colMaNhom, colTenNhom, colTenPhong);

        // CRUD form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        TextField txtMa = new TextField();
        txtMa.setPromptText("Mã Nhóm");
        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên Nhóm");
        TextField txtPhong = new TextField();
        txtPhong.setPromptText("Tên Phòng (P1/P2)");

        form.add(new Label("Mã Nhóm:"), 0, 0);
        form.add(txtMa, 1, 0);
        form.add(new Label("Tên Nhóm:"), 0, 1);
        form.add(txtTen, 1, 1);
        form.add(new Label("Tên Phòng:"), 0, 2);
        form.add(txtPhong, 1, 2);

        HBox buttons = new HBox(10);
        Button btnAdd = new Button("Add");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");
        Button btnRefresh = new Button("Refresh");
        buttons.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);

        // Button handlers
        btnAdd.setOnAction(e -> handleAddNhomNC(txtMa, txtTen, txtPhong));
        btnUpdate.setOnAction(e -> handleUpdateNhomNC(txtMa, txtTen, txtPhong));
        btnDelete.setOnAction(e -> handleDeleteNhomNC(txtMa));
        btnRefresh.setOnAction(e -> loadNhomNCData());

        vbox.getChildren().addAll(form, buttons, tableNhomNC);
        VBox.setVgrow(tableNhomNC, Priority.ALWAYS);
        return vbox;
    }

    private VBox createNhanVienTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Create table
        tableNhanVien = new TableView<>();
        TableColumn<NhanVien, String> colMaNV = new TableColumn<>("Mã NV");
        colMaNV.setCellValueFactory(cellData -> cellData.getValue().maNVProperty());
        colMaNV.setPrefWidth(120);
        TableColumn<NhanVien, String> colHoTen = new TableColumn<>("Họ Tên");
        colHoTen.setCellValueFactory(cellData -> cellData.getValue().hoTenProperty());
        colHoTen.setPrefWidth(250);
        TableColumn<NhanVien, String> colMaNhom = new TableColumn<>("Mã Nhóm");
        colMaNhom.setCellValueFactory(cellData -> cellData.getValue().maNhomProperty());
        colMaNhom.setPrefWidth(120);
        tableNhanVien.getColumns().addAll(colMaNV, colHoTen, colMaNhom);

        // CRUD form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        TextField txtMaNV = new TextField();
        txtMaNV.setPromptText("NV001");
        TextField txtHoTen = new TextField();
        txtHoTen.setPromptText("Nguyễn Văn A");
        TextField txtMaNhomNV = new TextField();
        txtMaNhomNV.setPromptText("NC01");

        form.add(new Label("Mã NV:"), 0, 0);
        form.add(txtMaNV, 1, 0);
        form.add(new Label("Họ Tên:"), 2, 0);
        form.add(txtHoTen, 3, 0);
        form.add(new Label("Mã Nhóm:"), 4, 0);
        form.add(txtMaNhomNV, 5, 0);

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        Button btnAdd = new Button("Add Employee");
        Button btnUpdate = new Button("Update Employee");
        Button btnDelete = new Button("Delete Employee");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        buttons.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);

        // Button handlers
        btnAdd.setOnAction(e -> handleAddNhanVien(txtMaNV, txtHoTen, txtMaNhomNV));
        btnUpdate.setOnAction(e -> handleUpdateNhanVien(txtMaNV, txtHoTen, txtMaNhomNV));
        btnDelete.setOnAction(e -> handleDeleteNhanVien(txtMaNV));
        btnRefresh.setOnAction(e -> loadNhanVienData());

        vbox.getChildren().addAll(form, buttons, tableNhanVien);
        VBox.setVgrow(tableNhanVien, Priority.ALWAYS);
        return vbox;
    }

    private VBox createDeAnTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Create table
        tableDeAn = new TableView<>();
        TableColumn<DeAn, String> colMaDA = new TableColumn<>("Mã Đề Án");
        colMaDA.setCellValueFactory(cellData -> cellData.getValue().maDAProperty());
        colMaDA.setPrefWidth(120);
        TableColumn<DeAn, String> colTenDA = new TableColumn<>("Tên Đề Án");
        colTenDA.setCellValueFactory(cellData -> cellData.getValue().tenDAProperty());
        colTenDA.setPrefWidth(300);
        TableColumn<DeAn, String> colMaNhom = new TableColumn<>("Mã Nhóm");
        colMaNhom.setCellValueFactory(cellData -> cellData.getValue().maNhomProperty());
        colMaNhom.setPrefWidth(120);
        tableDeAn.getColumns().addAll(colMaDA, colTenDA, colMaNhom);

        // CRUD form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        TextField txtMaDA = new TextField();
        txtMaDA.setPromptText("DA001");
        TextField txtTenDA = new TextField();
        txtTenDA.setPromptText("Đề Án Nghiên Cứu AI");
        TextField txtMaNhomDA = new TextField();
        txtMaNhomDA.setPromptText("NC01");

        form.add(new Label("Mã Đề Án:"), 0, 0);
        form.add(txtMaDA, 1, 0);
        form.add(new Label("Tên Đề Án:"), 2, 0);
        form.add(txtTenDA, 3, 0);
        form.add(new Label("Mã Nhóm:"), 4, 0);
        form.add(txtMaNhomDA, 5, 0);

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        Button btnAdd = new Button("Add Project");
        Button btnUpdate = new Button("Update Project");
        Button btnDelete = new Button("Delete Project");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        buttons.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);

        // Button handlers
        btnAdd.setOnAction(e -> handleAddDeAn(txtMaDA, txtTenDA, txtMaNhomDA));
        btnUpdate.setOnAction(e -> handleUpdateDeAn(txtMaDA, txtTenDA, txtMaNhomDA));
        btnDelete.setOnAction(e -> handleDeleteDeAn(txtMaDA));
        btnRefresh.setOnAction(e -> loadDeAnData());

        vbox.getChildren().addAll(form, buttons, tableDeAn);
        VBox.setVgrow(tableDeAn, Priority.ALWAYS);
        return vbox;
    }

    private VBox createThamGiaTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Create table
        tableThamGia = new TableView<>();
        TableColumn<ThamGia, String> colMaNV = new TableColumn<>("Mã NV");
        colMaNV.setCellValueFactory(cellData -> cellData.getValue().maNVProperty());
        colMaNV.setPrefWidth(200);
        TableColumn<ThamGia, String> colMaDA = new TableColumn<>("Mã Đề Án");
        colMaDA.setCellValueFactory(cellData -> cellData.getValue().maDAProperty());
        colMaDA.setPrefWidth(200);
        tableThamGia.getColumns().addAll(colMaNV, colMaDA);

        // CRUD form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        TextField txtMaNVTG = new TextField();
        txtMaNVTG.setPromptText("NV001");
        TextField txtMaDATG = new TextField();
        txtMaDATG.setPromptText("DA001");

        form.add(new Label("Mã NV:"), 0, 0);
        form.add(txtMaNVTG, 1, 0);
        form.add(new Label("Mã Đề Án:"), 2, 0);
        form.add(txtMaDATG, 3, 0);

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        Button btnAdd = new Button("Add Participation");
        Button btnDelete = new Button("Delete Participation");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        buttons.getChildren().addAll(btnAdd, btnDelete, btnRefresh);

        Label note = new Label("Note: Participation is a junction table (no Update needed)");
        note.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        // Button handlers
        btnAdd.setOnAction(e -> handleAddThamGia(txtMaNVTG, txtMaDATG));
        btnDelete.setOnAction(e -> handleDeleteThamGia(txtMaNVTG, txtMaDATG));
        btnRefresh.setOnAction(e -> loadThamGiaData());

        vbox.getChildren().addAll(form, buttons, note, tableThamGia);
        VBox.setVgrow(tableThamGia, Priority.ALWAYS);
        return vbox;
    }

    // ==================== CRUD HANDLERS ====================

    private void handleAddNhomNC(TextField txtMa, TextField txtTen, TextField txtPhong) {
        // Reuse existing handleAdd logic
        try {
            String maNhom = txtMa.getText().trim();
            String tenNhom = txtTen.getText().trim();
            String tenPhong = txtPhong.getText().trim();

            if (maNhom.isEmpty() || tenNhom.isEmpty() || tenPhong.isEmpty()) {
                showAlert("Validation Error", "All fields are required!");
                return;
            }

            if (!tenPhong.equals("P1") && !tenPhong.equals("P2")) {
                showAlert("Validation Error", "Tên Phòng must be P1 or P2");
                return;
            }

            Connection conn = tenPhong.equals("P1") ? dbManager.getConnection1() : dbManager.getConnection2();
            String sql = "INSERT INTO nhomnc (manhom, tennhom, tenphong) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhom);
                ps.setString(2, tenNhom);
                ps.setString(3, tenPhong);
                ps.executeUpdate();
                showAlert("Success", "Record added successfully!");
                loadNhomNCData();
                txtMa.clear();
                txtTen.clear();
                txtPhong.clear();
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void handleUpdateNhomNC(TextField txtMa, TextField txtTen, TextField txtPhong) {
        showAlert("Info", "Update functionality - use General Update button at top");
    }

    private void handleDeleteNhomNC(TextField txtMa) {
        String maNhom = txtMa.getText().trim();
        if (maNhom.isEmpty()) {
            showAlert("Error", "Enter Mã Nhóm to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete " + maNhom + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String sql = "DELETE FROM nhomnc WHERE manhom = ?";
                for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, maNhom);
                        ps.executeUpdate();
                    }
                }
                showAlert("Success", "Record deleted");
                loadNhomNCData();
                txtMa.clear();
            } catch (SQLException e) {
                showAlert("Error", e.getMessage());
            }
        }
    }

    // ==================== NHANVIEN CRUD HANDLERS ====================

    private void handleAddNhanVien(TextField txtMaNV, TextField txtHoTen, TextField txtMaNhom) {
        PreparedStatement ps = null;
        try {
            String maNV = txtMaNV.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String maNhom = txtMaNhom.getText().trim();

            // Validation
            if (maNV.isEmpty() || hoTen.isEmpty() || maNhom.isEmpty()) {
                showAlert("Validation Error", "All fields are required!\n\n" +
                        "Mã NV: Employee ID\n" +
                        "Họ Tên: Full Name\n" +
                        "Mã Nhóm: Research Group Code");
                return;
            }

            // Validate employee ID length
            if (maNV.length() > 10) {
                showAlert("Validation Error", "Mã NV must be 10 characters or less");
                return;
            }

            // Find which server the research group belongs to
            Connection targetConn = findServerForNhom(maNhom);
            if (targetConn == null) {
                showAlert("Error", "Research group '" + maNhom + "' not found!\n\nPlease create the research group first.");
                return;
            }

            // Check if employee already exists
            String checkSql = "SELECT COUNT(*) FROM nhanvien WHERE manv = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, maNV);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Duplicate Error", "Employee with ID '" + maNV + "' already exists!");
                            return;
                        }
                    }
                }
            }

            // Insert employee
            String sql = "INSERT INTO nhanvien (manv, hoten, manhom) VALUES (?, ?, ?)";
            ps = targetConn.prepareStatement(sql);
            ps.setString(1, maNV);
            ps.setString(2, hoTen);
            ps.setString(3, maNhom);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Employee added successfully!\n\n" +
                        "Mã NV: " + maNV + "\n" +
                        "Họ Tên: " + hoTen + "\n" +
                        "Mã Nhóm: " + maNhom);
                loadNhanVienData();
                txtMaNV.clear();
                txtHoTen.clear();
                txtMaNhom.clear();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add employee:\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error:\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    private void handleUpdateNhanVien(TextField txtMaNV, TextField txtHoTen, TextField txtMaNhom) {
        try {
            String maNV = txtMaNV.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String maNhom = txtMaNhom.getText().trim();

            // Validation
            if (maNV.isEmpty()) {
                showAlert("Validation Error", "Mã NV is required to identify the employee");
                return;
            }

            if (hoTen.isEmpty() && maNhom.isEmpty()) {
                showAlert("Validation Error", "Please enter at least one field to update:\n" +
                        "- Họ Tên (Full Name)\n" +
                        "- Mã Nhóm (Research Group Code)");
                return;
            }

            // If updating manhom, verify it exists
            if (!maNhom.isEmpty()) {
                Connection targetConn = findServerForNhom(maNhom);
                if (targetConn == null) {
                    showAlert("Error", "Research group '" + maNhom + "' not found!");
                    return;
                }
            }

            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Update");
            confirmAlert.setHeaderText("Update Employee");
            confirmAlert.setContentText("Update employee with Mã NV: " + maNV + "?");

            var result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            int totalUpdated = 0;
            StringBuilder updateFields = new StringBuilder();

            // Build dynamic UPDATE query
            StringBuilder sqlBuilder = new StringBuilder("UPDATE nhanvien SET ");
            List<String> updates = new ArrayList<>();

            if (!hoTen.isEmpty()) {
                updates.add("hoten = ?");
                updateFields.append("Họ Tên -> ").append(hoTen).append("\n");
            }
            if (!maNhom.isEmpty()) {
                updates.add("manhom = ?");
                updateFields.append("Mã Nhóm -> ").append(maNhom).append("\n");
            }

            sqlBuilder.append(String.join(", ", updates));
            sqlBuilder.append(" WHERE manv = ?");
            String sql = sqlBuilder.toString();

            // Try updating on both servers
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    if (!hoTen.isEmpty()) {
                        ps.setString(paramIndex++, hoTen);
                    }
                    if (!maNhom.isEmpty()) {
                        ps.setString(paramIndex++, maNhom);
                    }
                    ps.setString(paramIndex, maNV);

                    totalUpdated += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Update error on one server: " + e.getMessage());
                }
            }

            if (totalUpdated > 0) {
                showAlert("Success", "Employee updated successfully!\n\nUpdated " + totalUpdated + " record(s)");
                txtResults.setText("Update operation successful:\n" + updateFields.toString());
                loadNhanVienData();
            } else {
                showAlert("Not Found", "No employee found with Mã NV: " + maNV);
            }

        } catch (Exception e) {
            showAlert("Error", "Update error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteNhanVien(TextField txtMaNV) {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) {
            showAlert("Validation Error", "Please enter Mã NV to delete");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete employee: " + maNV + "?\n\n" +
                "This will also remove all participation records for this employee.\n" +
                "This action cannot be undone.");
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        var result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            int totalDeleted = 0;

            // Delete from both servers
            String sql = "DELETE FROM nhanvien WHERE manv = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maNV);
                    totalDeleted += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Delete error on one server: " + e.getMessage());
                }
            }

            if (totalDeleted > 0) {
                showAlert("Success", "Employee deleted successfully!\n\nDeleted " + totalDeleted + " record(s)");
                loadNhanVienData();
                loadThamGiaData(); // Refresh participation table too
                txtMaNV.clear();
            } else {
                showAlert("Not Found", "No employee found with Mã NV: " + maNV);
            }
        } catch (Exception e) {
            showAlert("Error", "Delete error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DEAN CRUD HANDLERS ====================

    private void handleAddDeAn(TextField txtMaDA, TextField txtTenDA, TextField txtMaNhom) {
        PreparedStatement ps = null;
        try {
            String maDA = txtMaDA.getText().trim();
            String tenDA = txtTenDA.getText().trim();
            String maNhom = txtMaNhom.getText().trim();

            // Validation
            if (maDA.isEmpty() || tenDA.isEmpty() || maNhom.isEmpty()) {
                showAlert("Validation Error", "All fields are required!\n\n" +
                        "Mã Đề Án: Project ID\n" +
                        "Tên Đề Án: Project Name\n" +
                        "Mã Nhóm: Research Group Code");
                return;
            }

            // Validate project ID length
            if (maDA.length() > 10) {
                showAlert("Validation Error", "Mã Đề Án must be 10 characters or less");
                return;
            }

            // Find which server the research group belongs to
            Connection targetConn = findServerForNhom(maNhom);
            if (targetConn == null) {
                showAlert("Error", "Research group '" + maNhom + "' not found!\n\nPlease create the research group first.");
                return;
            }

            // Check if project already exists
            String checkSql = "SELECT COUNT(*) FROM dean WHERE mada = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, maDA);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Duplicate Error", "Project with ID '" + maDA + "' already exists!");
                            return;
                        }
                    }
                }
            }

            // Insert project
            String sql = "INSERT INTO dean (mada, tenda, manhom) VALUES (?, ?, ?)";
            ps = targetConn.prepareStatement(sql);
            ps.setString(1, maDA);
            ps.setString(2, tenDA);
            ps.setString(3, maNhom);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Project added successfully!\n\n" +
                        "Mã Đề Án: " + maDA + "\n" +
                        "Tên Đề Án: " + tenDA + "\n" +
                        "Mã Nhóm: " + maNhom);
                loadDeAnData();
                txtMaDA.clear();
                txtTenDA.clear();
                txtMaNhom.clear();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add project:\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error:\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    private void handleUpdateDeAn(TextField txtMaDA, TextField txtTenDA, TextField txtMaNhom) {
        try {
            String maDA = txtMaDA.getText().trim();
            String tenDA = txtTenDA.getText().trim();
            String maNhom = txtMaNhom.getText().trim();

            // Validation
            if (maDA.isEmpty()) {
                showAlert("Validation Error", "Mã Đề Án is required to identify the project");
                return;
            }

            if (tenDA.isEmpty() && maNhom.isEmpty()) {
                showAlert("Validation Error", "Please enter at least one field to update:\n" +
                        "- Tên Đề Án (Project Name)\n" +
                        "- Mã Nhóm (Research Group Code)");
                return;
            }

            // If updating manhom, verify it exists
            if (!maNhom.isEmpty()) {
                Connection targetConn = findServerForNhom(maNhom);
                if (targetConn == null) {
                    showAlert("Error", "Research group '" + maNhom + "' not found!");
                    return;
                }
            }

            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Update");
            confirmAlert.setHeaderText("Update Project");
            confirmAlert.setContentText("Update project with Mã Đề Án: " + maDA + "?");

            var result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            int totalUpdated = 0;
            StringBuilder updateFields = new StringBuilder();

            // Build dynamic UPDATE query
            StringBuilder sqlBuilder = new StringBuilder("UPDATE dean SET ");
            List<String> updates = new ArrayList<>();

            if (!tenDA.isEmpty()) {
                updates.add("tenda = ?");
                updateFields.append("Tên Đề Án -> ").append(tenDA).append("\n");
            }
            if (!maNhom.isEmpty()) {
                updates.add("manhom = ?");
                updateFields.append("Mã Nhóm -> ").append(maNhom).append("\n");
            }

            sqlBuilder.append(String.join(", ", updates));
            sqlBuilder.append(" WHERE mada = ?");
            String sql = sqlBuilder.toString();

            // Try updating on both servers
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    if (!tenDA.isEmpty()) {
                        ps.setString(paramIndex++, tenDA);
                    }
                    if (!maNhom.isEmpty()) {
                        ps.setString(paramIndex++, maNhom);
                    }
                    ps.setString(paramIndex, maDA);

                    totalUpdated += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Update error on one server: " + e.getMessage());
                }
            }

            if (totalUpdated > 0) {
                showAlert("Success", "Project updated successfully!\n\nUpdated " + totalUpdated + " record(s)");
                txtResults.setText("Update operation successful:\n" + updateFields.toString());
                loadDeAnData();
            } else {
                showAlert("Not Found", "No project found with Mã Đề Án: " + maDA);
            }

        } catch (Exception e) {
            showAlert("Error", "Update error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteDeAn(TextField txtMaDA) {
        String maDA = txtMaDA.getText().trim();
        if (maDA.isEmpty()) {
            showAlert("Validation Error", "Please enter Mã Đề Án to delete");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Project");
        confirmAlert.setContentText("Are you sure you want to delete project: " + maDA + "?\n\n" +
                "This will also remove all participation records for this project.\n" +
                "This action cannot be undone.");
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        var result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            int totalDeleted = 0;

            // Delete from both servers
            String sql = "DELETE FROM dean WHERE mada = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maDA);
                    totalDeleted += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Delete error on one server: " + e.getMessage());
                }
            }

            if (totalDeleted > 0) {
                showAlert("Success", "Project deleted successfully!\n\nDeleted " + totalDeleted + " record(s)");
                loadDeAnData();
                loadThamGiaData(); // Refresh participation table too
                txtMaDA.clear();
            } else {
                showAlert("Not Found", "No project found with Mã Đề Án: " + maDA);
            }
        } catch (Exception e) {
            showAlert("Error", "Delete error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== THAMGIA CRUD HANDLERS ====================

    private void handleAddThamGia(TextField txtMaNV, TextField txtMaDA) {
        PreparedStatement ps = null;
        try {
            String maNV = txtMaNV.getText().trim();
            String maDA = txtMaDA.getText().trim();

            // Validation
            if (maNV.isEmpty() || maDA.isEmpty()) {
                showAlert("Validation Error", "Both fields are required!\n\n" +
                        "Mã NV: Employee ID\n" +
                        "Mã Đề Án: Project ID");
                return;
            }

            // Verify employee exists
            Connection employeeConn = findServerForEmployee(maNV);
            if (employeeConn == null) {
                showAlert("Error", "Employee '" + maNV + "' not found!\n\nPlease add the employee first.");
                return;
            }

            // Verify project exists
            Connection projectConn = findServerForProject(maDA);
            if (projectConn == null) {
                showAlert("Error", "Project '" + maDA + "' not found!\n\nPlease add the project first.");
                return;
            }

            // Check if participation already exists
            String checkSql = "SELECT COUNT(*) FROM thamgia WHERE manv = ? AND mada = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, maNV);
                    checkPs.setString(2, maDA);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Duplicate Error", "Participation already exists!\n\n" +
                                    "Employee " + maNV + " is already participating in project " + maDA);
                            return;
                        }
                    }
                }
            }

            // Insert participation on the same server as the employee
            String sql = "INSERT INTO thamgia (manv, mada) VALUES (?, ?)";
            ps = employeeConn.prepareStatement(sql);
            ps.setString(1, maNV);
            ps.setString(2, maDA);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Participation added successfully!\n\n" +
                        "Employee: " + maNV + "\n" +
                        "Project: " + maDA);
                loadThamGiaData();
                txtMaNV.clear();
                txtMaDA.clear();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add participation:\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error:\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    private void handleDeleteThamGia(TextField txtMaNV, TextField txtMaDA) {
        String maNV = txtMaNV.getText().trim();
        String maDA = txtMaDA.getText().trim();

        if (maNV.isEmpty() || maDA.isEmpty()) {
            showAlert("Validation Error", "Both fields are required to delete participation:\n\n" +
                    "Mã NV: Employee ID\n" +
                    "Mã Đề Án: Project ID");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Participation");
        confirmAlert.setContentText("Remove employee " + maNV + " from project " + maDA + "?");
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        var result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            int totalDeleted = 0;

            // Delete from both servers
            String sql = "DELETE FROM thamgia WHERE manv = ? AND mada = ?";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maNV);
                    ps.setString(2, maDA);
                    totalDeleted += ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Delete error on one server: " + e.getMessage());
                }
            }

            if (totalDeleted > 0) {
                showAlert("Success", "Participation deleted successfully!\n\nDeleted " + totalDeleted + " record(s)");
                loadThamGiaData();
                txtMaNV.clear();
                txtMaDA.clear();
            } else {
                showAlert("Not Found", "No participation found for Employee: " + maNV + " and Project: " + maDA);
            }
        } catch (Exception e) {
            showAlert("Error", "Delete error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Find which server contains the specified research group
     * @param maNhom Research group code
     * @return Connection to the server containing the group, or null if not found
     */
    private Connection findServerForNhom(String maNhom) throws SQLException {
        String sql = "SELECT tenphong FROM nhomnc WHERE manhom = ?";

        // Try Server 1
        try (PreparedStatement ps = dbManager.getConnection1().prepareStatement(sql)) {
            ps.setString(1, maNhom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection1();
                }
            }
        }

        // Try Server 2
        try (PreparedStatement ps = dbManager.getConnection2().prepareStatement(sql)) {
            ps.setString(1, maNhom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection2();
                }
            }
        }

        return null; // Not found
    }

    /**
     * Find which server contains the specified employee
     */
    private Connection findServerForEmployee(String maNV) throws SQLException {
        String sql = "SELECT manhom FROM nhanvien WHERE manv = ?";

        // Try Server 1
        try (PreparedStatement ps = dbManager.getConnection1().prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection1();
                }
            }
        }

        // Try Server 2
        try (PreparedStatement ps = dbManager.getConnection2().prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection2();
                }
            }
        }

        return null; // Not found
    }

    /**
     * Find which server contains the specified project
     */
    private Connection findServerForProject(String maDA) throws SQLException {
        String sql = "SELECT manhom FROM dean WHERE mada = ?";

        // Try Server 1
        try (PreparedStatement ps = dbManager.getConnection1().prepareStatement(sql)) {
            ps.setString(1, maDA);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection1();
                }
            }
        }

        // Try Server 2
        try (PreparedStatement ps = dbManager.getConnection2().prepareStatement(sql)) {
            ps.setString(1, maDA);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dbManager.getConnection2();
                }
            }
        }

        return null; // Not found
    }

    // ==================== DATA LOADING METHODS ====================

    private void loadAllData() {
        loadNhomNCData();
        loadNhanVienData();
        loadDeAnData();
        loadThamGiaData();
    }

    private void loadNhomNCData() {
        if (tableNhomNC == null) return;
        tableNhomNC.getItems().clear();
        try {
            String sql = "SELECT manhom, tennhom, tenphong FROM nhomnc";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        tableNhomNC.getItems().add(new NhomNC(
                                rs.getString("manhom"),
                                rs.getString("tennhom"),
                                rs.getString("tenphong")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Load NhomNC error: " + e.getMessage());
        }
    }

    private void loadNhanVienData() {
        if (tableNhanVien == null) return;
        tableNhanVien.getItems().clear();
        try {
            String sql = "SELECT manv, hoten, manhom FROM nhanvien";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        tableNhanVien.getItems().add(new NhanVien(
                                rs.getString("manv"),
                                rs.getString("hoten"),
                                rs.getString("manhom")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Load NhanVien error: " + e.getMessage());
        }
    }

    private void loadDeAnData() {
        if (tableDeAn == null) return;
        tableDeAn.getItems().clear();
        try {
            String sql = "SELECT mada, tenda, manhom FROM dean";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        tableDeAn.getItems().add(new DeAn(
                                rs.getString("mada"),
                                rs.getString("tenda"),
                                rs.getString("manhom")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Load DeAn error: " + e.getMessage());
        }
    }

    private void loadThamGiaData() {
        if (tableThamGia == null) return;
        tableThamGia.getItems().clear();
        try {
            String sql = "SELECT manv, mada FROM thamgia";
            for (Connection conn : Arrays.asList(dbManager.getConnection1(), dbManager.getConnection2())) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        tableThamGia.getItems().add(new ThamGia(
                                rs.getString("manv"),
                                rs.getString("mada")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Load ThamGia error: " + e.getMessage());
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
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeDatabase() {
        try {
            if (dbManager != null) {
                dbManager.closeConnections();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
