package com.drugarybna.embroidery_ultra;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
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

public class EmbroideryApp extends Application {

    private final int rows = 32;
    private final int cols = 32;
    private final int cellSize = 16;

    Color[][] loadedPreset = new Color[rows][cols];
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
        symmetryType.getItems().addAll("Horizontal", "Vertical");
        symmetryType.setValue("Horizontal");
        symmetrySwitch.setOnAction(e -> symmetryType.setDisable(!symmetrySwitch.isSelected()) );
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

        VBox tools = new VBox(colorPicker, symmetryPane, brushTypePane);
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
            drawBrush(gc, selectedBrush, currentColor, col * cellSize, row * cellSize, symmetrySwitch.isSelected());
        });

        BorderPane root = new BorderPane();
        root.setLeft(tools);
        root.setRight(canvasPane);

        Scene scene = new Scene(root, 800, 600);

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
                try {
                    loadedPreset = loadEMB(file);
                    resetGrid(gc, loadedPreset);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
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

    void drawBrush(GraphicsContext gc, Color[][] brush, Color col, int startX, int startY, boolean symmetry) {
        for (int r = 0; r < brush.length; r++) {
            for (int c = 0; c < brush[r].length; c++) {
                Color color = brush[r][c];
                if (color != null) {
                    gc.setFill(col);
                    gc.fillRect(startX + c * 16, startY + r * 16, 16, 16);
                    int presetRow = (startY / 16) + r;
                    int presetCol = (startX / 16) + c;
                    if (presetRow >= 0 && presetRow < loadedPreset.length &&
                            presetCol >= 0 && presetCol < loadedPreset[0].length) {
                        loadedPreset[presetRow][presetCol] = col;
                    }
                    if (symmetry) {
                        if (selectedSymmetry.equals("Horizontal")) {
                            int mirrorRow = (rows - 1) - presetRow;
                            if (mirrorRow >= 0 && mirrorRow < loadedPreset.length) {
                                gc.fillRect(startX + c * 16, (rows-1)*cellSize - startY - r * 16, 16, 16);
                                loadedPreset[mirrorRow][presetCol] = col;
                            }
                        } else if (selectedSymmetry.equals("Vertical")) {
                            int mirrorCol = (cols - 1) - presetCol;
                            if (mirrorCol >= 0 && mirrorCol < loadedPreset[0].length) {
                                gc.fillRect((cols-1)*cellSize - startX - r * 16, startY + c * 16, 16, 16);
                                loadedPreset[presetRow][mirrorCol] = col;
                            }
                        }
                    }
                }
            }
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

    private Color[][] loadEMB(File file) throws IOException {
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
        }
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