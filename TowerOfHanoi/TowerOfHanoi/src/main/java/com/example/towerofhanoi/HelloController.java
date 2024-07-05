package com.example.towerofhanoi;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Stack;

public class HelloController {
    @FXML
    private ComboBox<String> levelComboBox;
    @FXML
    private Label timeLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Pane drawPane;
    @FXML
    private Rectangle tower1;
    @FXML
    private Rectangle tower2;
    @FXML
    private Rectangle tower3;
    @FXML
    private Rectangle r1;
    @FXML
    private Rectangle r2;
    @FXML
    private Rectangle r3;

    private Stack<Integer>[] towers;
    private Rectangle diskBeingMoved;
    private int fromTower;
    private int numDisks = 3;
    private String userName;
    private Timeline timeline;
    private int elapsedTime = 0;
    private DBConnect connection = new DBConnect();

    @FXML
    public void initialize() {
        towers = new Stack[3];
        for (int i = 0; i < 3; i++) {
            towers[i] = new Stack<>();
        }
        levelComboBox.setValue("Level 1 (3 Disks)"); // Set default value
        userLabel.setText("User: ");
    }

    @FXML
    private void restartGame() {
        drawPane.getChildren().removeIf(node -> node instanceof Rectangle && node != tower1 && node != tower2 && node != tower3 && node != r1 && node != r2 && node != r3);

        // Kosongkan semua menara
        for (int i = 0; i < 3; i++) {
            towers[i].clear();
        }

        // Inisialisasi ulang tumpukan disk di menara 1
        for (int i = numDisks; i > 0; i--) {
            towers[0].push(i);
            Rectangle disk = createDisk(i);
            disk.setX(tower1.getLayoutX() + tower1.getWidth() / 2 - (i * 30) / 2.0);
            disk.setY(tower1.getLayoutY() + tower1.getHeight() - (numDisks - i + 1) * 20);
            drawPane.getChildren().add(disk);
        }
    }

    @FXML
    private void handleNewGame() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("User Input");
        dialog.setHeaderText("Enter your name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            userName = result.get().trim();
            userLabel.setText("User: " + userName);
            String selectedLevel = levelComboBox.getValue();
            if (selectedLevel.equals("Level 2 (4 Disks)")) {
                numDisks = 4;
            } else {
                numDisks = 3;
            }
            startGame();
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Input Required");
            alert.setHeaderText("No Name Entered");
            alert.setContentText("Please enter your name to start the game.");
            alert.showAndWait();
        }
    }

    private void startGame() {
        for (int i = 0; i < 3; i++) {
            towers[i].clear();
        }

        // Menghapus disk yang ada di drawPane
        drawPane.getChildren().removeIf(node -> node instanceof Rectangle && node != tower1 && node != tower2 && node != tower3 && node != r1 && node != r2 && node != r3);

        // Initialize the first tower with disks
        for (int i = numDisks; i > 0; i--) {
            towers[0].push(i);
            Rectangle disk = createDisk(i);
            disk.setX(tower1.getLayoutX() + tower1.getWidth() / 2 - (i * 30) / 2.0);
            disk.setY(tower1.getLayoutY() + tower1.getHeight() - (numDisks - i + 1) * 20);
            drawPane.getChildren().add(disk);
        }

        // Reset and start timer
        elapsedTime = 0;
        timeLabel.setText("Time: 0s");
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedTime++;
            timeLabel.setText("Time: " + elapsedTime + "s");
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private Color[] diskColors = {Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE};

    private Rectangle createDisk(int diskSize) {
        Rectangle disk = new Rectangle(diskSize * 30, 20);
        disk.setFill(diskColors[diskSize - 1]); // Set color based on disk size
        disk.setOnMousePressed(this::onMousePressed);
        disk.setOnMouseDragged(this::onMouseDragged);
        disk.setOnMouseReleased(this::onMouseReleased);
        return disk;
    }

    private Rectangle getTopDisk(int towerIndex) {
        if (towers[towerIndex].isEmpty()) {
            return null;
        }

        int topDiskSize = towers[towerIndex].peek();
        for (javafx.scene.Node node : drawPane.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle disk = (Rectangle) node;
                if (disk.getWidth() == topDiskSize * 30) {
                    return disk;
                }
            }
        }
        return null;
    }

    private void onMousePressed(MouseEvent event) {
        Rectangle disk = (Rectangle) event.getSource();
        int clickedTower = getTowerIndex(disk.getX());

        if (disk.equals(getTopDisk(clickedTower))) {
            fromTower = clickedTower;
            diskBeingMoved = disk;
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (diskBeingMoved != null) {
            diskBeingMoved.setX(event.getX() - diskBeingMoved.getWidth() / 2);
            diskBeingMoved.setY(event.getY() - diskBeingMoved.getHeight() / 2);
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (diskBeingMoved != null) {
            int targetTower = getTowerIndex(event.getX());
            if (!towers[fromTower].isEmpty() && (towers[targetTower].isEmpty() || towers[targetTower].peek() > towers[fromTower].peek())) {
                // Move the disk to the target tower
                towers[targetTower].push(towers[fromTower].pop());
                diskBeingMoved.setX(getTowerX(targetTower) + tower1.getWidth() / 2 - diskBeingMoved.getWidth() / 2);
                diskBeingMoved.setY(tower1.getLayoutY() + tower1.getHeight() - (towers[targetTower].size()) * 20);
            } else {
                // Return the disk to its original position
                diskBeingMoved.setX(getTowerX(fromTower) + tower1.getWidth() / 2 - diskBeingMoved.getWidth() / 2);
                diskBeingMoved.setY(tower1.getLayoutY() + tower1.getHeight() - (towers[fromTower].size()) * 20);
            }
            diskBeingMoved = null;
            checkGameCompletion();
        }
    }

    private int getTowerIndex(double x) {
        if (x < tower1.getLayoutX() + tower1.getWidth()) {
            return 0;
        } else if (x < tower2.getLayoutX() + tower2.getWidth()) {
            return 1;
        } else {
            return 2;
        }
    }

    private double getTowerX(int towerIndex) {
        switch (towerIndex) {
            case 0:
                return tower1.getLayoutX();
            case 1:
                return tower2.getLayoutX();
            case 2:
                return tower3.getLayoutX();
            default:
                return 0;
        }
    }

    private void checkGameCompletion() {
        if (isTowerComplete(towers[2])) {
            if (timeline != null) {
                timeline.stop();
            }

            // Save game record to database
            connection.saveGameRecord(userName, elapsedTime);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Completed");
            alert.setHeaderText("Congratulations, " + userName + "!");
            alert.setContentText("You have completed the game in " + elapsedTime + " seconds.");
            alert.showAndWait();

            resetToInitialState();
        }
    }

    private boolean isTowerComplete(Stack<Integer> tower) {
        if (tower.size() != numDisks) {
            return false;
        }
        for (int i = 0; i < numDisks; i++) {
            if (tower.get(i) != numDisks - i) {
                return false;
            }
        }
        return true;
    }

    private void resetToInitialState() {
        // Reset user name
        userName = null;
        userLabel.setText("User:");

        // Reset level combo box
        levelComboBox.setValue("Level 1 (3 Disks)");

        // Clear towers and remove disks from pane
        for (int i = 0; i < 3; i++) {
            towers[i].clear();
        }
        drawPane.getChildren().removeIf(node -> node instanceof Rectangle && node != tower1 && node != tower2 && node != tower3 && node != r1 && node != r2 && node != r3);

        // Reset timer label
        timeLabel.setText("Time: 0s");
        if (timeline != null) {
            timeline.stop();
        }
    }
}
