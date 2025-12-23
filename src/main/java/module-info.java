module qchess.chess {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.base;

    opens qchess.chess to javafx.fxml;
    exports qchess.chess;
}