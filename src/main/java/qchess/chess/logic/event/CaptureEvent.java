package qchess.chess.logic.event;

import javafx.event.EventType;

public class CaptureEvent extends ChessEvent {
    private static final EventType<CaptureEvent> CAPTURE = new EventType<>(ChessEvent.ANY, "CAPTURE");

    public CaptureEvent() {
        super(CAPTURE);
    }
}
