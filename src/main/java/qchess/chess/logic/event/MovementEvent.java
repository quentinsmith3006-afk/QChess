package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.logic.ChessBoard;

public class MovementEvent extends ChessEvent {
    public static final EventType<ChessEvent> MOVEMENT = new EventType<>(ChessEvent.ANY, "MOVEMENT");

    public MovementEvent() {
        super(MOVEMENT);
    }

}
