package net.mikolas.lyra.model;

/**
 * MIDI filter mode for send/receive.
 */
public enum MidiFilterMode {
    NONE,
    CTRL_SYSEX,
    PROGRAM_CHANGES,
    ALL_EVENTS
}
