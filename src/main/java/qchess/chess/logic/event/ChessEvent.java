package qchess.chess.logic.event;

import javafx.event.Event;
import javafx.event.EventType;
import qchess.chess.logic.ChessBoard;

public class ChessEvent extends Event {
    public static final EventType<Event> ANY = new EventType<>(Event.ANY,"CHESS_EVENT");

    public ChessEvent(EventType<? extends ChessEvent> eventType) {
        super(eventType);
    }
}
