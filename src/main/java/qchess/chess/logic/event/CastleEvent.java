package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class CastleEvent extends ChessEvent {
    public static EventType<CastleEvent> CASTLE = new EventType<>(ChessEvent.ANY, "CASTLE");
    private final ChessPiece conspirator;

    public CastleEvent(ChessPiece instigator, ChessPiece conspirator) {
        super(CASTLE, instigator);
        this.conspirator = conspirator;
    }

    public ChessPiece getConspirator() {
        return conspirator;
    }
}
