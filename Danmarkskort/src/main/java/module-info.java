module com.example.danmarkskort {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires itextpdf;
    requires jdk.jdi;
    requires trove4j;
    requires java.sql;
    requires org.junit.jupiter.api;

    opens com.example.danmarkskort to javafx.fxml;
    exports com.example.danmarkskort;
    exports com.example.danmarkskort.MapObjects;
    opens com.example.danmarkskort.MapObjects to javafx.fxml;
    exports com.example.danmarkskort.MVC;
    opens com.example.danmarkskort.MVC to javafx.fxml;
    exports com.example.danmarkskort.Exceptions;
    opens com.example.danmarkskort.Exceptions to javafx.fxml;
}