package org.wkuwku.quicklabelfx;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LabelController {
    @FXML
    private TextField uiNameField;
    @FXML
    private TextField uiLabelField;

    public SampleLabel getLabel() {
        if (uiNameField.getText().isEmpty() || uiLabelField.getText().isEmpty()) return null;
        return new SampleLabel(uiNameField.getText(), uiLabelField.getText());
    }
}
