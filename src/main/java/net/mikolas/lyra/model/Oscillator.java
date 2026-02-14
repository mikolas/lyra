package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Oscillator component wrapping a slice of the 385-byte parameter array.
 *
 * <p>Each oscillator has parameters for octave, semitone, detune, FM, shape, pulsewidth, PWM, etc.
 * Properties are lazily initialized and bidirectionally synced with the byte array.
 *
 * <p>Parameter offsets (v0.3.0 baseline):
 *
 * <ul>
 *   <li>OSC1: 1-16
 *   <li>OSC2: 17-32
 *   <li>OSC3: 33-48
 * </ul>
 */
public class Oscillator {
  private final Sound parent;
  private final byte[] parameters;
  private final int oscNumber;
  private final int baseOffset;

  // Lazy-initialized properties
  private IntegerProperty octave;
  private IntegerProperty semitone;
  private IntegerProperty detune;
  private IntegerProperty bendRange;
  private IntegerProperty keytrack;
  private IntegerProperty fmSource;
  private IntegerProperty fmAmount;
  private IntegerProperty shape;
  private IntegerProperty pulsewidth;
  private IntegerProperty pwmSource;
  private IntegerProperty pwmAmount;
  private IntegerProperty limitWt;
  private IntegerProperty brilliance;
  private IntegerProperty pitchModSource;
  private IntegerProperty pitchModAmount;
  private IntegerProperty sync;

  /**
   * Creates an oscillator wrapping a parameter array slice.
   *
   * @param parent parent sound object
   * @param parameters 385-byte parameter array
   * @param oscNumber oscillator number (1, 2, or 3)
   * @param baseOffset starting parameter offset (1, 17, or 33)
   */
  public Oscillator(Sound parent, byte[] parameters, int oscNumber, int baseOffset) {
    this.parent = parent;
    this.parameters = parameters;
    this.oscNumber = oscNumber;
    this.baseOffset = baseOffset;
  }

  public IntegerProperty octaveProperty() {
    if (octave == null) {
      octave = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(baseOffset)] & 0xFF);
      octave.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(baseOffset)] = (byte) val;
          parent.notifyParameterChanged(baseOffset, val);
      });
    }
    return octave;
  }

  public IntegerProperty semitoneProperty() {
    if (semitone == null) {
      int id = baseOffset + 1;
      semitone = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      semitone.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return semitone;
  }

  public IntegerProperty detuneProperty() {
    if (detune == null) {
      int id = baseOffset + 2;
      detune = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      detune.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return detune;
  }

  public IntegerProperty bendRangeProperty() {
    if (bendRange == null) {
      int id = baseOffset + 3;
      bendRange = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      bendRange.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return bendRange;
  }

  public IntegerProperty keytrackProperty() {
    if (keytrack == null) {
      int id = baseOffset + 4;
      keytrack = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      keytrack.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return keytrack;
  }

  public IntegerProperty fmSourceProperty() {
    if (fmSource == null) {
      int id = baseOffset + 5;
      fmSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      fmSource.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return fmSource;
  }

  public IntegerProperty fmAmountProperty() {
    if (fmAmount == null) {
      int id = baseOffset + 6;
      fmAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      fmAmount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return fmAmount;
  }

  public IntegerProperty shapeProperty() {
    if (shape == null) {
      int id = baseOffset + 7;
      shape = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      shape.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return shape;
  }

  public IntegerProperty pulsewidthProperty() {
    if (pulsewidth == null) {
      int id = baseOffset + 8;
      pulsewidth = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pulsewidth.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pulsewidth;
  }

  public IntegerProperty pwmSourceProperty() {
    if (pwmSource == null) {
      int id = baseOffset + 9;
      pwmSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pwmSource.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pwmSource;
  }

  public IntegerProperty pwmAmountProperty() {
    if (pwmAmount == null) {
      int id = baseOffset + 10;
      pwmAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pwmAmount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pwmAmount;
  }

  public IntegerProperty limitWtProperty() {
    if (limitWt == null) {
      int id = baseOffset + 11;
      limitWt = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      limitWt.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return limitWt;
  }

  public IntegerProperty brillianceProperty() {
    if (brilliance == null) {
      int id = baseOffset + 12;
      brilliance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      brilliance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return brilliance;
  }

  public IntegerProperty pitchModSourceProperty() {
    if (pitchModSource == null) {
      int id = baseOffset + 13;
      pitchModSource = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pitchModSource.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pitchModSource;
  }

  public IntegerProperty pitchModAmountProperty() {
    if (pitchModAmount == null) {
      int id = baseOffset + 14;
      pitchModAmount = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pitchModAmount.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return pitchModAmount;
  }

  public IntegerProperty syncProperty() {
    if (sync == null) {
      int id = baseOffset + 15;
      sync = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sync.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return sync;
  }

  public int getShape() { return shapeProperty().get(); }
  public void setShape(int value) { shapeProperty().set(value); }

  /**
   * Updates property when parameter changes externally (e.g., from MIDI).
   *
   * @param paramId absolute parameter ID (1-384)
   * @param value new value (0-127)
   */
  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - baseOffset;
    switch (localParam) {
      case 0 -> { if (octave != null) octave.set(value); }
      case 1 -> { if (semitone != null) semitone.set(value); }
      case 2 -> { if (detune != null) detune.set(value); }
      case 3 -> { if (bendRange != null) bendRange.set(value); }
      case 4 -> { if (keytrack != null) keytrack.set(value); }
      case 5 -> { if (fmSource != null) fmSource.set(value); }
      case 6 -> { if (fmAmount != null) fmAmount.set(value); }
      case 7 -> { if (shape != null) shape.set(value); }
      case 8 -> { if (pulsewidth != null) pulsewidth.set(value); }
      case 9 -> { if (pwmSource != null) pwmSource.set(value); }
      case 10 -> { if (pwmAmount != null) pwmAmount.set(value); }
      case 11 -> { if (limitWt != null) limitWt.set(value); }
      case 12 -> { if (brilliance != null) brilliance.set(value); }
      case 13 -> { if (pitchModSource != null) pitchModSource.set(value); }
      case 14 -> { if (pitchModAmount != null) pitchModAmount.set(value); }
      case 15 -> { if (sync != null) sync.set(value); }
    }
  }

  public int getOscNumber() {
    return oscNumber;
  }
}