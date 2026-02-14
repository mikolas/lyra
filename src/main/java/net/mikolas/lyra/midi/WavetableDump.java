package net.mikolas.lyra.midi;

/**
 * WTBD - Wavetable Dump message (Single Wave).
 * Following the detailed technical specification for Blofeld Wavetable Editor.
 *
 * @param deviceId Device ID (0-127)
 * @param slot Wavetable slot (80-118 = User Wt. 1-39)
 * @param waveNumber Wave index (0-63)
 * @param samples 128 samples (21-bit signed integers)
 * @param name 14-character wavetable name
 */
public record WavetableDump(
    int deviceId, 
    int slot, 
    int waveNumber, 
    int[] samples, 
    String name
) implements MidiMessage {
    public WavetableDump {
        if (slot < 80 || slot > 118) {
            throw new IllegalArgumentException("Slot must be 80-118 (User Wt. 1-39)");
        }
        if (waveNumber < 0 || waveNumber > 63) {
            throw new IllegalArgumentException("Wave number must be 0-63");
        }
        if (samples.length != 128) {
            throw new IllegalArgumentException("Samples array must be exactly 128 elements");
        }
    }
}