module com.example.towerofhanoi {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens com.example.towerofhanoi to javafx.fxml;
    exports com.example.towerofhanoi;
}