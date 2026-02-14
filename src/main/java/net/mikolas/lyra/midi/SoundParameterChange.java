package net.mikolas.lyra.midi;

/**
 * SNDP - Sound Parameter Change message.
 *
 * @param deviceId Device ID (0-127, typically 0x7F for broadcast)
 * @param location Location (0x00 for edit buffer)
 * @param paramId Parameter ID (0-384)
 * @param value Parameter value (0-127)
 */
public record SoundParameterChange(int deviceId, int location, int paramId, int value)
    implements MidiMessage {
  public SoundParameterChange {
    if (paramId < 0 || paramId > 384) {
      throw new IllegalArgumentException("Parameter ID must be 0-384");
    }
    if (value < 0 || value > 127) {
      throw new IllegalArgumentException("Value must be 0-127");
    }
  }
}
