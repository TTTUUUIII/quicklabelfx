package org.wkuwku.quicklabelfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class HomeController {
    private final ToggleGroup mLabelGroup = new ToggleGroup();
    private final List<SampleLabel> mLabels = new ArrayList<>();

    private final ObservableList<HpdSample> mListItems = FXCollections.observableArrayList();

    @FXML
    private FlowPane uiLabelContainer;
    @FXML
    private TableView<HpdSample> uiTableView;
    @FXML
    private TableColumn<HpdSample, Integer> uiIndexColumn;
    @FXML
    private TableColumn<HpdSample, Path> uiImageColum;
    @FXML
    private TableColumn<HpdSample, SampleLabel> uiLabelColum;
    @FXML
    private TableColumn<HpdSample, Integer> uiOperateColum;
    @FXML
    private PieChart uiPieChart;
    @FXML
    private BarChart<String, Number> uiBarChart;
    @FXML
    private ProgressBar uiProgressBar;
    @FXML
    private Label uiProgressLabel;

    private final HashMap<String, PieChart.Data> mPieSlice = new HashMap<>();
    private final HashMap<String, XYChart.Data<String, Number>> mBarSeries = new HashMap();

    @FXML
    public void initialize() {
        mLabels.add(new SampleLabel("无人", "0"));
        mLabels.add(new SampleLabel("有人", "1"));
        mLabels.add(new SampleLabel("有人（唤醒）", "2"));
        PieChart.Data slice = new PieChart.Data(SampleLabel.DEFAULT.name, 0);
        mPieSlice.put(SampleLabel.DEFAULT.name, slice);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        uiBarChart.getData().add(series);
        XYChart.Data<String, Number> data = new XYChart.Data<>(SampleLabel.DEFAULT.name, 0);
        series.getData().add(data);
        mBarSeries.put(SampleLabel.DEFAULT.name, data);

        uiPieChart.getData().add(slice);
        for (int i = 0; i < mLabels.size(); ++i) {
            SampleLabel label = mLabels.get(i);
            ToggleButton toggle = new ToggleButton(label.name);
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/bookmark.png")));
            toggle.setGraphic(new ImageView(icon));
            toggle.setToggleGroup(mLabelGroup);
            uiLabelContainer.getChildren().add(toggle);
            if (i == 0) {
                mLabelGroup.selectToggle(toggle);
            }
            slice = new PieChart.Data(label.name, 0);
            mPieSlice.put(label.name, slice);
            uiPieChart.getData().add(slice);
            data = new XYChart.Data<>(label.name, 0);
            series.getData().add(data);
            mBarSeries.put(label.name, data);
        }

        uiTableView.setItems(mListItems);
        uiImageColum.setCellValueFactory(new PropertyValueFactory<>("imgPath"));
        uiIndexColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HpdSample, Integer> call(TableColumn<HpdSample, Integer> col) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer index, boolean empty) {
                        super.updateItem(index, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(String.valueOf(getIndex() + 1));
                        }
                    }
                };
            }
        });
        uiImageColum.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HpdSample, Path> call(TableColumn<HpdSample, Path> col) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Path data, boolean empty) {
                        super.updateItem(data, empty);
                        if (empty || data == null) {
                            setGraphic(null);
                        } else {
                            StackPane pane = new StackPane();
                            pane.setAlignment(Pos.TOP_LEFT);
                            ImageView view = new ImageView();
                            view.setFitWidth(uiImageColum.getPrefWidth());
                            view.setFitHeight(uiTableView.getFixedCellSize());
                            view.setImage(new Image(data.toString()));
                            pane.getChildren().add(view);
                            String fileName = data.toFile().getName();
                            Label label = new Label(fileName.substring(0, fileName.lastIndexOf(".")));
                            label.getStyleClass().add("fill-warning");
                            label.getStyleClass().add("h6");
                            pane.getChildren().add(label);
                            setGraphic(pane);
                        }
                    }
                };
            }
        });

        uiLabelColum.setCellValueFactory(new PropertyValueFactory<>("label"));
        uiLabelColum.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HpdSample, SampleLabel> call(TableColumn<HpdSample, SampleLabel> hpdSampleStringTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(SampleLabel data, boolean empty) {
                        super.updateItem(data, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Label label = new Label();
                            label.setText(data.name);
                            ObservableList<String> styleClass = label.getStyleClass();
                            styleClass.add("badge");
                            if (data != SampleLabel.DEFAULT) {
                                styleClass.add("bg-success");
                            } else {
                                styleClass.add("bg-danger");
                            }
                            setGraphic(label);
                        }
                    }
                };
            }
        });

        uiOperateColum.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HpdSample, Integer> call(TableColumn<HpdSample, Integer> hpdSampleIntegerTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer index, boolean empty) {
                        super.updateItem(index, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox view = new HBox();
                            view.setAlignment(Pos.CENTER);
                            Hyperlink item = new Hyperlink("标注");
                            item.getStyleClass().add("btn-text");
                            item.setOnAction(event -> onLabelAction(getIndex()));
                            view.getChildren().add(item);
                            item = new Hyperlink("详情");
                            item.getStyleClass().add("btn-text");
                            view.getChildren().add(item);
                            setGraphic(view);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void onOpen() {
        Window window = uiLabelContainer.getScene().getWindow();

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("打开项目");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File directory = chooser.showDialog(window);
        if (directory != null) {
            mListItems.clear();
            File[] files = directory.listFiles(it -> it.getName().endsWith(".txt"));
            if (files == null) return;
            for (File file : files) {
                HpdSample sample = HpdSample.from(file.toPath());
                if (sample == null) continue;
                mListItems.add(sample);
            }
            if (!mListItems.isEmpty()) {
                updateChart();
            }
        }
    }

    @FXML
    private void onExportAction() {

    }

    @FXML
    private void onNewLabelAction() {

    }

    @FXML
    private void onClearLabelsAction() {
    }

    private void onLabelAction(int index) {
        ToggleButton toggle = (ToggleButton) mLabelGroup.getSelectedToggle();
        SampleLabel label = getLabelByName(toggle.getText());
        if (label != null) {
            HpdSample sample = mListItems.get(index);
            sample.setLabel(label);
            mListItems.set(index, sample);
            updateChart();
        }
    }

    private SampleLabel getLabelByName(@Nullable String name) {
        for (SampleLabel label : mLabels) {
            if (label.name.equals(name)) {
                return label;
            }
        }
        return null;
    }

    private void updateChart() {
        long count = mListItems.stream()
                .filter(it -> it.getLabel().label.equals(SampleLabel.DEFAULT.label))
                .count();
        mPieSlice.get(SampleLabel.DEFAULT.name)
                .setPieValue(count);
        uiPieChart.getData().set(0, mPieSlice.get(SampleLabel.DEFAULT.name));
        mBarSeries.get(SampleLabel.DEFAULT.name)
                .setYValue(count);
        uiProgressBar.setProgress((double) (mListItems.size() - count) / mListItems.size());
        uiProgressLabel.setText(String.format("%d/%d", mListItems.size() - count, mListItems.size()));
        for (SampleLabel label : mLabels) {
            count = mListItems.stream()
                    .filter(it -> it.getLabel().label.equals(label.name))
                    .count();
            mPieSlice.get(label.name)
                    .setPieValue(count);
            mBarSeries.get(label.name)
                    .setYValue(count);
        }
    }
}