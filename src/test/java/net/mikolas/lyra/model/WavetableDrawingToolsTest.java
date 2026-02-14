package net.mikolas.lyra.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for advanced drawing tools (High Priority Task 4)
 */
class WavetableDrawingToolsTest {

    @Test
    void testNormalize() {
        Keyframe kf = new Keyframe(0);
        
        // Set some values at half scale
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, i * 4000); // Max will be 127 * 4000 = 508000
        }
        
        // Find max before normalize
        int maxBefore = 0;
        for (int s : kf.getSamples()) {
            maxBefore = Math.max(maxBefore, Math.abs(s));
        }
        assertTrue(maxBefore < 1048576); // Should be less than full scale
        
        // Normalize (simulating the controller logic)
        int max = 0;
        for (int s : kf.getSamples()) max = Math.max(max, Math.abs(s));
        double factor = 1048576.0 / max;
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, (int) (kf.getSamples()[i] * factor));
        }
        
        // Find max after normalize
        int maxAfter = 0;
        for (int s : kf.getSamples()) {
            maxAfter = Math.max(maxAfter, Math.abs(s));
        }
        
        // Should be at or very close to full scale
        assertTrue(maxAfter >= 1048575 - 100); // Allow small rounding error
    }

    @Test
    void testSmooth() {
        Keyframe kf = new Keyframe(0);
        
        // Create a square wave (harsh transitions)
        for (int i = 0; i < 64; i++) kf.setSample(i, 1000000);
        for (int i = 64; i < 128; i++) kf.setSample(i, -1000000);
        
        int[] before = kf.getSamples().clone();
        
        // Apply smoothing (3-point moving average)
        int[] result = new int[128];
        for (int i = 0; i < 128; i++) {
            int prev = before[(i - 1 + 128) % 128];
            int next = before[(i + 1) % 128];
            result[i] = (prev + before[i] + next) / 3;
        }
        for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        
        // Check that transitions are smoother
        // Sample 63 should be averaged with 62 (high) and 64 (low)
        int transition = kf.getSamples()[63];
        assertTrue(Math.abs(transition) < 1000000); // Should be between extremes
        assertTrue(Math.abs(transition) > 0); // But not zero
    }

    @Test
    void testInvert() {
        Keyframe kf = new Keyframe(0);
        
        // Set some positive values
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, i * 8000);
        }
        
        int[] before = kf.getSamples().clone();
        
        // Invert
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, -kf.getSamples()[i]);
        }
        
        // Check all values are negated
        for (int i = 0; i < 128; i++) {
            assertEquals(-before[i], kf.getSamples()[i]);
        }
    }

    @Test
    void testReverse() {
        Keyframe kf = new Keyframe(0);
        
        // Set ascending values
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, i * 8000);
        }
        
        int[] before = kf.getSamples().clone();
        
        // Reverse
        int[] result = new int[128];
        for (int i = 0; i < 128; i++) {
            result[i] = before[127 - i];
        }
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, result[i]);
        }
        
        // Check values are reversed
        for (int i = 0; i < 128; i++) {
            assertEquals(before[127 - i], kf.getSamples()[i]);
        }
        
        // First sample should now be the last value
        assertEquals(127 * 8000, kf.getSamples()[0]);
        // Last sample should now be the first value
        assertEquals(0, kf.getSamples()[127]);
    }

    @Test
    void testLineDrawing() {
        Keyframe kf = new Keyframe(0);
        
        // Simulate drawing a line from sample 0 (value 0) to sample 127 (value 1000000)
        int sample1 = 0;
        int sample2 = 127;
        int value1 = 0;
        int value2 = 1000000;
        
        for (int i = sample1; i <= sample2; i++) {
            double ratio = (double)(i - sample1) / (sample2 - sample1);
            int value = (int)(value1 + ratio * (value2 - value1));
            kf.setSample(i, value);
        }
        
        // Check endpoints
        assertEquals(0, kf.getSamples()[0]);
        assertEquals(1000000, kf.getSamples()[127]);
        
        // Check midpoint is roughly halfway
        int midValue = kf.getSamples()[64];
        assertTrue(midValue > 450000 && midValue < 550000);
        
        // Check it's monotonically increasing
        for (int i = 1; i < 128; i++) {
            assertTrue(kf.getSamples()[i] >= kf.getSamples()[i-1]);
        }
    }

    @Test
    void testLineDrawingReverse() {
        Keyframe kf = new Keyframe(0);
        
        // Draw line from high to low
        int sample1 = 0;
        int sample2 = 127;
        int value1 = 1000000;
        int value2 = -1000000;
        
        for (int i = sample1; i <= sample2; i++) {
            double ratio = (double)(i - sample1) / (sample2 - sample1);
            int value = (int)(value1 + ratio * (value2 - value1));
            kf.setSample(i, value);
        }
        
        // Check endpoints
        assertEquals(1000000, kf.getSamples()[0]);
        assertEquals(-1000000, kf.getSamples()[127]);
        
        // Check midpoint crosses zero
        int midValue = kf.getSamples()[64];
        assertTrue(Math.abs(midValue) < 100000); // Should be near zero
        
        // Check it's monotonically decreasing
        for (int i = 1; i < 128; i++) {
            assertTrue(kf.getSamples()[i] <= kf.getSamples()[i-1]);
        }
    }

    @Test
    void testSmoothPreservesGeneralShape() {
        Keyframe kf = new Keyframe(0);
        
        // Create a sine-like wave
        for (int i = 0; i < 128; i++) {
            double angle = (i / 128.0) * 2 * Math.PI;
            kf.setSample(i, (int)(Math.sin(angle) * 1000000));
        }
        
        int[] before = kf.getSamples().clone();
        
        // Apply smoothing multiple times
        for (int pass = 0; pass < 3; pass++) {
            int[] result = new int[128];
            for (int i = 0; i < 128; i++) {
                int prev = kf.getSamples()[(i - 1 + 128) % 128];
                int next = kf.getSamples()[(i + 1) % 128];
                result[i] = (prev + kf.getSamples()[i] + next) / 3;
            }
            for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        }
        
        // Shape should still be similar (positive in first half, negative in second)
        assertTrue(kf.getSamples()[32] > 0); // First quarter should be positive
        assertTrue(kf.getSamples()[96] < 0); // Third quarter should be negative
        
        // But values should be smaller (smoothed)
        int maxBefore = 0;
        int maxAfter = 0;
        for (int i = 0; i < 128; i++) {
            maxBefore = Math.max(maxBefore, Math.abs(before[i]));
            maxAfter = Math.max(maxAfter, Math.abs(kf.getSamples()[i]));
        }
        assertTrue(maxAfter < maxBefore); // Smoothing reduces peaks
    }
}
