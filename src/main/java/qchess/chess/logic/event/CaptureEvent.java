package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class CaptureEvent extends ChessEvent {
    private static final EventType<CaptureEvent> CAPTURE = new EventType<>(ChessEvent.ANY, "CAPTURE");
    private final ChessPiece capturedPiece;

    public CaptureEvent(ChessPiece instigator, ChessPiece capturedPiece) {
        super(CAPTURE, instigator);
        this.capturedPiece = capturedPiece;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
}
