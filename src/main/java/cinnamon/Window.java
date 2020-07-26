package cinnamon;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;

public class Window {

    @FXML
    public Button remove;
    @FXML
    public TextField name;
    @FXML
    public ListView<String> listView;
    @FXML
    public Spinner<Integer> spinnerTo;
    @FXML
    public Spinner<Integer> spinnerFrom;
    @FXML
    public Spinner<Integer> spinnerMax;
    @FXML
    public AreaChart<Integer, Double> areaChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    public int row = -1;

    public void initialize() {
        listView.setCellFactory(TextFieldListCell.forListView());
        listView.setOnEditCommit(t -> listView.getItems().set(t.getIndex(), t.getNewValue()));
        listView.setOnMouseClicked(t -> {
            row = listView.getSelectionModel().getSelectedIndex();
            remove.setDisable(false);
        });
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(0);
        xAxis.setLabel("Level");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(0);
    }

    @FXML
    public void paste(ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String s = clipboard.getString();
            String[] rows = s.split("\n");
            if (rows.length > 0) {
                listView.getItems().clear();
                for (String row : rows) {
                    listView.getItems().add(row.trim());
                }
            }
        }
        remove.setDisable(true);
        row = -1;
    }

    @FXML
    public void insert(ActionEvent event) {
        if (!name.getText().isEmpty() && !listView.getItems().isEmpty()) {
            String[] expressions = listView.getItems().toArray(new String[0]);
            int max = spinnerTo.getValue();
            int min = spinnerFrom.getValue();
            int total = spinnerMax.getValue();
            if(max < min) return;

            XYChart.Series<Integer, Double> series = areaChart.getData().stream().filter(d -> d.getName().equals(name.getText())).findFirst().orElse(new AreaChart.Series<>());
            series.setName(name.getText());
            series.getData().clear();
            for (int i = min; i <= Math.min(max, min + 10000); i++) {
                double value = ExpressionHelper.getExpression(expressions, i, total);
                AreaChart.Data<Integer, Double> data = new AreaChart.Data<>(i, value);
                data.setNode(new HoveredThresholdNode(i, value));
                series.getData().add(data);
            }
            if (!areaChart.getData().contains(series)) {
                areaChart.getData().add(series);
            }
            if (xAxis.getLowerBound() == 0 || xAxis.getLowerBound() > min) {
                xAxis.setLowerBound(min);
            }
            if (xAxis.getUpperBound() < Math.min(max, min + 10000)) {
                xAxis.setUpperBound(Math.min(max, min + 10000));
            }
            if (yAxis.getLowerBound() == 0 || yAxis.getLowerBound() > ExpressionHelper.getExpression(expressions, min, total)) {
                yAxis.setLowerBound(ExpressionHelper.getExpression(expressions, min, total));
            }
            if (yAxis.getUpperBound() < ExpressionHelper.getExpression(expressions, Math.min(max, min + 10000), total)) {
                yAxis.setUpperBound(ExpressionHelper.getExpression(expressions, Math.min(max, min + 10000), total));
            }
            remove.setDisable(true);
            row = -1;
        }
    }

    @FXML
    public void reset(ActionEvent event) {
        areaChart.getData().clear();
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(0);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(0);
    }

    @FXML
    public void add(ActionEvent event) {
        int index = 0;
        if (row != -1) {
            listView.getItems().add(row + 1, "");
            index = row;
        } else {
            listView.getItems().add("");
        }
        listView.getSelectionModel().clearSelection();
        listView.getSelectionModel().select(index);
        listView.scrollTo(index);
        remove.setDisable(true);
        row = -1;
    }

    @FXML
    public void remove(ActionEvent event) {
        if (row != -1) {
            listView.getItems().remove(row);
            remove.setDisable(true);
            row = -1;
        }
    }

    @FXML
    public void clear(ActionEvent event) {
        listView.getItems().clear();
        remove.setDisable(true);
        row = -1;
    }

    static class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int i, double value) {
            setPrefSize(5, 5);
            Label label = createDataThresholdLabel(i, value);
            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        private Label createDataThresholdLabel(int i, double value) {
            final Label label = new Label(i + " -> " + value);
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
            return label;
        }
    }
}
