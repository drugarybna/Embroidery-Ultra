package com.drugarybna.embroidery_ultra;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EmbroideryApp extends Application {

    private final int rows = 32;
    private final int cols = 32;
    private final int cellSize = 16;

    Color[][] grid = new Color[rows][cols];
    Color currentColor = Color.RED;

    @Override
    public void start(Stage stage) throws Exception {

        Canvas canvas = new Canvas(cols * cellSize, rows * cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawGrid(gc);

        VBox canvasPane = new VBox(canvas);
        canvasPane.setPadding(new Insets(20, 20, 0, 0));

        Button exportPNG = new Button("Export PNG");
        exportPNG.setOnAction(e -> {

        });
        Button exportEMB = new Button("Export EMB");
        exportEMB.setOnAction(e -> {

        });
        Button importEMB = new Button("Import EMB");
        importEMB.setOnAction(e -> {

        });
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            grid = new Color[rows][cols];
            gc.clearRect(0, 0, cols * cellSize, rows * cellSize);
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    gc.setStroke(Color.GRAY);
                    gc.strokeRect(col * cellSize, row * cellSize, cellSize, cellSize);
                }
            }
        });

        HBox buttonPane = new HBox(exportPNG, exportEMB, importEMB);
        buttonPane.setSpacing(10);
        BorderPane buttonPaneRoot = new BorderPane(buttonPane);
        buttonPaneRoot.setRight(resetButton);
        buttonPaneRoot.setPadding(new Insets(20, 0, 0, 0));
        canvasPane.getChildren().add(buttonPaneRoot);

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

        CheckBox symmetrySwitch = new CheckBox("Symmetry");

        ComboBox<String> symmetryType = new ComboBox<>();
        symmetryType.setDisable(true);
        symmetryType.getItems().addAll("Horizontal", "Vertical");
        symmetryType.setValue("Horizontal");
        symmetrySwitch.setOnAction(e -> {
            symmetryType.setDisable(!symmetrySwitch.isSelected());
        });
        symmetryType.setOnAction(event -> {

        });

        VBox symmetryPane = new VBox(symmetrySwitch, symmetryType);
        symmetryPane.setSpacing(10);

        ToggleGroup brushesType = new ToggleGroup();

        HBox brushTypeDefault = addBrush(brushesType, getBrush(0), "Default");
        HBox brushTypeSquare = addBrush(brushesType, getBrush(1), "Square");
        HBox brushTypeRhombus = addBrush(brushesType, getBrush(2), "Rhombus");
        HBox brushTypeCross1 = addBrush(brushesType, getBrush(3), "Cross 1");
        HBox brushTypeCross2 = addBrush(brushesType, getBrush(4), "Cross 2");

        Text brushTypeText = new Text("Brush type:");
        VBox brushTypePane = new VBox(brushTypeText, brushTypeDefault, brushTypeSquare, brushTypeRhombus, brushTypeCross1, brushTypeCross2);
        brushTypePane.setSpacing(20);

        VBox tools = new VBox(colorPicker, symmetryPane, brushTypePane);
        tools.setPadding(new Insets(20, 0, 0, 20));
        tools.setSpacing(20);

        BorderPane root = new BorderPane();
        root.setLeft(tools);
        root.setRight(canvasPane);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Embroidery Ultra by Volodymyr 'drugarybna' Stepanov");
        stage.setScene(scene);
        stage.show();

    }

    private Color[][] getBrush(int type) {
        List<Color[][]> brushes = new ArrayList<>();
        brushes.add(
                new Color[][] {
                        {null, null, null},
                        {null, Color.RED, null},
                        {null, null, null}
                }
        );
        brushes.add(
                new Color[][] {
                        {Color.RED, Color.RED, Color.RED},
                        {Color.RED, null, Color.RED},
                        {Color.RED, Color.RED, Color.RED}
                }
        );
        brushes.add(
                new Color[][] {
                        {null, Color.RED, null},
                        {Color.RED, null, Color.RED},
                        {null, Color.RED, null}
                }
        );
        brushes.add(
                new Color[][] {
                        {null, Color.RED, null},
                        {Color.RED, Color.RED, Color.RED},
                        {null, Color.RED, null}
                }
        );
        brushes.add(
                new Color[][] {
                        {Color.RED, null, Color.RED},
                        {null, Color.RED, null},
                        {Color.RED, null, Color.RED}
                }
        );
        return brushes.get(type);
    }

    private HBox addBrush(ToggleGroup group, Color[][] pattern, String name) {
        Canvas brushCanvas = new Canvas(3 * cellSize, 3 * cellSize);
        GraphicsContext brushGC = brushCanvas.getGraphicsContext2D();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Color brushColor = pattern[row][col];
                if (brushColor != null) {
                    brushGC.setFill(brushColor);
                    brushGC.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                }
            }
        }
        RadioButton brushRadioButton = new RadioButton(name);
        if (name.equals("Default")) {
            brushRadioButton.setSelected(true);
        }
        brushRadioButton.setToggleGroup(group);
        HBox brushTypePane = new HBox(brushCanvas, brushRadioButton);
        brushTypePane.setSpacing(20);
        brushTypePane.setAlignment(Pos.CENTER);
        return brushTypePane;
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