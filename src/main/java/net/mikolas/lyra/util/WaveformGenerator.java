package net.mikolas.lyra.util;

import net.mikolas.lyra.model.Wavetable;

/**
 * Generates basic factory wavetables (Pulse, Saw, Triangle, Sine).
 * These are simple single-cycle waveforms replicated across all 64 waves.
 */
public class WaveformGenerator {
    
    public static Wavetable createPulse() {
        Wavetable wt = new Wavetable();
        wt.setName("Pulse");
        wt.setSlot(1);
        wt.setFactory(true);
        
        int[] wave = new int[128];
        for (int i = 0; i < 64; i++) wave[i] = 1048576;  // High
        for (int i = 64; i < 128; i++) wave[i] = -1048576; // Low
        
        replicateWave(wt, wave);
        return wt;
    }
    
    public static Wavetable createSaw() {
        Wavetable wt = new Wavetable();
        wt.setName("Saw");
        wt.setSlot(2);
        wt.setFactory(true);
        
        int[] wave = new int[128];
        for (int i = 0; i < 128; i++) {
            wave[i] = (int)((i / 127.0 * 2 - 1) * 1048576);
        }
        
        replicateWave(wt, wave);
        return wt;
    }
    
    public static Wavetable createTriangle() {
        Wavetable wt = new Wavetable();
        wt.setName("Triangle");
        wt.setSlot(3);
        wt.setFactory(true);
        
        int[] wave = new int[128];
        for (int i = 0; i < 64; i++) {
            wave[i] = (int)((i / 63.0 * 2 - 1) * 1048576);
        }
        for (int i = 64; i < 128; i++) {
            wave[i] = (int)((1 - (i - 64) / 63.0 * 2) * 1048576);
        }
        
        replicateWave(wt, wave);
        return wt;
    }
    
    public static Wavetable createSine() {
        Wavetable wt = new Wavetable();
        wt.setName("Sine");
        wt.setSlot(4);
        wt.setFactory(true);
        
        int[] wave = new int[128];
        for (int i = 0; i < 128; i++) {
            wave[i] = (int)(Math.sin(i / 128.0 * 2 * Math.PI) * 1048576);
        }
        
        replicateWave(wt, wave);
        return wt;
    }
    
    private static void replicateWave(Wavetable wt, int[] wave) {
        for (int i = 0; i < 64; i++) {
            System.arraycopy(wave, 0, wt.getBouncedWaves()[i], 0, 128);
        }
    }
}
