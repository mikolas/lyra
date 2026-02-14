package net.mikolas.lyra.exception;

/**
 * Base exception for all Lyra-specific errors.
 */
public class LyraException extends Exception {
  public LyraException(String message) {
    super(message);
  }

  public LyraException(String message, Throwable cause) {
    super(message, cause);
  }
}
