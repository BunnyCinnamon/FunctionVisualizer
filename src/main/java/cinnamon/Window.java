package cinnamon;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public class Window {

    @FXML
    private TabPane tabId;
    @FXML
    private TextField text;
    @FXML
    private Button aaaa;

    public ConfigDSL.Config config;
    public String[] lines;
    public Path path;
    public Path file;

    int number = 0;

    @FXML
    public void reload(MouseEvent event) {
        try {
            lines = null;
            if (file != null) {
                try {
                    lines = Files.readAllLines(file, Charset.availableCharsets().get("UTF-8")).toArray(new String[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    lines = null;
                }
            }
            ExpressionHelper.EXPRESSION_CACHE.clear();
            ExpressionHelper.EXPRESSION_FUNCTION_CACHE.clear();
            number = tabId.getSelectionModel().getSelectedIndex();
            tabId.getTabs().clear();
            read();
        } catch (Exception e) {
            text.setText(e.toString());
        }
    }

    @FXML
    public void load(MouseEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(Objects.requireNonNullElseGet(path, () -> FileSystems.getDefault().getPath(System.getProperty("user.home"), "Desktop")).toFile());
            File file = fileChooser.showOpenDialog(App.stage);
            lines = null;
            if (file != null) {
                try {
                    this.file = file.toPath();
                    path = file.getParentFile().toPath();
                    lines = Files.readAllLines(file.toPath(), Charset.availableCharsets().get("UTF-8")).toArray(new String[0]);
                    text.setText(file.getAbsolutePath());
                    aaaa.setDisable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    lines = null;
                }
            }
            ExpressionHelper.EXPRESSION_CACHE.clear();
            ExpressionHelper.EXPRESSION_FUNCTION_CACHE.clear();
            number = 0;
            tabId.getTabs().clear();
            read();
        } catch (Exception e) {
            text.setText(e.toString());
        }
    }

    void read() {
        if (lines != null) {
            this.config = ConfigDSL.parse(lines);
            int max = config.max_level;
            int min = config.min_level;

            this.config.map.forEach((name, property) -> {
                NumberAxis x = new NumberAxis();
                NumberAxis y = new NumberAxis();
                x.setLabel("Level");
                x.setTickUnit(1);
                x.setAutoRanging(false);
                x.setLowerBound(min - 1);
                x.setUpperBound(max + 1);
                y.setAutoRanging(true);
                if (name.equals("DAMAGE_MIRROR")) {
                    y.setLabel("Percentage (%)");
                } else if (name.contains("DURATION") || name.contains("COOL") || name.contains("TIME") || name.contains("STUN") || name.contains("DELAY")) {
                    y.setLabel("Seconds (s)");
                } else if (name.contains("DAMAGE") || name.contains("DOT") || name.contains("HEALTH")) {
                    y.setLabel("Full Hearts (h)");
                } else if (name.contains("RANGE") || name.contains("SIZE") || name.contains("DISTANCE") || name.contains("FORCE") || name.contains("DISPLACEMENT")) {
                    y.setLabel("Blocks (b)");
                } else if (name.contains("REDUCTION") || name.contains("SPEED") || name.contains("MODIFIER")) {
                    y.setLabel("Percentage (%)");
                } else {
                    y.setLabel(name.toLowerCase(Locale.ROOT));
                }
                Tab tab = new Tab(name);
                tabId.getTabs().add(tab);

                AnchorPane pane = new AnchorPane();
                AnchorPane.setBottomAnchor(pane, 0D);
                AnchorPane.setTopAnchor(pane, 0D);
                AnchorPane.setLeftAnchor(pane, 0D);
                AnchorPane.setRightAnchor(pane, 0D);
                tab.setContent(pane);

                XYChart<Integer, Double> chart = (XYChart<Integer, Double>) (Object) new AreaChart<>(x, y);
                AnchorPane.setBottomAnchor(chart, 0D);
                AnchorPane.setTopAnchor(chart, 0D);
                AnchorPane.setLeftAnchor(chart, 0D);
                AnchorPane.setRightAnchor(chart, 0D);
                chart.setPadding(new Insets(5, 5, 5, 5));
                pane.getChildren().add(chart);

                tabId.getSelectionModel().select(number);

                XYChart.Series<Integer, Double> series = new AreaChart.Series<>();
                series.setName(name);
                series.getData().clear();
                for (int i = min; i <= max; i++) {
                    double value = config.get(name, i);
                    AreaChart.Data<Integer, Double> data = new AreaChart.Data<>(i, value);
                    data.setNode(new HoveredThresholdNode(i, y.getLabel(), value));
                    series.getData().add(data);
                }

                chart.getData().add(series);
                series.getNode().setStyle("-fx-stroke: blue;");
                tabId.applyCss();
            });
        }
    }

    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int i, String name, double value) {
            setPrefSize(5, 5);
            setOnMouseEntered(mouseEvent -> {
                text.setText("Level " + i + ": " + value + " " + name);
            });
            setOnMouseExited(mouseEvent -> {
                text.setText(file.toString());
            });
        }
    }
}
