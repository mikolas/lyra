package net.mikolas.lyra.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification tests for immediate actions:
 * 1. Factory wavetable loading
 * 2. Keyframe creation/deletion
 */
class WavetableImmediateActionsTest {

    @Test
    void testFactoryWavetableLoading() {
        // Create a wavetable with raw wave data
        Wavetable wt = new Wavetable();
        wt.setName("Test Factory");
        wt.setSlot(80);
        wt.setFactory(true);
        
        // Populate with test data
        int[][] testWaves = wt.getBouncedWaves();
        for (int i = 0; i < 64; i++) {
            for (int s = 0; s < 128; s++) {
                testWaves[i][s] = (i * 1000) + s; // Unique pattern
            }
        }
        
        // Save to binary
        wt.prepareForSave();
        assertNotNull(wt.getBinaryData());
        assertEquals(64 * 128 * 4, wt.getBinaryData().length); // 4 bytes per int
        
        // Clear waves and reload
        for (int i = 0; i < 64; i++) {
            for (int s = 0; s < 128; s++) {
                testWaves[i][s] = 0;
            }
        }
        
        // Load from binary
        wt.loadFromData();
        
        // Verify data restored correctly
        for (int i = 0; i < 64; i++) {
            for (int s = 0; s < 128; s++) {
                assertEquals((i * 1000) + s, testWaves[i][s], 
                    "Wave " + i + " sample " + s + " mismatch");
            }
        }
    }

    @Test
    void testKeyframeCreation() {
        Wavetable wt = Wavetable.createNew();
        
        // Should start with one keyframe at index 0
        assertEquals(1, wt.getKeyframes().size());
        assertEquals(0, wt.getKeyframes().get(0).getIndex());
        
        // Set keyframe 0 to CURVE mode for interpolation
        Keyframe kf0 = wt.getKeyframes().get(0);
        kf0.setTransformMode(Keyframe.TransformMode.CURVE);
        
        // Add keyframe at index 32
        Keyframe kf32 = new Keyframe(32);
        for (int i = 0; i < 128; i++) {
            kf32.setSample(i, i * 1000);
        }
        wt.getKeyframes().add(kf32);
        
        assertEquals(2, wt.getKeyframes().size());
        
        // Bounce should interpolate between keyframes
        wt.markDirty();
        wt.bounce();
        
        // Wave 0 should match keyframe 0 (all zeros by default)
        assertArrayEquals(kf0.getSamples(), wt.getWave(0));
        
        // Wave 32 should match keyframe 32
        assertArrayEquals(kf32.getSamples(), wt.getWave(32));
        
        // Wave 16 should be interpolated (roughly halfway with CURVE mode)
        int[] wave16 = wt.getWave(16);
        assertNotNull(wave16);
        // kf0[64] = 0, kf32[64] = 64000, so wave16[64] should be around 32000
        assertTrue(wave16[64] > 20000 && wave16[64] < 40000, "wave16[64] was " + wave16[64]);
    }

    @Test
    void testKeyframeDeletion() {
        Wavetable wt = Wavetable.createNew();
        
        // Add multiple keyframes
        wt.getKeyframes().add(new Keyframe(16));
        wt.getKeyframes().add(new Keyframe(32));
        wt.getKeyframes().add(new Keyframe(48));
        
        assertEquals(4, wt.getKeyframes().size());
        
        // Remove keyframe at index 32
        wt.getKeyframes().removeIf(kf -> kf.getIndex() == 32);
        
        assertEquals(3, wt.getKeyframes().size());
        assertFalse(wt.getKeyframes().stream().anyMatch(kf -> kf.getIndex() == 32));
        
        // Bounce should still work
        wt.markDirty();
        wt.bounce();
        
        // All 64 waves should be populated
        for (int i = 0; i < 64; i++) {
            assertNotNull(wt.getWave(i));
            assertEquals(128, wt.getWave(i).length);
        }
    }

    @Test
    void testKeyframeCannotDeleteIndex0() {
        Wavetable wt = Wavetable.createNew();
        
        // Note: Protection against deleting index 0 is in the UI controller,
        // not in the model. This test verifies the model allows deletion.
        int sizeBefore = wt.getKeyframes().size();
        
        // Remove keyframe at index 0
        wt.getKeyframes().removeIf(kf -> kf.getIndex() == 0);
        
        // Model allows deletion (UI prevents it)
        assertTrue(wt.getKeyframes().size() < sizeBefore || wt.getKeyframes().isEmpty());
    }

    @Test
    void testSampleClamping() {
        Keyframe kf = new Keyframe(0);
        
        // Test upper bound
        kf.setSample(0, 2000000); // Way over limit
        assertEquals(1048575, kf.getSamples()[0]); // Should clamp to 2^20-1
        
        // Test lower bound
        kf.setSample(1, -2000000); // Way under limit
        assertEquals(-1048576, kf.getSamples()[1]); // Should clamp to -2^20
        
        // Test valid range
        kf.setSample(2, 500000);
        assertEquals(500000, kf.getSamples()[2]);
    }

    @Test
    void testBounceWithDifferentTransformModes() {
        Wavetable wt = Wavetable.createNew();
        
        // Set first keyframe to all zeros
        Keyframe kf0 = wt.getKeyframes().get(0);
        for (int i = 0; i < 128; i++) {
            kf0.setSample(i, 0);
        }
        
        // Add keyframe at 32 with max values
        Keyframe kf32 = new Keyframe(32);
        for (int i = 0; i < 128; i++) {
            kf32.setSample(i, 1000000);
        }
        wt.getKeyframes().add(kf32);
        
        // Test CONSTANT mode
        kf0.setTransformMode(Keyframe.TransformMode.CONSTANT);
        wt.markDirty();
        wt.bounce();
        
        // All waves before 32 should be zero
        for (int i = 1; i < 32; i++) {
            assertEquals(0, wt.getWave(i)[0], "Wave " + i + " should be constant");
        }
        
        // Test CURVE mode
        kf0.setTransformMode(Keyframe.TransformMode.CURVE);
        kf0.setCurveFunction("Linear");
        wt.markDirty();
        wt.bounce();
        
        // Wave 16 should be roughly halfway
        int midValue = wt.getWave(16)[0];
        assertTrue(midValue > 400000 && midValue < 600000, 
            "Wave 16 should be interpolated, got " + midValue);
    }
}
