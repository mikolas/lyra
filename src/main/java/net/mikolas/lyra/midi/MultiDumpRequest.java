package net.mikolas.lyra.midi;

/**
 * MULR - Multi Dump Request message.
 *
 * @param deviceId Device ID (0-127)
 * @param bank Bank number (0 or 127)
 * @param multi Multi number (0-127)
 */
public record MultiDumpRequest(int deviceId, int bank, int multi) implements MidiMessage {}
