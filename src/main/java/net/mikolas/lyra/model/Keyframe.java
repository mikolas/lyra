package net.mikolas.lyra.model;

import java.util.*;
import java.util.UUID;

/**
 * Represents a single keyframe in a wavetable.
 * Stores 128 samples as 21-bit signed integers.
 */
public class Keyframe implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private int index; // 0-63
    private final int[] samples = new int[128];
    private TransformMode transformMode = TransformMode.CONSTANT;
    private int translateOffset = 0;
    private String curveFunction = "Linear";
    private final List<Harmonic> harmonics = new ArrayList<>();

    public enum TransformMode {
        CONSTANT, CURVE, TRANSLATE, SPECTRAL
    }

    public Keyframe(int index) {
        this.id = UUID.randomUUID();
        this.index = index;
    }

    public UUID getId() { return id; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public int[] getSamples() { return samples; }
    
    public TransformMode getTransformMode() { return transformMode; }
    public void setTransformMode(TransformMode mode) { this.transformMode = mode; }

    public int getTranslateOffset() { return translateOffset; }
    public void setTranslateOffset(int offset) { this.translateOffset = offset; }

    public String getCurveFunction() { return curveFunction; }
    public void setCurveFunction(String function) { this.curveFunction = function; }
    
    public List<Harmonic> getHarmonics() { return harmonics; }
    
    public void setSample(int sampleIdx, int value) {
        if (sampleIdx >= 0 && sampleIdx < 128) {
            // Clamp to 21-bit signed range: -2^20 to 2^20-1
            samples[sampleIdx] = Math.clamp(value, -1048576, 1048575);
        }
    }
}
