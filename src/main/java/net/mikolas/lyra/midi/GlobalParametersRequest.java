package net.mikolas.lyra.midi;

/**
 * GLBR - Global Parameters Request message.
 *
 * @param deviceId Device ID (0-127)
 */
public record GlobalParametersRequest(int deviceId) implements MidiMessage {}
