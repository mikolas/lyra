package net.mikolas.lyra.midi;

/**
 * GLBD - Global Parameters Data message.
 *
 * @param deviceId Device ID (0-127)
 * @param data Global parameters data
 */
public record GlobalParametersData(int deviceId, byte[] data) implements MidiMessage {}
