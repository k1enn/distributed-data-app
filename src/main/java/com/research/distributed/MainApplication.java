package com.research.distributed;

import com.research.distributed.connection.FragmentConnectionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Distributed Research Database Application");

            // Initialize database connections
            initializeConnections();

            // Load main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/pastel-theme.css").toExternalForm());

            primaryStage.setTitle("Distributed Research Database System");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(900);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // Handle window close
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application closing...");
                shutdown();
            });

            primaryStage.show();
            logger.info("Application started successfully");

        } catch (Exception e) {
            logger.error("Failed to start application: {}", e.getMessage(), e);
            showErrorAndExit("Startup Error",
                    "Failed to start the application: " + e.getMessage());
        }
    }

    private void initializeConnections() {
        try {
            logger.info("Initializing database connections...");
            FragmentConnectionManager connectionManager = FragmentConnectionManager.getInstance();
            connectionManager.initialize();
            logger.info("Database connections initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connections: {}", e.getMessage());
            showErrorAndExit("Database Connection Error",
                    "Failed to connect to the database servers. Please ensure the SQL Server containers are running.\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void shutdown() {
        try {
            logger.info("Shutting down connections...");
            FragmentConnectionManager.getInstance().shutdown();
            logger.info("Connections shut down successfully");
        } catch (Exception e) {
            logger.error("Error during shutdown: {}", e.getMessage());
        }
    }

    private void showErrorAndExit(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
        });
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        logger.info("Launching application...");
        launch(args);
    }
}
