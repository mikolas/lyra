package net.mikolas.lyra.midi;

/**
 * SNDD - Sound Dump Data message.
 *
 * @param deviceId Device ID (0-127)
 * @param bank Bank number (0-7 = A-H, 127 = edit buffer)
 * @param program Program number (0-127)
 * @param parameters 385 parameter values (0-127 each)
 */
public record SoundDumpData(int deviceId, int bank, int program, byte[] parameters)
    implements MidiMessage {
  public SoundDumpData {
    if (parameters.length != 385) {
      throw new IllegalArgumentException("Must have exactly 385 parameters");
    }
  }
}
