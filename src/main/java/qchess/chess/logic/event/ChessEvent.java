package qchess.chess.logic.event;

import javafx.event.Event;
import javafx.event.EventType;
import qchess.chess.create.ChessPiece;
import qchess.chess.logic.ChessBoard;

public class ChessEvent extends Event {
    public static final EventType<Event> ANY = new EventType<>(Event.ANY,"CHESS_EVENT");
    public ChessPiece instigator;

    public ChessEvent(EventType<? extends ChessEvent> event, ChessPiece instigator) {
        super(event);

        this.instigator = instigator;
    }

    public ChessPiece getInstigator() {
        return instigator;
    }
}
