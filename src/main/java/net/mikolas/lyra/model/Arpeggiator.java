package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Arpeggiator component wrapping parameters 311-358 of the 385-byte parameter array.
 *
 * <p>Controls arpeggiator mode, pattern, clock, length, octave, direction, velocity, timing, and
 * pattern steps (16 steps with glide/accent and timing/length).
 */
public class Arpeggiator {
  private final Sound parent;
  private final byte[] parameters;
  private static final int BASE_OFFSET = 311;
  private static final int PATTERN_STEPS_OFFSET = 327;

  // Lazy-initialized properties
  private IntegerProperty mode;
  private IntegerProperty pattern;
  private IntegerProperty clock;
  private IntegerProperty length;
  private IntegerProperty octave;
  private IntegerProperty direction;
  private IntegerProperty sortOrder;
  private IntegerProperty velocity;
  private IntegerProperty timingFactor;
  private IntegerProperty patternReset;
  private IntegerProperty patternLength;
  private IntegerProperty tempo;

  // Pattern steps (16 steps × 2 params each)
  private IntegerProperty[] patternStepGlideAccent = new IntegerProperty[16]; // 327-342
  private IntegerProperty[] patternStepTimingLength = new IntegerProperty[16]; // 343-358

  // Bitpacked helper properties for UI
  private final IntegerProperty[][] stepProperties = new IntegerProperty[16][5]; // [step][type, glide, accent, timing, length]

  public Arpeggiator(Sound parent, byte[] parameters) {
    this.parent = parent;
    this.parameters = parameters;
  }

  public IntegerProperty modeProperty() {
    if (mode == null) {
      int id = BASE_OFFSET;
      mode = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      mode.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return mode;
  }

  public IntegerProperty patternProperty() {
    if (pattern == null) {
      int id = BASE_OFFSET + 1;
      pattern = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      pattern.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return pattern;
  }

  public IntegerProperty clockProperty() {
    if (clock == null) {
      int id = BASE_OFFSET + 3;
      clock = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      clock.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return clock;
  }

  public IntegerProperty lengthProperty() {
    if (length == null) {
      int id = BASE_OFFSET + 4;
      length = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      length.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return length;
  }

  public IntegerProperty octaveProperty() {
    if (octave == null) {
      int id = BASE_OFFSET + 5;
      octave = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      octave.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return octave;
  }

  public IntegerProperty directionProperty() {
    if (direction == null) {
      int id = BASE_OFFSET + 6;
      direction = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      direction.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return direction;
  }

  public IntegerProperty sortOrderProperty() {
    if (sortOrder == null) {
      int id = BASE_OFFSET + 7;
      sortOrder = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      sortOrder.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return sortOrder;
  }

  public IntegerProperty velocityProperty() {
    if (velocity == null) {
      int id = BASE_OFFSET + 8;
      velocity = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      velocity.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return velocity;
  }

  public IntegerProperty timingFactorProperty() {
    if (timingFactor == null) {
      int id = BASE_OFFSET + 9;
      timingFactor = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      timingFactor.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return timingFactor;
  }

  public IntegerProperty patternResetProperty() {
    if (patternReset == null) {
      int id = BASE_OFFSET + 11;
      patternReset = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      patternReset.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return patternReset;
  }

  public IntegerProperty patternLengthProperty() {
    if (patternLength == null) {
      int id = BASE_OFFSET + 12;
      patternLength = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      patternLength.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return patternLength;
  }

  public IntegerProperty tempoProperty() {
    if (tempo == null) {
      int id = BASE_OFFSET + 15;
      tempo = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      tempo.addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return tempo;
  }

  /**
   * Get pattern step glide/accent property for a specific step (0-15).
   *
   * @param step step index (0-15)
   * @return glide/accent property
   */
  public IntegerProperty patternStepGlideAccentProperty(int step) {
    if (step < 0 || step >= 16) {
      throw new IllegalArgumentException("Step must be 0-15");
    }
    if (patternStepGlideAccent[step] == null) {
      int id = PATTERN_STEPS_OFFSET + step;
      patternStepGlideAccent[step] = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      patternStepGlideAccent[step].addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return patternStepGlideAccent[step];
  }

  /**
   * Get pattern step timing/length property for a specific step (0-15).
   *
   * @param step step index (0-15)
   * @return timing/length property
   */
  public IntegerProperty patternStepTimingLengthProperty(int step) {
    if (step < 0 || step >= 16) {
      throw new IllegalArgumentException("Step must be 0-15");
    }
    if (patternStepTimingLength[step] == null) {
      int id = PATTERN_STEPS_OFFSET + 16 + step;
      patternStepTimingLength[step] = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      patternStepTimingLength[step].addListener(
          (_, _, newVal) -> {
              int val = newVal.intValue();
              parameters[parent.getMemoryIndex(id)] = (byte) val;
              parent.notifyParameterChanged(id, val);
          });
    }
    return patternStepTimingLength[step];
  }

  /**
   * Get a specific sub-parameter of an Arp step.
   * 0: Step Type (3 bits: 4-6 of 327-342)
   * 1: Glide (1 bit: 3 of 327-342)
   * 2: Accent (3 bits: 0-2 of 327-342)
   * 3: Timing (3 bits: 0-2 of 343-358)
   * 4: Length (3 bits: 4-6 of 343-358)
   */
  public IntegerProperty getStepProperty(int step, int subParam) {
    if (stepProperties[step][subParam] == null) {
      IntegerProperty rawProp = (subParam < 3) ? patternStepGlideAccentProperty(step) : patternStepTimingLengthProperty(step);
      int initialValue = switch (subParam) {
        case 0 -> (rawProp.get() >> 4) & 0x07; // Type (bits 4-6)
        case 1 -> (rawProp.get() >> 3) & 0x01; // Glide (bit 3)
        case 2 -> rawProp.get() & 0x07;        // Accent (bits 0-2)
        case 3 -> rawProp.get() & 0x07;        // Timing (bits 0-2)
        case 4 -> (rawProp.get() >> 4) & 0x07; // Length (bits 4-6)
        default -> 0;
      };
      
      IntegerProperty subProp = new SimpleIntegerProperty(initialValue);
      stepProperties[step][subParam] = subProp;
      
      // Sync from sub to raw
      subProp.addListener((obs, old, val) -> {
        int currentRaw = rawProp.get();
        int newVal = val.intValue();
        int updatedRaw = switch (subParam) {
          case 0 -> (currentRaw & ~0x70) | ((newVal & 0x07) << 4);
          case 1 -> (currentRaw & ~0x08) | ((newVal & 0x01) << 3);
          case 2 -> (currentRaw & ~0x07) | (newVal & 0x07);
          case 3 -> (currentRaw & ~0x07) | (newVal & 0x07);
          case 4 -> (currentRaw & ~0x70) | ((newVal & 0x07) << 4);
          default -> currentRaw;
        };
        if (updatedRaw != currentRaw) {
          rawProp.set(updatedRaw);
        }
      });
      
      // Sync from raw to sub
      rawProp.addListener((obs, old, val) -> {
        int rawVal = val.intValue();
        int updatedSub = switch (subParam) {
          case 0 -> (rawVal >> 4) & 0x07;
          case 1 -> (rawVal >> 3) & 0x01;
          case 2 -> rawVal & 0x07;
          case 3 -> rawVal & 0x07;
          case 4 -> (rawVal >> 4) & 0x07;
          default -> subProp.get();
        };
        if (updatedSub != subProp.get()) {
          subProp.set(updatedSub);
        }
      });
    }
    return stepProperties[step][subParam];
  }

  public void updateFromParameter(int paramId, int value) {
    parameters[paramId] = (byte) value;
    
    // Handle pattern steps (327-358)
    if (paramId >= 327 && paramId <= 342) {
      int step = paramId - 327;
      if (patternStepGlideAccent[step] != null) {
        patternStepGlideAccent[step].set(value);
      }
      return;
    }
    if (paramId >= 343 && paramId <= 358) {
      int step = paramId - 343;
      if (patternStepTimingLength[step] != null) {
        patternStepTimingLength[step].set(value);
      }
      return;
    }
    
    // Handle main arpeggiator parameters
    int localParam = paramId - BASE_OFFSET;
    switch (localParam) {
      case 0 -> {
        if (mode != null) mode.set(value);
      }
      case 1 -> {
        if (pattern != null) pattern.set(value);
      }
      case 3 -> {
        if (clock != null) clock.set(value);
      }
      case 4 -> {
        if (length != null) length.set(value);
      }
      case 5 -> {
        if (octave != null) octave.set(value);
      }
      case 6 -> {
        if (direction != null) direction.set(value);
      }
      case 7 -> {
        if (sortOrder != null) sortOrder.set(value);
      }
      case 8 -> {
        if (velocity != null) velocity.set(value);
      }
      case 9 -> {
        if (timingFactor != null) timingFactor.set(value);
      }
      case 11 -> {
        if (patternReset != null) patternReset.set(value);
      }
      case 12 -> {
        if (patternLength != null) patternLength.set(value);
      }
      case 15 -> {
        if (tempo != null) tempo.set(value);
      }
    }
  }
}