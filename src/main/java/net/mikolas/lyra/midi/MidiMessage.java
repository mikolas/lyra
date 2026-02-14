package net.mikolas.lyra.midi;

/**
 * Sealed interface for all Blofeld MIDI messages.
 */
public sealed interface MidiMessage
    permits SoundParameterChange,
        SoundDumpRequest,
        SoundDumpData,
        MultiDumpRequest,
        MultiDumpData,
        GlobalParametersRequest,
        GlobalParametersData,
        GlobalParameterChange,
        WavetableDump,
        DeviceIdentityRequest {}
