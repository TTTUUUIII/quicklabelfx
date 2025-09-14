package org.wkuwku.quicklabelfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HpdSample {
    private static final String[] IMG_FORMATS = new String[] {".jpg", ".png"};

    public final Path txtPath;
    private final Path imgPath;
    private final String id;

    private ObjectProperty<SampleLabel> label = new SimpleObjectProperty<>(SampleLabel.DEFAULT);
    private HpdSample(Path txtPath, Path imgPath) {
        this.txtPath = txtPath;
        this.imgPath = imgPath;
        id = imgPath.toString().substring(0, imgPath.toString().lastIndexOf("."));
    }

    public String getId() {
        return id;
    }

    public Path getImgPath() {
        return imgPath;
    }

    public ObjectProperty<SampleLabel> labelProperty() {
        return label;
    }

    public SampleLabel getLabel() {
        return label.get();
    }

    public void setLabel(SampleLabel label) {
        this.label.set(label);
    }

    public void moveTo(Path dirPath) throws IOException {
        String txtFileName = txtPath.toFile().getName();
        String imgFileName = imgPath.toFile().getName();
        Files.move(txtPath, dirPath.resolve(txtFileName));
        Files.move(imgPath, dirPath.resolve(imgFileName));
    }

    public void copyTo(Path dirPath) throws IOException {
        String txtFileName = txtPath.toFile().getName();
        String imgFileName = imgPath.toFile().getName();
        Files.copy(txtPath, dirPath.resolve(txtFileName));
        Files.copy(imgPath, dirPath.resolve(imgFileName));
    }

    @Override
    public String toString() {
        return "HpdSample{" +
                "txtPath=" + txtPath +
                ", imgPath=" + imgPath +
                ", id='" + id + '\'' +
                ", label=" + label +
                '}';
    }

    @Nullable
    public static HpdSample from(@NonNull Path txtPath) {
        File file = txtPath.toFile();
        String fileName = file.getName();
        if (!fileName.endsWith(".txt")) {
            return null;
        }
        Path imgPath = null;
        int index = fileName.lastIndexOf(".");
        for (String it: IMG_FORMATS) {
            Path tmp = txtPath.getParent()
                    .resolve(fileName.substring(0, index) + it);
            if (tmp.toFile().exists()) {
                imgPath = tmp;
                break;
            }
        }
        if (imgPath == null) return null;
        return new HpdSample(txtPath, imgPath);
    }
}
