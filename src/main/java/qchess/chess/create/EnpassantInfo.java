package qchess.chess.create;

/**
 * Information relating to enpassant.
 * @param coordinate the coordinate of the enpassant piece.
 * @param team the team of the enpassant piece.
 * @param leftChessPiece the chess piece left of the pawn that starts enpassant.
 * @param rightChessPiece the chess piece right of the pawn that starts enpassant.
 */
public record EnpassantInfo(Coordinate coordinate, Team team, ChessPiece leftChessPiece, ChessPiece rightChessPiece) {
}
