package net.mikolas.lyra.midi;

import net.mikolas.lyra.model.MultiPatch;
import net.mikolas.lyra.model.Sound;

/**
 * Parser for Blofeld SysEx messages.
 *
 * <p>Handles parsing of SNDD (Sound Dump Data) and MULD (Multi Dump Data) messages.
 *
 * <p>SNDD Format (392 bytes): F0 3E 13 <dev> 10 <bank> <prog> <383 params> 7F F7
 * <p>MULD Format (425 bytes): F0 3E 13 <dev> 11 <418 payload> CHK F7
 */
public class SysExParser {

  private static final byte SYSEX_START = (byte) 0xF0;
  private static final byte SYSEX_END = (byte) 0xF7;
  private static final byte WALDORF_ID = 0x3E;
  private static final byte BLOFELD_ID = 0x13;
  private static final byte SNDD_COMMAND = 0x10;
  private static final byte MULD_COMMAND = 0x11;

  /**
   * Parse a Sound Dump Data (SNDD) message.
   *
   * @param sysex SysEx message bytes (392 or 394 bytes)
   * @return Sound object, or null if invalid
   */
  public static Sound parseSoundDump(byte[] sysex) {
    if (!isValidSoundDump(sysex)) {
      return null;
    }

    // [0]: F0
    // [1-2]: 0x3E, 0x13
    // [3]: Device ID
    // [4]: 0x10 (SNDD command)
    // [5]: Bank
    // [6]: Program
    // [7...]: parameters
    // [len-2]: Checksum
    // [len-1]: 0xF7 (END)

    int bank = sysex[5] & 0xFF;
    int program = sysex[6] & 0xFF;

    byte[] parameters = new byte[385];
    if (sysex.length == 392) {
        // Hardware standard: 383 parameters (IDs 0-382)
        // Copy 383 bytes from sysex[7-389] to parameters[0-382]
        System.arraycopy(sysex, 7, parameters, 0, 383);
    } else {
        // Padded format: 385 parameters (indices 0-384)
        // Copy 385 bytes from sysex[7-391] to parameters[0-384]
        System.arraycopy(sysex, 7, parameters, 0, 385);
    }

    String name = extractName(parameters);
    Integer category = extractCategory(parameters);

    return Sound.builder()
        .name(name)
        .category(category)
        .bank(bank)
        .program(program)
        .parameters(parameters)
        .build();
  }

  /**
   * Parse a Multi Dump Data (MULD) message.
   *
   * <p>MULD Format (425 bytes): F0 3E 13 <dev> 11 <418 payload> CHK F7
   *
   * @param sysex SysEx message bytes (425 bytes)
   * @return MultiPatch object, or null if invalid
   */
  public static MultiPatch parseMultiDump(byte[] sysex) {
    if (!isValidMultiDump(sysex)) {
      System.err.printf("[PARSER] MULD Validation Failed. Len: %d | Header: %02X %02X %02X %02X %02X | End: %02X%n",
          sysex != null ? sysex.length : 0,
          sysex != null && sysex.length > 0 ? sysex[0] : 0,
          sysex != null && sysex.length > 1 ? sysex[1] : 0,
          sysex != null && sysex.length > 2 ? sysex[2] : 0,
          sysex != null && sysex.length > 3 ? sysex[3] : 0,
          sysex != null && sysex.length > 4 ? sysex[4] : 0,
          sysex != null && sysex.length > 0 ? sysex[sysex.length-1] : 0);
      return null;
    }

    // Extract payload (418 bytes) from bytes 5-422
    byte[] data = new byte[418];
    System.arraycopy(sysex, 5, data, 0, 418);

    // Payload already contains Bank (0) and Prog (1)
    MultiPatch multi = new MultiPatch();
    multi.updateFromMidi(data);
    
    return multi;
  }

  /**
   * Validate SNDD message structure.
   *
   * @param sysex SysEx message bytes
   * @return true if valid SNDD message
   */
  private static boolean isValidSoundDump(byte[] sysex) {
    if (sysex == null || (sysex.length != 394 && sysex.length != 392)) {
      return false;
    }

    return sysex[0] == SYSEX_START
        && sysex[1] == WALDORF_ID
        && sysex[2] == BLOFELD_ID
        && sysex[4] == SNDD_COMMAND
        && sysex[sysex.length - 1] == SYSEX_END;
  }

  /**
   * Validate MULD message structure.
   *
   * @param sysex SysEx message bytes
   * @return true if valid MULD message
   */
  private static boolean isValidMultiDump(byte[] sysex) {
    if (sysex == null || (sysex.length != 425 && sysex.length != 424)) {
      return false;
    }

    return sysex[0] == SYSEX_START
        && sysex[1] == WALDORF_ID
        && sysex[2] == BLOFELD_ID
        && sysex[4] == MULD_COMMAND
        && sysex[sysex.length - 1] == SYSEX_END;
  }

  /**
   * Extract sound name from parameters.
   *
   * @param parameters 385-byte parameter array
   * @return sound name (trimmed)
   */
  private static String extractName(byte[] parameters) {
    StringBuilder sb = new StringBuilder(16);
    // Sound Name is at parameter IDs 363-378
    for (int i = 363; i <= 378; i++) {
      char c = (char) (parameters[i] & 0xFF);
      if (c >= 32 && c < 127) { // Printable ASCII
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

  /**
   * Extract category from parameters.
   *
   * @param parameters 385-byte parameter array
   * @return category index (0-12)
   */
  private static Integer extractCategory(byte[] parameters) {
    // Category is at parameter ID 379
    int categoryIndex = parameters[379] & 0xFF;
    if (categoryIndex >= 0 && categoryIndex <= 12) {
      return categoryIndex;
    }
    return 0; // Default to Init (0) if invalid
  }
  
  /**
   * Get category name from category index.
   *
   * @param categoryIndex category index (0-12)
   * @return category name
   */
  public static String getCategoryName(Integer categoryIndex) {
    if (categoryIndex == null || categoryIndex < 0 || categoryIndex > 12) {
      return "Init";
    }
    String[] categories = {
      "Init", "Arp", "Atmo", "Bass", "Drum", "FX", "Keys", "Lead", "Mono", "Pad", "Perc",
      "Poly", "Seq"
    };
    return categories[categoryIndex];
  }
}