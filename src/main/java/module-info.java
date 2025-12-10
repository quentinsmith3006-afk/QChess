module qchess.chess.qchessv2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens qchess.chess to javafx.fxml;
    exports qchess.chess;
}