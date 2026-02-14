package net.mikolas.lyra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Mixer component wrapping parameters 61-72 of the 385-byte parameter array.
 */
public class Mixer {
  private final Sound parent;
  private final byte[] parameters;
  private static final int BASE_OFFSET = 61;

  // Lazy-initialized properties
  private IntegerProperty osc1Level;
  private IntegerProperty osc1Balance;
  private IntegerProperty osc2Level;
  private IntegerProperty osc2Balance;
  private IntegerProperty osc3Level;
  private IntegerProperty osc3Balance;
  private IntegerProperty noiseLevel;
  private IntegerProperty noiseBalance;
  private IntegerProperty noiseColor;
  private IntegerProperty ringModLevel;
  private IntegerProperty ringModBalance;

  public Mixer(Sound parent, byte[] parameters) {
    this.parent = parent;
    this.parameters = parameters;
  }

  public IntegerProperty osc1LevelProperty() {
    if (osc1Level == null) {
      int id = BASE_OFFSET;
      osc1Level = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc1Level.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc1Level;
  }

  public IntegerProperty osc1BalanceProperty() {
    if (osc1Balance == null) {
      int id = BASE_OFFSET + 1;
      osc1Balance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc1Balance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc1Balance;
  }

  public IntegerProperty osc2LevelProperty() {
    if (osc2Level == null) {
      int id = BASE_OFFSET + 2;
      osc2Level = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc2Level.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc2Level;
  }

  public IntegerProperty osc2BalanceProperty() {
    if (osc2Balance == null) {
      int id = BASE_OFFSET + 3;
      osc2Balance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc2Balance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc2Balance;
  }

  public IntegerProperty osc3LevelProperty() {
    if (osc3Level == null) {
      int id = BASE_OFFSET + 4;
      osc3Level = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc3Level.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc3Level;
  }

  public IntegerProperty osc3BalanceProperty() {
    if (osc3Balance == null) {
      int id = BASE_OFFSET + 5;
      osc3Balance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      osc3Balance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return osc3Balance;
  }

  public IntegerProperty noiseLevelProperty() {
    if (noiseLevel == null) {
      int id = BASE_OFFSET + 6;
      noiseLevel = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      noiseLevel.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return noiseLevel;
  }

  public IntegerProperty noiseBalanceProperty() {
    if (noiseBalance == null) {
      int id = BASE_OFFSET + 7;
      noiseBalance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      noiseBalance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return noiseBalance;
  }

  public IntegerProperty noiseColorProperty() {
    if (noiseColor == null) {
      int id = BASE_OFFSET + 8;
      noiseColor = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      noiseColor.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return noiseColor;
  }

  public IntegerProperty ringModLevelProperty() {
    if (ringModLevel == null) {
      int id = BASE_OFFSET + 10;
      ringModLevel = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ringModLevel.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ringModLevel;
  }

  public IntegerProperty ringModBalanceProperty() {
    if (ringModBalance == null) {
      int id = BASE_OFFSET + 11;
      ringModBalance = new SimpleIntegerProperty(parameters[parent.getMemoryIndex(id)] & 0xFF);
      ringModBalance.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[parent.getMemoryIndex(id)] = (byte) val;
          parent.notifyParameterChanged(id, val);
      });
    }
    return ringModBalance;
  }

  public void updateFromParameter(int paramId, int value) {
    int localParam = paramId - BASE_OFFSET;
    switch (localParam) {
      case 0 -> { if (osc1Level != null) osc1Level.set(value); }
      case 1 -> { if (osc1Balance != null) osc1Balance.set(value); }
      case 2 -> { if (osc2Level != null) osc2Level.set(value); }
      case 3 -> { if (osc2Balance != null) osc2Balance.set(value); }
      case 4 -> { if (osc3Level != null) osc3Level.set(value); }
      case 5 -> { if (osc3Balance != null) osc3Balance.set(value); }
      case 6 -> { if (noiseLevel != null) noiseLevel.set(value); }
      case 7 -> { if (noiseBalance != null) noiseBalance.set(value); }
      case 8 -> { if (noiseColor != null) noiseColor.set(value); }
      case 10 -> { if (ringModLevel != null) ringModLevel.set(value); }
      case 11 -> { if (ringModBalance != null) ringModBalance.set(value); }
    }
  }
}