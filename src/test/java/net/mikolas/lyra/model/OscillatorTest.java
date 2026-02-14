package net.mikolas.lyra.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** TDD RED: Tests for Oscillator component - write tests first. */
class OscillatorTest {
  private Sound sound;
  private byte[] parameters;
  private Oscillator osc1;

  @BeforeEach
  void setUp() {
    parameters = new byte[385];
    sound = Sound.builder().parameters(parameters).build();
    // OSC1 starts at offset 1
    osc1 = new Oscillator(sound, parameters, 1, 1);
  }

  @Test
  void shouldReadOctaveFromParameters() {
    parameters[sound.getMemoryIndex(1)] = 64; // Octave default
    assertEquals(64, osc1.octaveProperty().get());
  }

  @Test
  void shouldWriteOctaveToParameters() {
    osc1.octaveProperty().set(80);
    assertEquals(80, parameters[sound.getMemoryIndex(1)] & 0xFF);
  }

  @Test
  void shouldReadSemitoneFromParameters() {
    parameters[sound.getMemoryIndex(2)] = 64; // Semitone default
    assertEquals(64, osc1.semitoneProperty().get());
  }

  @Test
  void shouldWriteSemitoneToParameters() {
    osc1.semitoneProperty().set(70);
    assertEquals(70, parameters[sound.getMemoryIndex(2)] & 0xFF);
  }

  @Test
  void shouldUpdatePropertyWhenParameterChanges() {
    osc1.octaveProperty(); // Initialize property
    osc1.updateFromParameter(1, 100);
    assertEquals(100, osc1.octaveProperty().get());
    assertEquals(100, parameters[sound.getMemoryIndex(1)] & 0xFF);
  }

  @Test
  void shouldHandleMultipleOscillators() {
    Oscillator osc2 = new Oscillator(sound, parameters, 2, 17);
    Oscillator osc3 = new Oscillator(sound, parameters, 3, 33);

    osc1.octaveProperty().set(64);
    osc2.octaveProperty().set(80);
    osc3.octaveProperty().set(96);

    assertEquals(64, parameters[sound.getMemoryIndex(1)] & 0xFF);
    assertEquals(80, parameters[sound.getMemoryIndex(17)] & 0xFF);
    assertEquals(96, parameters[sound.getMemoryIndex(33)] & 0xFF);
  }
}