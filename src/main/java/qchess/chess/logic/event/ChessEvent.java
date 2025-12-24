package qchess.chess.logic.event;

import javafx.event.Event;
import javafx.event.EventType;
import qchess.chess.create.ChessPiece;
import qchess.chess.logic.ChessBoard;

public class ChessEvent extends Event {
    public static final EventType<Event> ANY = new EventType<>(Event.ANY,"CHESS_EVENT");
    public ChessPiece chessPiece;

    public ChessEvent(EventType<? extends ChessEvent> eventType, ChessPiece chessPiece) {
        super(eventType);

        this.chessPiece = chessPiece;
    }

    public ChessPiece getChessPiece() {
        return chessPiece;
    }
}
