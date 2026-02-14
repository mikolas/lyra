package net.mikolas.lyra.midi;

/**
 * MULD - Multi Dump Data message.
 *
 * @param deviceId Device ID (0-127)
 * @param bank Bank number (0 or 127)
 * @param multi Multi number (0-127)
 * @param data Multi configuration data
 */
public record MultiDumpData(int deviceId, int bank, int multi, byte[] data) implements MidiMessage {}
