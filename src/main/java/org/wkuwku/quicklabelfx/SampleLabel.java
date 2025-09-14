package org.wkuwku.quicklabelfx;

public class SampleLabel {

    public static final SampleLabel DEFAULT = new SampleLabel("未标注", "NA");

    public final String name;
    public final String label;

    public SampleLabel(String name, String label) {
        this.name = name;
        this.label = label;
    }
}
