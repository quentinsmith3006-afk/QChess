package qchess.chess.create.exceptions;

public class ChessPieceCyclicalDependencyException extends RuntimeException {
  public ChessPieceCyclicalDependencyException(String message) {
    super(message);
  }
}
