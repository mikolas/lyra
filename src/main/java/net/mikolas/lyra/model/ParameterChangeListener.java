package net.mikolas.lyra.model;

/**
 * Listener for parameter changes in a Sound model.
 * Used for real-time MIDI synchronization.
 */
@FunctionalInterface
public interface ParameterChangeListener {
    /**
     * Called when a parameter value changes.
     * @param paramId Absolute parameter ID (0-384)
     * @param value New value (0-127)
     */
    void onParameterChanged(int paramId, int value);
}
