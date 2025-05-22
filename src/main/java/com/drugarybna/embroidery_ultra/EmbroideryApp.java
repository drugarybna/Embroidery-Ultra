package com.drugarybna.embroidery_ultra;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EmbroideryApp extends Application {

    private final int rows = 20;
    private final int cols = 20;
    private final int cellSize = 20;

    Color[][] grid = new Color[rows][cols];
    Color currentColor = Color.RED;

    @Override
    public void start(Stage stage) throws Exception {

        Canvas canvas = new Canvas(cols * cellSize, rows * cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        canvas.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / cellSize);
            int row = (int)(e.getY() / cellSize);
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                grid[row][col] = currentColor;
                drawGrid(gc);
            }
        });

        ColorPicker colorPicker = new ColorPicker(Color.RED);
        colorPicker.setOnAction(event -> currentColor = colorPicker.getValue());

        VBox tools = new VBox(colorPicker);
        BorderPane root = new BorderPane();
        root.setTop(tools);
        root.setCenter(canvas);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Embroidery Ultra");
        stage.setScene(scene);
        stage.show();

    }

    private void drawGrid(GraphicsContext gc) {
        gc.clearRect(0, 0, cols * cellSize, rows * cellSize);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color color = grid[row][col];
                if (color != null) {
                    gc.setFill(color);
                    gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                }
                gc.setStroke(Color.GRAY);
                gc.strokeRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }

}