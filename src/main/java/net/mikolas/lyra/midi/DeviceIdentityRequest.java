package net.mikolas.lyra.midi;

/**
 * Device Identity Request message (Universal SysEx).
 *
 * @param deviceId Device ID (0x7F = all devices)
 */
public record DeviceIdentityRequest(int deviceId) implements MidiMessage {}
