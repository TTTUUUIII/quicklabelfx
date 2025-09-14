module org.wkuwku.quicklabelfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.wkuwku.quicklabelfx to javafx.fxml;
    exports org.wkuwku.quicklabelfx;
}