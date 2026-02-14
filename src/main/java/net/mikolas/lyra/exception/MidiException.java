package net.mikolas.lyra.exception;

/**
 * Exception for MIDI-related errors.
 */
public class MidiException extends LyraException {
  public MidiException(String message) {
    super(message);
  }

  public MidiException(String message, Throwable cause) {
    super(message, cause);
  }
}
