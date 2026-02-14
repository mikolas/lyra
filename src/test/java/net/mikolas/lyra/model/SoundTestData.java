package net.mikolas.lyra.model;

/**
 * Test data for Sound model tests.
 *
 * <p>Contains the Blofeld "Init" sound dump (385 bytes) for testing parameter conversion and
 * round-trip serialization.
 */
public class SoundTestData {

  /**
   * Blofeld "Init" sound - factory initialization patch.
   *
   * <p>385-byte parameter array representing the default sound state.
   * Parameter IDs map directly to array indices (ID 0 at index 0, ID 1 at index 1, etc.)
   */
  public static final byte[] INIT_SOUND = {
    1, 64, 64, 64, 66, 96, 0, 0, 2, 127, 1, 64, 0, 0, 0, 0, 0, 64, 64, 64, 66, 96, 0, 0, 0, 127, 3,
    64, 0, 0, 0, 0, 0, 52, 64, 64, 66, 96, 0, 0, 0, 127, 5, 64, 0, 0, 0, 0, 0, 0, 2, 64, 0, 0, 0,
    0, 0, 20, 0, 0, 0, 127, 0, 127, 0, 127, 0, 0, 0, 64, 0, 0, 0, 0, 1, 0, 0, 1, 127, 64, 0, 0, 0,
    0, 0, 0, 64, 64, 64, 1, 64, 0, 0, 64, 1, 64, 0, 0, 127, 64, 0, 0, 0, 0, 0, 0, 64, 64, 64, 0,
    64, 0, 0, 64, 3, 64, 0, 0, 3, 0, 0, 127, 114, 5, 64, 0, 0, 0, 1, 0, 20, 64, 64, 0, 127, 127,
    127, 127, 127, 127, 127, 127, 127, 127, 8, 0, 53, 64, 100, 0, 64, 100, 0, 100, 110, 0, 15, 64,
    127, 127, 0, 50, 64, 0, 0, 0, 0, 64, 0, 0, 64, 0, 0, 40, 64, 0, 0, 0, 0, 64, 0, 0, 64, 0, 0,
    30, 64, 0, 0, 0, 0, 64, 0, 0, 64, 1, 0, 64, 0, 0, 127, 50, 0, 0, 127, 0, 0, 0, 0, 64, 0, 0,
    127, 52, 127, 0, 127, 0, 0, 0, 0, 64, 0, 0, 64, 64, 64, 64, 64, 64, 0, 0, 0, 64, 0, 0, 64, 64,
    64, 64, 64, 64, 0, 0, 1, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, 0, 64, 1, 1, 64, 0, 0,
    64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64,
    0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 16, 100, 0, 0, 15, 8, 5, 0, 0, 0, 1, 12, 0,
    0, 15, 0, 0, 55, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 68, 68, 68, 68, 68, 68, 68,
    68, 68, 68, 68, 68, 68, 68, 68, 68, 68, 0, 0, 0, 73, 110, 105, 116, 32, 32, 32, 32, 32, 32,
    32, 32, 32, 32, 32, 32, 0, 0, 0, 0, 0, 0
  };

  /**
   * Create a Sound instance with Init sound data.
   *
   * @return Sound with Init parameters
   */
  public static Sound createInitSound() {
    byte[] params = new byte[385];
    System.arraycopy(INIT_SOUND, 0, params, 0, 385);
    return Sound.builder().name("Init").category(0).bank(0).program(0).parameters(params).build();
  }
}
