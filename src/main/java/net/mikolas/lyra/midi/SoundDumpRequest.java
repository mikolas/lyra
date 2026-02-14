package net.mikolas.lyra.midi;

/**
 * SNDR - Sound Dump Request message.
 *
 * @param deviceId Device ID (0-127, typically 0x7F for broadcast)
 * @param bank Bank number (0-7 = A-H, 127 = edit buffer)
 * @param program Program number (0-127)
 */
public record SoundDumpRequest(int deviceId, int bank, int program) implements MidiMessage {}
