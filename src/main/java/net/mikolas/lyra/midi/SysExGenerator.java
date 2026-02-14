package net.mikolas.lyra.midi;

import net.mikolas.lyra.model.Sound;

/**
 * Generator for Blofeld SysEx messages.
 *
 * <p>Handles generation of SNDD (Sound Dump Data) messages from Sound objects.
 *
 * <p>SNDD Format (392 bytes): F0 3E 13 <dev> 10 <bank> <prog> <383 params> 7F F7
 *
 * <p>Note: Only parameters[2-384] are stored in MIDI files (383 bytes). Parameters[0-1] are
 * reserved and not transmitted. Checksum is always 0x7F (constant, not calculated).
 */
public class SysExGenerator {

  private static final byte SYSEX_START = (byte) 0xF0;
  private static final byte SYSEX_END = (byte) 0xF7;
  private static final byte WALDORF_ID = 0x3E;
  private static final byte BLOFELD_ID = 0x13;
  private static final byte SNDD_COMMAND = 0x10;
  private static final byte CHECKSUM = 0x7F; // Constant, not calculated

  /**
   * Generate a Sound Parameter Change (SNDP) message.
   * Format: F0 3E 13 <dev> 11 <location> <paramId_H> <paramId_L> <value> 7F F7
   *
   * @param deviceId Device ID (0-127, use 0x7F for broadcast)
   * @param location Location (0x00 for Edit Buffer)
   * @param paramId Parameter ID (0-384)
   * @param value New parameter value (0-127)
   * @return 10-byte SysEx message
   */
  public static byte[] generateParameterChange(int deviceId, int location, int paramId, int value) {
    byte[] sysex = new byte[10];

    sysex[0] = SYSEX_START;
    sysex[1] = WALDORF_ID;
    sysex[2] = BLOFELD_ID;
    sysex[3] = (byte) (deviceId & 0x7F);
    sysex[4] = 0x20; // SNDP command
    sysex[5] = (byte) (location & 0x7F);
    
    // Parameter ID is sent as two bytes (H and L)
    sysex[6] = (byte) ((paramId >> 7) & 0x7F);
    sysex[7] = (byte) (paramId & 0x7F);
    
    sysex[8] = (byte) (value & 0x7F);
    sysex[9] = SYSEX_END;

    return sysex;
  }

  /**
   * Generate a Sound Dump Data (SNDD) message.
   *
   * @param sound Sound object to convert
   * @param deviceId Device ID (0-127, use 0x7F for broadcast)
   * @return 392-byte SysEx message (Hardware standard)
   */
  public static byte[] generateSoundDump(Sound sound, int deviceId) {
    byte[] sysex = new byte[392];

    // Header
    sysex[0] = SYSEX_START;
    sysex[1] = WALDORF_ID;
    sysex[2] = BLOFELD_ID;
    sysex[3] = (byte) (deviceId & 0x7F);
    sysex[4] = SNDD_COMMAND;

    // Bank and program
    sysex[5] = (byte) (sound.getBank() & 0x7F);
    sysex[6] = (byte) (sound.getProgram() & 0x7F);

    // Parameters (383 bytes: parameters[0...382])
    System.arraycopy(sound.getParameters(), 0, sysex, 7, 383);

    // Checksum (constant)
    sysex[390] = CHECKSUM;

    // End
    sysex[391] = SYSEX_END;

    return sysex;
  }

  /**
   * Generate a Sound Dump Data (SNDD) message with broadcast device ID.
   *
   * @param sound Sound object to convert
   * @return 394-byte SysEx message
   */
  public static byte[] generateSoundDump(Sound sound) {
    return generateSoundDump(sound, 0x7F);
  }

  /**
   * Generate a Sound Dump Request (SNDR) message.
   *
   * @param bank Bank number (0-7 for A-H)
   * @param program Program number (0-127)
   * @param deviceId Device ID (0-127, use 0x7F for broadcast)
   * @return 9-byte SysEx message: F0 3E 13 dev 00 bank prog 7F F7
   */
  public static byte[] generateSoundRequest(int bank, int program, int deviceId) {
    byte[] sysex = new byte[9];

    sysex[0] = SYSEX_START;
    sysex[1] = WALDORF_ID;
    sysex[2] = BLOFELD_ID;
    sysex[3] = (byte) (deviceId & 0x7F);
    sysex[4] = (byte) 0x00; // SNDR command
    sysex[5] = (byte) (bank & 0x7F);
    sysex[6] = (byte) (program & 0x7F);
    sysex[7] = CHECKSUM;
    sysex[8] = SYSEX_END;

    return sysex;
  }

  /**
   * Generate a Sound Dump Request (SNDR) message with broadcast device ID.
   *
   * @param bank Bank number (0-7 for A-H)
   * @param program Program number (0-127)
   * @return 9-byte SysEx message
   */
  public static byte[] generateSoundRequest(int bank, int program) {
    return generateSoundRequest(bank, program, 0x7F);
  }
}
