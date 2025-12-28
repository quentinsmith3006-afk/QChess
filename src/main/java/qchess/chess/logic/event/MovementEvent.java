package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;
import qchess.chess.logic.ChessBoard;

public class MovementEvent extends ChessEvent {
    public static final EventType<MovementEvent> MOVEMENT = new EventType<>(ChessEvent.ANY, "MOVEMENT");


    public MovementEvent(ChessPiece instigator) {
        super(MOVEMENT, instigator);
    }
}
