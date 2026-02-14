package net.mikolas.lyra.model;

import java.io.Serializable;

/**
 * Represents a single harmonic in spectral morphing.
 * Each harmonic has a number (1-50), wave type, and amplitude.
 */
public class Harmonic implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum WaveType {
        SINE, SQUARE, TRIANGLE, SAWTOOTH, INV_SAWTOOTH
    }
    
    private int number;        // 1-50
    private WaveType type;
    private double amplitude;  // 0.0-1.0
    
    public Harmonic(int number, WaveType type, double amplitude) {
        this.number = number;
        this.type = type;
        this.amplitude = Math.clamp(amplitude, 0.0, 1.0);
    }
    
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    
    public WaveType getType() { return type; }
    public void setType(WaveType type) { this.type = type; }
    
    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { 
        this.amplitude = Math.clamp(amplitude, 0.0, 1.0);
    }
    
    /**
     * Generate a single cycle of this harmonic's waveform.
     * @return 128 samples representing one cycle
     */
    public int[] generateWave() {
        int[] wave = new int[128];
        for (int i = 0; i < 128; i++) {
            double phase = (i / 128.0) * number; // Multiply frequency by harmonic number
            double value = switch (type) {
                case SINE -> Math.sin(phase * 2 * Math.PI);
                case SQUARE -> (phase % 1.0) < 0.5 ? 1.0 : -1.0;
                case TRIANGLE -> {
                    double p = phase % 1.0;
                    yield p < 0.5 ? (4 * p - 1) : (3 - 4 * p);
                }
                case SAWTOOTH -> 2 * (phase % 1.0) - 1;
                case INV_SAWTOOTH -> 1 - 2 * (phase % 1.0);
            };
            wave[i] = (int)(value * amplitude * 1048576); // Scale by amplitude and 21-bit range
        }
        return wave;
    }
}
