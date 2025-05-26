package com.drugarybna.embroidery_ultra;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmbroideryApp extends Application {

    private final int rows = 32;
    private final int cols = 32;
    private final int cellSize = 16;

    Color[][] loadedPreset = loadEMB(new File("src/main/resources/com/drugarybna/embroidery_ultra/Vova.emb"));
    Color currentColor = Color.RED;

    Color[][] selectedBrush = {
            {null, null, null},
            {null, Color.RED, null},
            {null, null, null}
    };

    String selectedSymmetry = "Horizontal";

    @Override
    public void start(Stage stage) throws Exception {

        ColorPicker colorPicker = new ColorPicker(Color.RED);
        colorPicker.setOnAction(event -> currentColor = colorPicker.getValue());

        CheckBox symmetrySwitch = new CheckBox("Symmetry");

        ComboBox<String> symmetryType = new ComboBox<>();
        symmetryType.setDisable(true);
        symmetryType.getItems().addAll("Horizontal", "Vertical", "Diagonal");
        symmetryType.setValue("Horizontal");
        symmetryType.setOnAction(event -> selectedSymmetry = symmetryType.getValue() );

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

        CheckBox duplicateSwitch = new CheckBox("Duplicate");
        duplicateSwitch.setDisable(true);

        Spinner<Integer> duplicatesNum = new Spinner<>(2, 8, 2);
        duplicatesNum.setDisable(true);
        duplicateSwitch.setOnAction(e -> duplicatesNum.setDisable(!duplicateSwitch.isSelected()));

        symmetrySwitch.setOnAction(e -> {
            symmetryType.setDisable(!symmetrySwitch.isSelected());
            duplicateSwitch.setSelected(false);
            duplicateSwitch.setDisable(!symmetrySwitch.isSelected());
            if (duplicateSwitch.isSelected()) {
                duplicatesNum.setDisable(false);
            } else {
                duplicatesNum.setDisable(true);
            }
        });

        VBox duplicatePane = new VBox(duplicateSwitch, duplicatesNum);
        duplicatePane.setSpacing(10);

        VBox tools = new VBox(colorPicker, symmetryPane, brushTypePane, duplicatePane);
        tools.setPadding(new Insets(20, 0, 0, 20));
        tools.setSpacing(20);

        Canvas gridCanvas = new Canvas(cols * cellSize, rows * cellSize);
        GraphicsContext gcGrid = gridCanvas.getGraphicsContext2D();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gcGrid.setStroke(Color.LIGHTGRAY);
                gcGrid.strokeRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }
        Canvas canvas = new Canvas(cols * cellSize, rows * cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane canvasStack = new StackPane(gridCanvas, canvas);

        brushesType.selectedToggleProperty().addListener(e -> selectedBrush = (Color[][]) brushesType.getSelectedToggle().getUserData() );
        resetGrid(gc, loadedPreset);

        VBox canvasPane = new VBox(canvasStack);
        canvasPane.setPadding(new Insets(20, 20, 0, 0));

        Button exportPNG = getExportPNG(canvas, stage);
        Button exportEMB = getExportEMB(stage);
        Button importEMB = getImportEMB(gc, stage);
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            loadedPreset = new Color[rows][cols];
            resetGrid(gc, null);
        });

        HBox buttonPane = new HBox(exportPNG, exportEMB, importEMB);
        buttonPane.setSpacing(10);
        BorderPane buttonPaneRoot = new BorderPane(buttonPane);
        buttonPaneRoot.setRight(resetButton);
        buttonPaneRoot.setPadding(new Insets(20, 0, 0, 0));
        canvasPane.getChildren().add(buttonPaneRoot);

        canvas.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / cellSize) - 1;
            int row = (int)(e.getY() / cellSize) - 1;
            int dup = 1;
            if (duplicateSwitch.isSelected()) {
                dup = duplicatesNum.getValue();
            }
            drawBrush(gc, selectedBrush, currentColor, col * cellSize, row * cellSize, symmetrySwitch.isSelected(), dup);
        });

        BorderPane root = new BorderPane();
        root.setLeft(tools);
        root.setRight(canvasPane);

        Scene scene = new Scene(root, 800, 600);

        InputStream iconStream = getClass().getResourceAsStream("/com/drugarybna/embroidery_ultra/icon.png");
        if (iconStream == null) {
            System.err.println("Icon resource not found!");
        } else {
            Image icon = new Image(iconStream);
            stage.getIcons().add(icon);
        }

        stage.setTitle("Embroidery Ultra by Volodymyr 'drugarybna' Stepanov");
        stage.setScene(scene);
        stage.show();

    }

    private Button getExportPNG(Canvas canvas, Stage stage) {
        Button exportPNG = new Button("Export PNG");
        exportPNG.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export PNG");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                savePNG(canvas, file);
            }
        });
        return exportPNG;
    }

    private Button getImportEMB(GraphicsContext gc, Stage stage) {
        Button importEMB = new Button("Import EMB");
        importEMB.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import EMB");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Embroidery Picture", "*.emb"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                loadedPreset = loadEMB(file);
                resetGrid(gc, loadedPreset);
            }
        });
        return importEMB;
    }

    private Button getExportEMB(Stage stage) {
        Button exportEMB = new Button("Export EMB");
        exportEMB.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export EMB");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Embroidery Picture", "*.emb"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    saveEMB(loadedPreset, file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return exportEMB;
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
        brushRadioButton.setUserData(pattern);
        HBox brushTypePane = new HBox(brushCanvas, brushRadioButton);
        brushTypePane.setSpacing(20);
        brushTypePane.setAlignment(Pos.CENTER);
        return brushTypePane;
    }

    void drawBrush(GraphicsContext gc, Color[][] brush, Color col,
                   int startX, int startY, boolean symmetry, int dup) {
        int brushWidth = brush[0].length * cellSize;
        int brushHeight = brush.length * cellSize;
        for (int i = 0; i < dup; i++) {
            int offsetX = calculateOffsetX(i, brushWidth);
            int offsetY = calculateOffsetY(i, brushHeight);
            drawBrushCopy(gc, brush, col,
                    startX + offsetX,
                    startY + offsetY);
        }
        if (symmetry) {
            drawSymmetryCopies(gc, brush, col, startX, startY, dup);
        }
    }

    private int calculateOffsetX(int copyIndex, int brushWidth) {
        return switch (selectedSymmetry) {
            case "Vertical", "Diagonal" -> copyIndex * brushWidth;
            default -> 0;
        };
    }

    private int calculateOffsetY(int copyIndex, int brushHeight) {
        return switch (selectedSymmetry) {
            case "Horizontal", "Diagonal" -> copyIndex * brushHeight;
            default -> 0;
        };
    }

    private void drawSymmetryCopies(GraphicsContext gc, Color[][] brush, Color col,
                                    int startX, int startY, int dup) {
        int patternWidth = dup * brush[0].length * cellSize;
        int patternHeight = dup * brush.length * cellSize;
        int endX = startX + patternWidth;
        int endY = startY + patternHeight;
        switch (selectedSymmetry) {
            case "Horizontal" -> mirrorHorizontal(gc, brush, col, startX, startY, endX, endY, dup);
            case "Vertical" -> mirrorVertical(gc, brush, col, startX, startY, endX, endY, dup);
            case "Diagonal" -> mirrorDiagonal(gc, brush, col, startX, startY, endX, endY, dup);
        }
    }

    private void mirrorHorizontal(GraphicsContext gc, Color[][] brush, Color col,
                                  int startX, int startY, int endX, int endY, int dup) {
        int mirrorY = (rows * cellSize) - startY - (endY - startY);
        drawPatternWithDuplicates(gc, brush, col, startX, mirrorY, dup);
    }

    private void mirrorVertical(GraphicsContext gc, Color[][] brush, Color col,
                                int startX, int startY, int endX, int endY, int dup) {
        int mirrorX = (cols * cellSize) - startX - (endX - startX);
        drawPatternWithDuplicates(gc, brush, col, mirrorX, startY, dup);
    }

    private void mirrorDiagonal(GraphicsContext gc, Color[][] brush, Color col,
                                int startX, int startY, int endX, int endY, int dup) {
        int mirrorX = (cols * cellSize) - startX - (endX - startX);
        int mirrorY = (rows * cellSize) - startY - (endY - startY);
        drawPatternWithDuplicates(gc, brush, col, mirrorX, mirrorY, dup);
    }

    private void drawPatternWithDuplicates(GraphicsContext gc, Color[][] brush, Color col,
                                           int baseX, int baseY, int dup) {
        int brushWidth = brush[0].length * cellSize;
        int brushHeight = brush.length * cellSize;
        for (int i = 0; i < dup; i++) {
            int offsetX = calculateOffsetX(i, brushWidth);
            int offsetY = calculateOffsetY(i, brushHeight);
            drawBrushCopy(gc, brush, col,
                    baseX + offsetX,
                    baseY + offsetY);
        }
    }

    private void drawBrushCopy(GraphicsContext gc, Color[][] brush, Color col, int baseX, int baseY) {
        for (int r = 0; r < brush.length; r++) {
            for (int c = 0; c < brush[r].length; c++) {
                if (brush[r][c] != null) {
                    int x = baseX + c * cellSize;
                    int y = baseY + r * cellSize;
                    if (isWithinBounds(x, y)) {
                        gc.setFill(col);
                        gc.fillRect(x, y, cellSize, cellSize);
                        updatePreset(x, y, col);
                    }
                }
            }
        }
    }
    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < cols * cellSize &&
                y >= 0 && y < rows * cellSize;
    }

    private void updatePreset(int x, int y, Color color) {
        int row = y / cellSize;
        int col = x / cellSize;
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            loadedPreset[row][col] = color;
        }
    }

    private void resetGrid(GraphicsContext gc, Color[][] preset) {
        gc.clearRect(0, 0, cols * cellSize, rows * cellSize);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (loadedPreset != null) {
                    Color color = loadedPreset[row][col];
                    if (color != null) {
                        gc.setFill(color);
                        gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                    }
                }
            }
        }
    }

    private void saveEMB(Color[][] data, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int rows = data.length;
            int cols = data[0].length;
            writer.write(rows + " " + cols);
            writer.newLine();
            for (Color[] row : data) {
                for (Color color : row) {
                    if (color == null) {
                        writer.write("null ");
                    } else {
                        int r = (int) (color.getRed() * 255);
                        int g = (int) (color.getGreen() * 255);
                        int b = (int) (color.getBlue() * 255);
                        writer.write(r + "," + g + "," + b + " ");
                    }
                }
                writer.newLine();
            }
        }
    }

    private Color[][] loadEMB(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] size = reader.readLine().split(" ");
            int rows = Integer.parseInt(size[0]);
            int cols = Integer.parseInt(size[1]);
            Color[][] data = new Color[rows][cols];
            for (int r = 0; r < rows; r++) {
                String[] tokens = reader.readLine().split(" ");
                for (int c = 0; c < cols; c++) {
                    String token = tokens[c];
                    if (token.equals("null")) {
                        data[r][c] = null;
                    } else {
                        String[] rgb = token.split(",");
                        int red = Integer.parseInt(rgb[0]);
                        int green = Integer.parseInt(rgb[1]);
                        int blue = Integer.parseInt(rgb[2]);
                        data[r][c] = Color.rgb(red, green, blue);
                    }
                }
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void savePNG(Canvas canvas, File file) {
        WritableImage fxImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, fxImage);
        BufferedImage bImage = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        PixelReader pr = fxImage.getPixelReader();
        for (int y = 0; y < (int) canvas.getHeight(); y++) {
            for (int x = 0; x < (int) canvas.getWidth(); x++) {
                int argb = pr.getArgb(x, y);
                bImage.setRGB(x, y, argb);
            }
        }
        try {
            ImageIO.write(bImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}