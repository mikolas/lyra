package net.mikolas.lyra.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sound model representing a Blofeld sound with 385 parameters.
 *
 * <p>Uses Lombok for boilerplate elimination and ORMLite for database persistence. JavaFX
 * properties are lazily initialized for UI binding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "sounds")
public class Sound {
  @DatabaseField(generatedId = true)
  private Integer id;

  @DatabaseField(canBeNull = false)
  private String name;

  @DatabaseField
  private Integer category;

  @DatabaseField(uniqueCombo = true)
  private Integer bank;

  @DatabaseField(uniqueCombo = true)
  private Integer program;

  @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = false)
  private byte[] parameters;

  public byte[] getParameters() {
    return parameters;
  }

  public void setParameters(byte[] parameters) {
    this.parameters = parameters;
  }

  // Many-to-many relationships via join tables
  @ForeignCollectionField(eager = false)
  private ForeignCollection<SoundCollection> soundCollections;

  @ForeignCollectionField(eager = false)
  private ForeignCollection<SoundTag> soundTags;

  // Transient domain components (not persisted, wrap parameters array)
  private transient Oscillator[] oscillators;
  private transient Filter[] filters;
  private transient Envelope[] envelopes;
  private transient LFO[] lfos;
  private transient Mixer mixer;
  private transient Arpeggiator arpeggiator;
  private transient ModulationSlot[] modulationSlots;
  private transient Modifier[] modifiers;
  private transient Effect[] effects;
  private transient CommonParameters common;

  // Transient JavaFX properties for UI binding (not persisted)
  private transient StringProperty nameProperty;
  private transient IntegerProperty categoryProperty;
  private transient IntegerProperty bankProperty;
  private transient IntegerProperty programProperty;

  private transient ParameterChangeListener parameterChangeListener;
  @Builder.Default
  private transient boolean silentUpdate = false;

  public void setParameterChangeListener(ParameterChangeListener listener) {
    this.parameterChangeListener = listener;
  }

  public void notifyParameterChanged(int paramId, int value) {
    if (!silentUpdate && parameterChangeListener != null) {
      parameterChangeListener.onParameterChanged(paramId, value);
    }
  }

  /**
   * Update a parameter without triggering the MIDI sync listener.
   * Used for incoming MIDI messages to prevent feedback loops.
   */
  public void updateParameterSilently(int paramId, int value) {
    this.silentUpdate = true;
    try {
      updateParameter(paramId, value);
    } finally {
      this.silentUpdate = false;
    }
  }

  /**
   * Get oscillators array (OSC1, OSC2, OSC3).
   *
   * @return array of 3 oscillators
   */
  public Oscillator[] getOscillators() {
    if (oscillators == null) {
      oscillators =
          new Oscillator[] {
            new Oscillator(this, parameters, 1, 1), // OSC1: IDs 1-16
            new Oscillator(this, parameters, 2, 17), // OSC2: IDs 17-32
            new Oscillator(this, parameters, 3, 33) // OSC3: IDs 33-48
          };
    }
    return oscillators;
  }

  /**
   * Get filters array (Filter 1, Filter 2).
   *
   * @return array of 2 filters
   */
  public Filter[] getFilters() {
    if (filters == null) {
      filters =
          new Filter[] {
            new Filter(this, parameters, 1, 77), // Filter 1: IDs 77-96
            new Filter(this, parameters, 2, 97) // Filter 2: IDs 97-116
          };
    }
    return filters;
  }

  /**
   * Get envelopes array (Filter, Amp, Env3, Env4).
   *
   * @return array of 4 envelopes
   */
  public Envelope[] getEnvelopes() {
    if (envelopes == null) {
      envelopes =
          new Envelope[] {
            new Envelope(this, parameters, 1, 196), // Filter Env: IDs 196-207
            new Envelope(this, parameters, 2, 208), // Amp Env: IDs 208-219 (Actually 222 in spec?)
            new Envelope(this, parameters, 3, 220), // Env 3: IDs 220-231
            new Envelope(this, parameters, 4, 232) // Env 4: IDs 232-243
          };
    }
    return envelopes;
  }

  /**
   * Get LFOs array (LFO 1, LFO 2, LFO 3).
   *
   * @return array of 3 LFOs
   */
  public LFO[] getLFOs() {
    if (lfos == null) {
      lfos =
          new LFO[] {
            new LFO(this, parameters, 1, 160), // LFO 1: params 160-171
            new LFO(this, parameters, 2, 172), // LFO 2: params 172-183
            new LFO(this, parameters, 3, 184) // LFO 3: params 184-195
          };
    }
    return lfos;
  }

  /**
   * Get mixer component.
   *
   * @return mixer object
   */
  public Mixer getMixer() {
    if (mixer == null) {
      mixer = new Mixer(this, parameters); // Mixer: params 61-72
    }
    return mixer;
  }

  /**
   * Get arpeggiator component.
   *
   * @return arpeggiator object
   */
  public Arpeggiator getArpeggiator() {
    if (arpeggiator == null) {
      arpeggiator = new Arpeggiator(this, parameters); // Arp: params 311-358
    }
    return arpeggiator;
  }

  /**
   * Get modulation matrix slots array.
   *
   * @return array of 16 modulation slots
   */
  public ModulationSlot[] getModulationSlots() {
    if (modulationSlots == null) {
      modulationSlots = new ModulationSlot[16];
      for (int i = 0; i < 16; i++) {
        modulationSlots[i] = new ModulationSlot(this, parameters, i + 1, 261 + (i * 3));
      }
    }
    return modulationSlots;
  }

  /**
   * Get modifiers array.
   *
   * @return array of 4 modifiers
   */
  public Modifier[] getModifiers() {
    if (modifiers == null) {
      modifiers = new Modifier[4];
      for (int i = 0; i < 4; i++) {
        modifiers[i] = new Modifier(this, parameters, i); // Modifiers: params 247-260
      }
    }
    return modifiers;
  }

  /**
   * Get effects array (FX 1, FX 2).
   *
   * @return array of 2 effects
   */
  public Effect[] getEffects() {
    if (effects == null) {
      effects =
          new Effect[] {
            new Effect(this, parameters, 1, 128), // FX 1: params 128-143
            new Effect(this, parameters, 2, 144) // FX 2: params 144-159
          };
    }
    return effects;
  }

  /**
   * Get common parameters component.
   *
   * @return common parameters object
   */
  public CommonParameters getCommon() {
    if (common == null) {
      common = new CommonParameters(this, parameters); // Common parameters
    }
    return common;
  }

  /**
   * Get the array index for a given parameter ID.
   *
   * <p>Decouples the MIDI parameter ID (from specification) from the internal array memory
   * index (which includes reserved bytes and non-linear shifts).
   *
   * @param id MIDI parameter ID (1-384)
   * @return internal array index (0-384)
   */
  public int getMemoryIndex(int id) {
    // Parameter IDs map directly to array indices
    return id;
  }

  /**
   * Replace all parameters with new data and notify all components.
   *
   * @param newParams 385 bytes of sound data
   */
  public void replaceParameters(byte[] newParams) {
    if (newParams == null || newParams.length != 385) return;
    System.arraycopy(newParams, 0, this.parameters, 0, 385);
    
    // Refresh all sub-components and properties
    updateNameFromParameters();
    updateCategoryFromParameter();
    
    // Notify sub-controllers silently by triggering all parameter paths
    this.silentUpdate = true;
    try {
      for (int id = 1; id <= 384; id++) {
          updateParameter(id, parameters[getMemoryIndex(id)] & 0xFF);
      }
    } finally {
      this.silentUpdate = false;
    }
  }

  /**
   * Update a specific parameter in the internal array and notify components.
   *
   * @param paramId parameter ID (1-384)
   * @param value parameter value
   */
  public void updateParameter(int paramId, int value) {
    if (paramId < 1 || paramId > 384) return;

    parameters[getMemoryIndex(paramId)] = (byte) value;
    notifyParameterChanged(paramId, value);

    // Route updates to sub-components
    if (paramId >= 1 && paramId <= 16) {
      getOscillators()[0].updateFromParameter(paramId, value);
    } else if (paramId >= 17 && paramId <= 32) {
      getOscillators()[1].updateFromParameter(paramId, value);
    } else if (paramId >= 33 && paramId <= 48) {
      getOscillators()[2].updateFromParameter(paramId, value);
    } else if (paramId == 49 || paramId == 50 || paramId == 51 || paramId == 53 || paramId == 56
            || paramId == 57 || paramId == 58 || paramId == 59 || paramId == 117
            || (paramId >= 121 && paramId <= 124)) {
      getCommon().updateFromParameter(paramId, value);
    } else if (paramId >= 61 && paramId <= 72) {
      getMixer().updateFromParameter(paramId, value);
    } else if (paramId >= 77 && paramId <= 96) {
      getFilters()[0].updateFromParameter(paramId, value);
    } else if (paramId >= 97 && paramId <= 116) {
      getFilters()[1].updateFromParameter(paramId, value);
    } else if (paramId >= 128 && paramId <= 143) {
      getEffects()[0].updateFromParameter(paramId, value);
    } else if (paramId >= 144 && paramId <= 159) {
      getEffects()[1].updateFromParameter(paramId, value);
    } else if (paramId >= 160 && paramId <= 171) {
      getLFOs()[0].updateFromParameter(paramId, value);
    } else if (paramId >= 172 && paramId <= 183) {
      getLFOs()[1].updateFromParameter(paramId, value);
    } else if (paramId >= 184 && paramId <= 195) {
      getLFOs()[2].updateFromParameter(paramId, value);
    } else if (paramId >= 196 && paramId <= 207) {
      getEnvelopes()[0].updateFromParameter(paramId, value);
    } else if (paramId >= 208 && paramId <= 219) {
      getEnvelopes()[1].updateFromParameter(paramId, value);
    } else if (paramId >= 220 && paramId <= 231) {
      getEnvelopes()[2].updateFromParameter(paramId, value);
    } else if (paramId >= 232 && paramId <= 243) {
      getEnvelopes()[3].updateFromParameter(paramId, value);
    } else if (paramId >= 247 && paramId <= 260) {
      int modifierIndex = (paramId - 247) / 4;
      getModifiers()[modifierIndex].updateFromParameter(paramId, value);
    } else if (paramId >= 261 && paramId <= 308) {
      int slotIndex = (paramId - 261) / 3;
      getModulationSlots()[slotIndex].updateFromParameter(paramId, value);
    } else if ((paramId >= 311 && paramId <= 326) || (paramId >= 327 && paramId <= 358)) {
      getArpeggiator().updateFromParameter(paramId, value);
    } else if (paramId >= 363 && paramId <= 378) {
      updateNameFromParameters();
    } else if (paramId == 379) {
      updateCategoryFromParameter();
    }
  }

  /**
   * Sets the sound name and synchronizes it with the parameter array (IDs 363-378).
   *
   * @param newName New name (max 16 characters)
   */
  public void setNameAndSyncParameters(String newName) {
    this.name = newName;
    if (nameProperty != null) {
      nameProperty.set(newName);
    }

    // Update parameter array (IDs 363-378)
    for (int i = 0; i < 16; i++) {
      int id = 363 + i;
      int val;
      if (i < newName.length()) {
        val = (int) newName.charAt(i);
      } else {
        val = 32; // Space
      }
      parameters[getMemoryIndex(id)] = (byte) val;
      notifyParameterChanged(id, val);
    }
  }

  /**
   * Create a shallow copy of this sound (same parameters, new ID).
   *
   * @return cloned Sound
   */
  public Sound cloneSound() {
    byte[] newParams = new byte[385];
    System.arraycopy(parameters, 0, newParams, 0, 385);
    return Sound.builder()
        .name(name)
        .category(category)
        .bank(bank)
        .program(program)
        .parameters(newParams)
        .build();
  }

  /**
   * Update name field from parameters IDs 363-378 (16 ASCII characters).
   */
  private void updateNameFromParameters() {
    StringBuilder sb = new StringBuilder(16);
    for (int i = 0; i < 16; i++) {
      int id = 363 + i;
      char c = (char) (parameters[getMemoryIndex(id)] & 0xFF);
      if (c >= 32 && c < 127) { // Printable ASCII
        sb.append(c);
      }
    }
    name = sb.toString().trim();
    if (nameProperty != null) {
      nameProperty.set(name);
    }
  }

  /**
   * Update category field from parameter ID 379.
   */
  private void updateCategoryFromParameter() {
    int catIndex = parameters[getMemoryIndex(379)] & 0xFF;
    if (catIndex >= 0 && catIndex <= 12) {
      category = catIndex;
      if (categoryProperty != null) {
        categoryProperty.set(catIndex);
      }
    }
  }

  /**
   * Get or create the name property for JavaFX binding.
   *
   * @return StringProperty bound to name field
   */
  public StringProperty nameProperty() {
    if (nameProperty == null) {
      nameProperty = new SimpleStringProperty(name);
      nameProperty.addListener((_, _, newVal) -> name = newVal);
    }
    return nameProperty;
  }

  /**
   * Get or create the category property for JavaFX binding.
   *
   * @return IntegerProperty bound to category field
   */
  public IntegerProperty categoryProperty() {
    if (categoryProperty == null) {
      categoryProperty = new SimpleIntegerProperty(category == null ? 0 : category);
      categoryProperty.addListener((_, _, newVal) -> {
          int val = newVal.intValue();
          parameters[getMemoryIndex(379)] = (byte) val;
          this.category = val;
          notifyParameterChanged(379, val);
      });
    }
    return categoryProperty;
  }

  /**
   * Get or create the bank property for JavaFX binding.
   *
   * @return IntegerProperty bound to bank field
   */
  public IntegerProperty bankProperty() {
    if (bankProperty == null) {
      bankProperty = new SimpleIntegerProperty(bank == null ? 0 : bank);
      bankProperty.addListener((_, _, newVal) -> bank = newVal.intValue());
    }
    return bankProperty;
  }

  /**
   * Get or create the program property for JavaFX binding.
   *
   * @return IntegerProperty bound to program field
   */
  public IntegerProperty programProperty() {
    if (programProperty == null) {
      programProperty = new SimpleIntegerProperty(program == null ? 0 : program);
      programProperty.addListener((_, _, newVal) -> program = newVal.intValue());
    }
    return programProperty;
  }

  /**
   * Create an initialized sound with default values.
   *
   * @return Sound with default parameters
   */
  public static Sound init() {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = 64;
    }
    return Sound.builder().name("Init").category(0).parameters(params).build();
  }
}
