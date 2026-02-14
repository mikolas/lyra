package net.mikolas.lyra.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for spectral morphing (High Priority Task 3)
 */
class WavetableSpectralMorphingTest {

    @Test
    void testHarmonicGeneration() {
        // Test sine wave generation
        Harmonic sine = new Harmonic(1, Harmonic.WaveType.SINE, 1.0);
        int[] wave = sine.generateWave();
        
        assertEquals(128, wave.length);
        
        // Check sine wave properties
        assertEquals(0, wave[0], 1000); // Should start near zero
        assertTrue(wave[32] > 1000000); // Peak around quarter cycle
        assertEquals(0, wave[64], 1000); // Zero crossing at half cycle
        assertTrue(wave[96] < -1000000); // Trough at three-quarter cycle
    }

    @Test
    void testHarmonicAmplitude() {
        // Test amplitude scaling
        Harmonic half = new Harmonic(1, Harmonic.WaveType.SINE, 0.5);
        int[] wave = half.generateWave();
        
        // Peak should be around half of full scale
        int max = 0;
        for (int s : wave) max = Math.max(max, Math.abs(s));
        assertTrue(max > 400000 && max < 600000); // ~0.5 * 1048576
    }

    @Test
    void testHarmonicNumber() {
        // Harmonic 2 should have twice the frequency
        Harmonic h2 = new Harmonic(2, Harmonic.WaveType.SINE, 1.0);
        int[] wave = h2.generateWave();
        
        // Should have 2 complete cycles in 128 samples
        // Zero crossings at 0, 32, 64, 96
        assertEquals(0, wave[0], 1000);
        assertEquals(0, wave[32], 1000);
        assertEquals(0, wave[64], 1000);
        assertEquals(0, wave[96], 1000);
    }

    @Test
    void testSquareWave() {
        Harmonic square = new Harmonic(1, Harmonic.WaveType.SQUARE, 1.0);
        int[] wave = square.generateWave();
        
        // First half should be positive
        assertTrue(wave[16] > 0);
        assertTrue(wave[32] > 0);
        
        // Second half should be negative
        assertTrue(wave[80] < 0);
        assertTrue(wave[112] < 0);
    }

    @Test
    void testTriangleWave() {
        Harmonic triangle = new Harmonic(1, Harmonic.WaveType.TRIANGLE, 1.0);
        int[] wave = triangle.generateWave();
        
        // Triangle: starts at -1, ramps to +1 at midpoint, back to -1
        assertTrue(wave[0] < wave[16]);   // -1048576 < -524288
        assertTrue(wave[16] < wave[32]);  // -524288 < 0
        assertTrue(wave[32] < wave[48]);  // 0 < 524288
        assertTrue(wave[48] < wave[64]);  // 524288 < 1048576
        assertTrue(wave[64] > wave[80]);  // 1048576 > 524288 (going back down)
    }

    @Test
    void testAdditiveKeyframe() {
        Keyframe kf = new Keyframe(0);
        
        // Add fundamental and 3rd harmonic
        kf.getHarmonics().add(new Harmonic(1, Harmonic.WaveType.SINE, 1.0));
        kf.getHarmonics().add(new Harmonic(3, Harmonic.WaveType.SINE, 0.3));
        
        assertEquals(2, kf.getHarmonics().size());
        
        // Generate combined wave
        int[] result = new int[128];
        for (Harmonic h : kf.getHarmonics()) {
            int[] harmonicWave = h.generateWave();
            for (int i = 0; i < 128; i++) {
                result[i] = Math.clamp(result[i] + harmonicWave[i], -1048576, 1048575);
            }
        }
        
        // Result should have significant amplitude (harmonics add together)
        int max = 0;
        for (int s : result) max = Math.max(max, Math.abs(s));
        assertTrue(max > 900000, "Max amplitude was " + max); // Should be close to full scale
    }

    @Test
    void testSpectralMorphing() {
        Wavetable wt = Wavetable.createNew();
        
        // Keyframe 0: Base sine wave
        Keyframe kf0 = wt.getKeyframes().get(0);
        for (int i = 0; i < 128; i++) {
            double phase = (i / 128.0) * 2 * Math.PI;
            kf0.setSample(i, (int)(Math.sin(phase) * 1000000));
        }
        
        // Add harmonics to keyframe 0
        kf0.getHarmonics().add(new Harmonic(2, Harmonic.WaveType.SINE, 0.5));
        kf0.setTransformMode(Keyframe.TransformMode.SPECTRAL);
        
        // Keyframe 32: Different wave
        Keyframe kf32 = new Keyframe(32);
        for (int i = 0; i < 128; i++) {
            kf32.setSample(i, 500000); // Constant value
        }
        wt.getKeyframes().add(kf32);
        
        // Bounce to calculate interpolated waves
        wt.markDirty();
        wt.bounce();
        
        // Wave 16 should be interpolated with harmonics fading out
        int[] wave16 = wt.getWave(16);
        assertNotNull(wave16);
        
        // Should have some variation (not constant)
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int s : wave16) {
            min = Math.min(min, s);
            max = Math.max(max, s);
        }
        assertTrue(max - min > 100000); // Should have significant variation
    }

    @Test
    void testSpectralMorphFadeOut() {
        Wavetable wt = Wavetable.createNew();
        
        Keyframe kf0 = wt.getKeyframes().get(0);
        kf0.getHarmonics().add(new Harmonic(1, Harmonic.WaveType.SINE, 1.0));
        kf0.setTransformMode(Keyframe.TransformMode.SPECTRAL);
        
        Keyframe kf32 = new Keyframe(32);
        for (int i = 0; i < 128; i++) kf32.setSample(i, 0);
        wt.getKeyframes().add(kf32);
        
        wt.markDirty();
        wt.bounce();
        
        // Harmonics should fade out as we approach keyframe 32
        int[] wave8 = wt.getWave(8);
        int[] wave24 = wt.getWave(24);
        
        int max8 = 0;
        int max24 = 0;
        for (int i = 0; i < 128; i++) {
            max8 = Math.max(max8, Math.abs(wave8[i]));
            max24 = Math.max(max24, Math.abs(wave24[i]));
        }
        
        // Wave 24 should have less amplitude than wave 8 (harmonics fading)
        assertTrue(max24 < max8);
    }

    @Test
    void testHarmonicClamping() {
        Keyframe kf = new Keyframe(0);
        
        // Add many loud harmonics
        for (int i = 1; i <= 10; i++) {
            kf.getHarmonics().add(new Harmonic(i, Harmonic.WaveType.SINE, 1.0));
        }
        
        // Generate combined wave
        int[] result = new int[128];
        for (Harmonic h : kf.getHarmonics()) {
            int[] harmonicWave = h.generateWave();
            for (int i = 0; i < 128; i++) {
                result[i] = Math.clamp(result[i] + harmonicWave[i], -1048576, 1048575);
            }
        }
        
        // All values should be within 21-bit range
        for (int s : result) {
            assertTrue(s >= -1048576 && s <= 1048575);
        }
    }
}
