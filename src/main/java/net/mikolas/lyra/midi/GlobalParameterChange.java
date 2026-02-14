package net.mikolas.lyra.midi;

/**
 * Global Parameter Change (GLBP) message.
 * Command: 0x05
 * Format: F0 3E 13 <dev> 05 <param_id> <value> F7
 */
public record GlobalParameterChange(int deviceId, int paramId, int value) implements MidiMessage {}
