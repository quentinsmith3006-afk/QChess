package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.direction.ChessDirection;

public record PinCheckInformation(Integer distFromOrigin, ChessDirection pinCheckDirection, ChessPiece pinnedPiece, ChessPiece pinnerPiece) {
}
