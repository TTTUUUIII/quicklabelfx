package org.wkuwku.quicklabelfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HomeController {
    private final ToggleGroup mLabelGroup = new ToggleGroup();
    private final List<SampleLabel> mLabels = new ArrayList<>();

    private final ObservableList<HpdSample> mListItems = FXCollections.observableArrayList();

    @FXML
    private ToggleGroup mExportModeGroup;
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

    @FXML
    public void initialize() {
        mLabels.add(new SampleLabel("无人", "0"));
        mLabels.add(new SampleLabel("有人", "1"));
        mLabels.add(new SampleLabel("有人（唤醒）", "2"));
        for (int i = 0; i < mLabels.size(); ++i) {
            SampleLabel label = mLabels.get(i);
            Toggle toggle = addUiLabel(label);
            if (i == 0) {
                mLabelGroup.selectToggle(toggle);
            }
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
                            view.setImage(new Image(data.toUri().toString()));
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

        uiLabelColum.setCellValueFactory(cellData -> cellData.getValue().labelProperty());
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
                            item.setOnMouseClicked(event -> {
                                if (event.getButton() == MouseButton.PRIMARY) {
                                    onSetSampleLabelAction(event, getIndex());
                                }
                            });
                            view.getChildren().add(item);
                            item = new Hyperlink("删除");
                            item.getStyleClass().add("btn-text");
                            item.setOnAction(event -> {
                                onDeleteSample(getIndex());
                            });
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
        if (mListItems.isEmpty()) {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            chooser.setInitialDirectory(mListItems.get(0).getImgPath().getParent().getParent().toFile());
        }

        File directory = chooser.showDialog(window);
        if (directory != null) {
            ArrayList<HpdSample> samples = new ArrayList<>();
            File[] files = directory.listFiles(it -> it.getName().endsWith(".txt"));
            if (files == null) return;
            for (File file : files) {
                HpdSample sample = HpdSample.from(file.toPath());
                if (sample == null) continue;
                samples.add(sample);
            }
            if (!samples.isEmpty()) {
                samples.sort(Comparator.comparing(HpdSample::getId));
                mListItems.clear();
                mListItems.addAll(samples);
                updateChart();
            }
        }
    }

    @FXML
    private void onExportAction() throws IOException {
        if (mListItems.isEmpty()) {
            return;
        }
        if (uiProgressBar.getProgress() != 1.0) {
            showAlert(Alert.AlertType.WARNING, "警告", "有样本还未被标注，请检查！");
            return;
        }
        HpdSample hpdSample = mListItems.get(0);
        Path parent = hpdSample.getImgPath().getParent();
        Path out = parent.resolve(parent.toFile().getName() + "（已标注）");
        Files.createDirectory(out);
        for (HpdSample item : mListItems) {
            Path labelPath = out.resolve(item.getLabel().label);
            if (!labelPath.toFile().exists()) {
                Files.createDirectory(labelPath);
            }
            Toggle toggle = mExportModeGroup.getSelectedToggle();
            if (toggle == null || "copy".equals(toggle.getUserData())) {
                item.copyTo(labelPath);
            } else {
                item.moveTo(labelPath);
            }
        }
        showAlert("提示", "导出完成！");
    }

    @FXML
    private void onNewLabelAction() throws IOException {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.getButtonTypes().add(ButtonType.OK);
        alert.setTitle("添加标签");
        alert.setContentText(null);
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("layout-newlabel.fxml")));
        Node content = fxmlLoader.load();
        LabelController controller = fxmlLoader.getController();
        alert.getDialogPane().setContent(content);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SampleLabel label = controller.getLabel();
            mLabels.add(label);
            addUiLabel(label);
        }
    }

    @FXML
    private void onClearLabelsAction() {
        uiLabelContainer.getChildren().clear();
        mLabels.clear();
    }

    private void onSetSampleLabelAction(MouseEvent event, int index) {
        ToggleButton toggle = (ToggleButton) mLabelGroup.getSelectedToggle();
        if (toggle == null) return;
        SampleLabel label = getLabelByName(toggle.getText());
        if (label == null) return;
        int selectedIndex = uiTableView.getSelectionModel().getSelectedIndex();
        if (event.isShiftDown() && selectedIndex != -1) {
            int startIndex = Math.min(index, selectedIndex);
            int endIndex = index + selectedIndex - startIndex;
            for (int i = startIndex; i <= endIndex; i++) {
                HpdSample sample = mListItems.get(i);
                sample.setLabel(label);
            }
            updateChart();
        } else {
            HpdSample sample = mListItems.get(index);
            sample.setLabel(label);
            updateChart();
        }
    }

    private void onDeleteSample(int index) {
        mListItems.remove(index);
    }

    private void showAlert(String title, String msg) {
        showAlert(Alert.AlertType.INFORMATION, title, msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Toggle addUiLabel(@NonNull SampleLabel label) {
        ToggleButton toggle = new ToggleButton(label.name);
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/bookmark.png")));
        toggle.setGraphic(new ImageView(icon));
        toggle.setToggleGroup(mLabelGroup);
        uiLabelContainer.getChildren().add(toggle);
        return toggle;
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
        PieChart.Data slice = new PieChart.Data(SampleLabel.DEFAULT.name, count);
        uiPieChart.getData().clear();
        uiPieChart.getData().add(slice);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        uiBarChart.getData().clear();
        uiBarChart.getData().add(series);
        XYChart.Data<String, Number> data = new XYChart.Data<>(SampleLabel.DEFAULT.name, count);
        series.getData().add(data);
        uiProgressBar.setProgress((double) (mListItems.size() - count) / mListItems.size());
        uiProgressLabel.setText(String.format("%d/%d", mListItems.size() - count, mListItems.size()));
        for (SampleLabel label : mLabels) {
            count = mListItems.stream()
                    .filter(it -> it.getLabel().label.equals(label.label))
                    .count();
            slice = new PieChart.Data(label.name, count);
            uiPieChart.getData().add(slice);
            data = new XYChart.Data<>(label.name, count);
            series.getData().add(data);
        }
    }
}