module com.drugarybna.embroidery_ultra {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.drugarybna.embroidery_ultra to javafx.fxml;
    exports com.drugarybna.embroidery_ultra;
}