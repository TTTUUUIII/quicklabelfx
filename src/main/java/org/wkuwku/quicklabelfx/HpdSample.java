package org.wkuwku.quicklabelfx;

import java.io.File;
import java.nio.file.Path;

public class HpdSample {
    private static final String[] IMG_FORMATS = new String[] {
            ".jpg", ".png"
    };

    public final Path txtPath;
    private final Path imgPath;

    private SampleLabel label = SampleLabel.DEFAULT;
    private HpdSample(Path txtPath, Path imgPath) {
        this.txtPath = txtPath;
        this.imgPath = imgPath;
    }

    public Path getImgPath() {
        return imgPath;
    }

    public SampleLabel getLabel() {
        return label;
    }

    public void setLabel(SampleLabel label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "HpdSample{" +
                "txtPath=" + txtPath +
                ", imgPath=" + imgPath +
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
