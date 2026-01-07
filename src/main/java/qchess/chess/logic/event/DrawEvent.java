package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class DrawEvent extends ChessEvent {
    public static EventType<DrawEvent> DRAW = new EventType<>(ChessEvent.ANY, "DRAW");

    public DrawEvent(ChessPiece chessPiece) {
        super(DRAW, chessPiece);
    }
}
