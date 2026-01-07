module qchess.chess {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.base;
    requires org.jetbrains.annotations;
    requires java.desktop;

    opens qchess.chess to javafx.fxml;
    opens qchess.chess.logic to javafx.fxml;
}