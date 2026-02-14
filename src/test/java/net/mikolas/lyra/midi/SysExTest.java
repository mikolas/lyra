package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;

import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.SoundTestData;
import org.junit.jupiter.api.Test;

/** Tests for SysEx parser and generator. */
class SysExTest {

  @Test
  void testParseSoundDump() {
    // Create valid SNDD message
    byte[] sysex = createValidSoundDump();

    Sound sound = SysExParser.parseSoundDump(sysex);

    assertNotNull(sound);
    assertEquals(0, sound.getBank());
    assertEquals(0, sound.getProgram());
    assertEquals(385, sound.getParameters().length);
  }

  @Test
  void testParseInvalidLength() {
    byte[] sysex = new byte[100]; // Too short
    assertNull(SysExParser.parseSoundDump(sysex));
  }

  @Test
  void testParseInvalidHeader() {
    byte[] sysex = createValidSoundDump();
    sysex[1] = 0x00; // Wrong manufacturer ID

    assertNull(SysExParser.parseSoundDump(sysex));
  }

  // Note: Checksum validation removed - Python Bigglesworth doesn't validate it
  // The checksum at byte 390 is calculated by Blofeld and varies per sound

  @Test
  void testGenerateSoundDump() {
    Sound sound = SoundTestData.createInitSound();

    byte[] sysex = SysExGenerator.generateSoundDump(sound);

    assertEquals(392, sysex.length);
    assertEquals((byte) 0xF0, sysex[0]); // SysEx start
    assertEquals(0x3E, sysex[1]); // Waldorf ID
    assertEquals(0x13, sysex[2]); // Blofeld ID
    assertEquals(0x10, sysex[4]); // SNDD command
    assertEquals((byte) 0x7F, sysex[390]); // Checksum constant
    assertEquals((byte) 0xF7, sysex[391]); // SysEx end
  }

  @Test
  void testGenerateSoundRequest() {
    byte[] sysex = SysExGenerator.generateSoundRequest(0, 0);

    assertEquals(9, sysex.length);
    assertEquals((byte) 0xF0, sysex[0]); // SysEx start
    assertEquals(0x3E, sysex[1]); // Waldorf ID
    assertEquals(0x13, sysex[2]); // Blofeld ID
    assertEquals(0x00, sysex[4]); // SNDR command
    assertEquals(0x00, sysex[5]); // Bank 0
    assertEquals(0x00, sysex[6]); // Program 0
    assertEquals((byte) 0x7F, sysex[7]); // Checksum constant
    assertEquals((byte) 0xF7, sysex[8]); // SysEx end
  }

  @Test
  void testGenerateSoundRequestWithDeviceId() {
    byte[] sysex = SysExGenerator.generateSoundRequest(3, 64, 5);

    assertEquals(9, sysex.length);
    assertEquals(0x05, sysex[3]); // Device ID 5
    assertEquals(0x03, sysex[5]); // Bank 3
    assertEquals(0x40, sysex[6]); // Program 64
  }

  @Test
  void testRoundTripConversion() {
    // Create sound
    Sound original = SoundTestData.createInitSound();

    // Generate SysEx
    byte[] sysex = SysExGenerator.generateSoundDump(original);

    // Parse back
    Sound parsed = SysExParser.parseSoundDump(sysex);

    // Verify
    assertNotNull(parsed);
    assertEquals(original.getName(), parsed.getName());
    assertEquals(original.getBank(), parsed.getBank());
    assertEquals(original.getProgram(), parsed.getProgram());
    assertArrayEquals(original.getParameters(), parsed.getParameters());
  }

  @Test
  void testExtractName() {
    byte[] sysex = createValidSoundDump();
    // Name is at ID 363-378. 
    // In 392-byte sysex, this is bytes 7+363 to 7+378 = 370 to 385
    for (int i = 370; i <= 385; i++) {
      sysex[i] = ' ';
    }
    sysex[370] = 'T';
    sysex[371] = 'e';
    sysex[372] = 's';
    sysex[373] = 't';

    Sound sound = SysExParser.parseSoundDump(sysex);

    assertNotNull(sound);
    assertEquals("Test", sound.getName());
  }

  @Test
  void testExtractCategory() {
    byte[] sysex = createValidSoundDump();
    // Category is at ID 379.
    // In 392-byte sysex, this is byte 7+379 = 386
    sysex[386] = 3; // Bass

    Sound sound = SysExParser.parseSoundDump(sysex);

    assertNotNull(sound);
    assertEquals(3, sound.getCategory()); // Now returns Integer, not String
  }

  // Helper methods

  private byte[] createValidSoundDump() {
    byte[] sysex = new byte[392];

    // Header
    sysex[0] = (byte) 0xF0;
    sysex[1] = 0x3E; // Waldorf
    sysex[2] = 0x13; // Blofeld
    sysex[3] = 0x7F; // Device ID
    sysex[4] = 0x10; // SNDD

    // Bank and program
    sysex[5] = 0; // Bank A
    sysex[6] = 0; // Program 0

    // Parameters (383 bytes from params[2-384])
    System.arraycopy(SoundTestData.INIT_SOUND, 2, sysex, 7, 383);

    // Checksum (constant)
    sysex[390] = (byte) 0x7F;

    // End
    sysex[391] = (byte) 0xF7;

    return sysex;
  }
}
