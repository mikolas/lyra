package net.mikolas.lyra.exception;

/**
 * Exception for database-related errors.
 */
public class DatabaseException extends LyraException {
  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
