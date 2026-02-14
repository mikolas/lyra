package net.mikolas.lyra.util;

/**
 * Utility methods for MIDI data manipulation.
 */
public class MidiUtils {

    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    /**
     * Converts a MIDI note number (0-127) to a string representation (e.g., "C3").
     */
    public static String noteName(int note) {
        int octave = (note / 12) - 2;
        String name = NOTE_NAMES[note % 12];
        return name + octave;
    }
}
