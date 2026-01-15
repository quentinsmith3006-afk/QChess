module qchess.chess {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;

    exports qchess.chess.chessmen;
    exports qchess.chess.create.direction;
    exports qchess.chess.create.interfaces;
    exports qchess.chess.create.piecemodifiers;
    exports qchess.chess.create;
    exports qchess.chess.logic.event;
    exports qchess.chess.logic;
    exports qchess.chess.logic.promotion;

}