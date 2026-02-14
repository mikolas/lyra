package net.mikolas.lyra.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Wavetable undo/redo functionality.
 */
class WavetableUndoRedoTest {

  private Wavetable wavetable;

  @BeforeEach
  void setUp() {
    wavetable = new Wavetable();
    wavetable.setName("Test Wavetable");
  }

  @Test
  void testCanUndoInitiallyFalse() {
    assertFalse(wavetable.canUndo());
  }

  @Test
  void testCanRedoInitiallyFalse() {
    assertFalse(wavetable.canRedo());
  }

  @Test
  void testCanUndoAfterModification() {
    // Modify wave data
    int[] waveData = new int[128];
    for (int i = 0; i < 128; i++) {
      waveData[i] = i * 100;
    }
    wavetable.setWave(0, waveData);

    assertTrue(wavetable.canUndo());
  }

  @Test
  void testUndoRestoresPreviousState() {
    // Get initial state
    int[] originalWave = wavetable.getWave(0).clone();

    // Modify wave
    int[] newWave = new int[128];
    for (int i = 0; i < 128; i++) {
      newWave[i] = i * 100;
    }
    wavetable.setWave(0, newWave);

    // Undo
    wavetable.undo();

    // Verify restored
    assertArrayEquals(originalWave, wavetable.getWave(0));
  }

  @Test
  void testRedoReappliesChange() {
    // Modify wave
    int[] newWave = new int[128];
    for (int i = 0; i < 128; i++) {
      newWave[i] = i * 100;
    }
    wavetable.setWave(0, newWave);

    // Undo then redo
    wavetable.undo();
    wavetable.redo();

    // Verify change reapplied
    assertArrayEquals(newWave, wavetable.getWave(0));
  }

  @Test
  void testCanRedoAfterUndo() {
    // Modify and undo
    int[] newWave = new int[128];
    wavetable.setWave(0, newWave);
    wavetable.undo();

    assertTrue(wavetable.canRedo());
  }

  @Test
  void testCannotRedoAfterNewChange() {
    // Modify, undo, then make new change
    wavetable.setWave(0, new int[128]);
    wavetable.undo();

    int[] anotherWave = new int[128];
    for (int i = 0; i < 128; i++) {
      anotherWave[i] = i * 50;
    }
    wavetable.setWave(0, anotherWave);

    assertFalse(wavetable.canRedo());
  }

  @Test
  void testMultipleUndos() {
    // Make three changes
    int[] wave1 = new int[128];
    int[] wave2 = new int[128];
    int[] wave3 = new int[128];

    for (int i = 0; i < 128; i++) {
      wave1[i] = i * 10;
      wave2[i] = i * 20;
      wave3[i] = i * 30;
    }

    wavetable.setWave(0, wave1);
    wavetable.setWave(0, wave2);
    wavetable.setWave(0, wave3);

    // Undo twice
    wavetable.undo();
    wavetable.undo();

    // Should be at wave1 state
    assertArrayEquals(wave1, wavetable.getWave(0));
  }

  @Test
  void testUndoLimitEnforced() {
    // Make more than limit changes (assume limit is 50)
    for (int i = 0; i < 60; i++) {
      int[] wave = new int[128];
      wave[0] = i;
      wavetable.setWave(0, wave);
    }

    // Undo 50 times should work
    for (int i = 0; i < 50; i++) {
      assertTrue(wavetable.canUndo());
      wavetable.undo();
    }

    // 51st undo should not be possible
    assertFalse(wavetable.canUndo());
  }

  @Test
  void testUndoWithKeyframeChanges() {
    // Add keyframe
    wavetable.addKeyframe(32);

    assertTrue(wavetable.canUndo());

    // Undo should remove keyframe
    wavetable.undo();

    assertFalse(wavetable.getKeyframeIndices().contains(32));
  }

  @Test
  void testUndoWithNormalize() {
    // Set wave data
    int[] wave = new int[128];
    for (int i = 0; i < 128; i++) {
      wave[i] = i * 50;
    }
    wavetable.setWave(0, wave);

    // Normalize (this is a tool operation)
    int[] originalWave = wavetable.getWave(0).clone();
    wavetable.normalize(0);

    // Undo should restore pre-normalize state
    wavetable.undo();

    assertArrayEquals(originalWave, wavetable.getWave(0));
  }
}
