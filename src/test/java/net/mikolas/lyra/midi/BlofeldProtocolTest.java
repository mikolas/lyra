package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;

import net.mikolas.lyra.exception.MidiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for BlofeldProtocol encoding and decoding.
 */
class BlofeldProtocolTest {
  private BlofeldProtocol protocol;

  @BeforeEach
  void setUp() {
    protocol = new BlofeldProtocol();
  }

  // ========== SNDP Encoding Tests ==========

  @Test
  void testEncodeSoundParameterChange() throws MidiException {
    SoundParameterChange msg = new SoundParameterChange(0x7F, 0x00, 1, 62);
    byte[] expected = {
      (byte) 0xF0, 0x3E, 0x13, 0x7F, 0x20, 0x00, 0x00, 0x01, 0x3E, (byte) 0xF7
    };

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDP encoding failed for param 1, value 62");
  }

  @Test
  void testEncodeSoundParameterChangeHighParamId() throws MidiException {
    SoundParameterChange msg = new SoundParameterChange(0x7F, 0x00, 200, 64);
    int msb = 200 >> 7;
    int lsb = 200 & 0x7F;
    byte[] expected = {
      (byte) 0xF0, 0x3E, 0x13, 0x7F, 0x20, 0x00, (byte) msb, (byte) lsb, 0x40, (byte) 0xF7
    };

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDP encoding failed for high param ID 200");
  }

  @Test
  void testEncodeSoundParameterChangeMaxParamId() throws MidiException {
    SoundParameterChange msg = new SoundParameterChange(0x7F, 0x00, 384, 127);
    int msb = 384 >> 7;
    int lsb = 384 & 0x7F;
    byte[] expected = {
      (byte) 0xF0, 0x3E, 0x13, 0x7F, 0x20, 0x00, (byte) msb, (byte) lsb, 0x7F, (byte) 0xF7
    };

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDP encoding failed for max param ID 384");
  }

  // ========== SNDR Encoding Tests ==========

  @Test
  void testEncodeSoundDumpRequest() throws MidiException {
    SoundDumpRequest msg = new SoundDumpRequest(0x7F, 0, 14);
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x00, 0x00, 0x0E, 0x7F, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDR encoding failed for Bank A, Program 14");
  }

  @Test
  void testEncodeSoundDumpRequestEditBuffer() throws MidiException {
    SoundDumpRequest msg = new SoundDumpRequest(0x7F, 127, 0);
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x00, 0x7F, 0x00, 0x7F, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDR encoding failed for edit buffer");
  }

  @Test
  void testEncodeSoundDumpRequestBankH() throws MidiException {
    SoundDumpRequest msg = new SoundDumpRequest(0x7F, 7, 127);
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x00, 0x07, 0x7F, 0x7F, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "SNDR encoding failed for Bank H, Program 127");
  }

  // ========== SNDD Encoding Tests ==========

  @Test
  void testEncodeSoundDumpData() throws MidiException {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = 64;
    }
    SoundDumpData msg = new SoundDumpData(0x7F, 0, 14, params);

    byte[] actual = protocol.encode(msg);

    assertEquals(392, actual.length, "SNDD message should be 392 bytes");
    assertEquals((byte) 0xF0, actual[0], "SNDD should start with 0xF0");
    assertEquals(0x3E, actual[1], "SNDD should have Waldorf ID 0x3E");
    assertEquals(0x13, actual[2], "SNDD should have Blofeld ID 0x13");
    assertEquals(0x7F, actual[3], "SNDD should have device ID 0x7F");
    assertEquals(0x10, actual[4], "SNDD should have command 0x10");
    assertEquals(0x00, actual[5], "SNDD should have bank 0");
    assertEquals(0x0E, actual[6], "SNDD should have program 14");
    
    // Check parameters (starting from parameters[2] because encoded message skips first 2 bytes)
    for (int i = 0; i < 383; i++) {
      assertEquals(64, actual[7 + i], "Parameter " + i + " should be 64");
    }
    
    assertEquals(0x7F, actual[390], "Checksum should be 0x7F wildcard");
    assertEquals((byte) 0xF7, actual[391], "SNDD should end with 0xF7");
  }

  @Test
  void testEncodeSoundDumpDataVariedParameters() throws MidiException {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = (byte) (i % 128);
    }
    SoundDumpData msg = new SoundDumpData(0x7F, 3, 99, params);

    byte[] actual = protocol.encode(msg);

    assertEquals(392, actual.length, "SNDD message should be 392 bytes");
    assertEquals(0x03, actual[5], "SNDD should have bank 3");
    assertEquals(0x63, actual[6], "SNDD should have program 99");
    
    for (int i = 0; i < 383; i++) {
      assertEquals((byte) ((i + 2) % 128), actual[7 + i], "Parameter " + i + " mismatch");
    }
    
    assertEquals(0x7F, actual[390], "Checksum should be 0x7F wildcard");
  }

  // ========== Other Message Encoding Tests ==========

  @Test
  void testEncodeMultiDumpRequest() throws MidiException {
    MultiDumpRequest msg = new MultiDumpRequest(0x7F, 0x00, 5); // Bank 0, Multi 5
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x01, 0x00, 0x05, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "MULR encoding failed (Bank 0)");
  }

  @Test
  void testEncodeMultiDumpRequestEditBuffer() throws MidiException {
    MultiDumpRequest msg = new MultiDumpRequest(0x7F, 127, 0); // Bank 127 (Edit Buffer)
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x01, 0x7F, 0x00, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "MULR encoding failed (Edit Buffer)");
  }

  @Test
  void testEncodeMultiDumpData() throws MidiException {
    byte[] payload = new byte[418];
    for(int i=0; i<418; i++) payload[i] = (byte)(i % 128);
    // Ensure payload has correct bank/prog at 0/1 for the test, 
    // although encode() just copies payload.
    payload[0] = 0;
    payload[1] = 5;
    
    MultiDumpData msg = new MultiDumpData(0x7F, 0, 5, payload);
    
    // Header: F0 3E 13 7F 11 (5 bytes)
    // Payload: 418 bytes
    // Checksum: 1 byte
    // End: F7 (1 byte)
    // Total: 425 bytes
    
    byte[] actual = protocol.encode(msg);
    
    assertEquals(425, actual.length);
    assertEquals((byte)0xF0, actual[0]);
    assertEquals(0x11, actual[4]); // MULD
    // Index 5 is start of payload
    assertEquals(payload[0], actual[5]); // Payload start (Bank)
    assertEquals(payload[1], actual[6]); // Payload byte 1 (Prog)
    assertEquals((byte)0xF7, actual[424]); // End
  }

  @Test
  void testDecodeMultiDumpRequest() throws MidiException {
    byte[] data = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x01, 0x00, 0x05, (byte) 0xF7};
    MidiMessage msg = protocol.decode(data);
    assertInstanceOf(MultiDumpRequest.class, msg);
    MultiDumpRequest req = (MultiDumpRequest) msg;
    assertEquals(0, req.bank());
    assertEquals(5, req.multi());
  }

  @Test
  void testDecodeMultiDumpData() throws MidiException {
    byte[] payload = new byte[418];
    int sum = 0;
    for(int i=0; i<418; i++) {
        payload[i] = (byte)(i % 128);
        sum += (payload[i] & 0xFF);
    }
    // Set bank/prog in payload for extraction test
    payload[0] = 0;
    payload[1] = 5;
    // Update sum for changed bytes (assuming original loop set them differently)
    // Re-calculate sum strictly
    sum = 0;
    for(byte b : payload) sum += (b & 0xFF);
    
    int checksum = sum & 0x7F;
    
    byte[] data = new byte[425];
    data[0] = (byte)0xF0;
    data[1] = 0x3E;
    data[2] = 0x13;
    data[3] = 0x7F;
    data[4] = 0x11;
    // Data starts at 5
    System.arraycopy(payload, 0, data, 5, 418);
    data[423] = (byte)checksum;
    data[424] = (byte)0xF7;
    
    MidiMessage msg = protocol.decode(data);
    assertInstanceOf(MultiDumpData.class, msg);
    MultiDumpData muld = (MultiDumpData) msg;
    assertEquals(0, muld.bank());
    assertEquals(5, muld.multi());
    assertArrayEquals(payload, muld.data());
  }

  @Test
  void testEncodeGlobalParametersRequest() throws MidiException {
    GlobalParametersRequest msg = new GlobalParametersRequest(0x7F);
    byte[] expected = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x04, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "GLBR encoding failed");
  }

  @Test
  void testEncodeDeviceIdentityRequest() throws MidiException {
    DeviceIdentityRequest msg = new DeviceIdentityRequest(0x7F);
    byte[] expected = {(byte) 0xF0, 0x7E, 0x7F, 0x06, 0x01, (byte) 0xF7};

    byte[] actual = protocol.encode(msg);

    assertArrayEquals(expected, actual, "Device Identity Request encoding failed");
  }

  // ========== Checksum Tests ==========

  @Test
  void testCalculateChecksumAllZeros() {
    byte[] params = new byte[385];

    int checksum = protocol.calculateChecksum(params);

    assertEquals(0, checksum, "Checksum of all zeros should be 0");
  }

  @Test
  void testCalculateChecksumAllSixtyFour() {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = 64;
    }

    int checksum = protocol.calculateChecksum(params);

    assertEquals((385 * 64) & 0x7F, checksum, "Checksum calculation incorrect");
  }

  @Test
  void testCalculateChecksumMaxValues() {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = 127;
    }

    int checksum = protocol.calculateChecksum(params);

    assertEquals((385 * 127) & 0x7F, checksum, "Checksum with max values incorrect");
  }

  // ========== Decoding Tests ==========

  @Test
  void testDecodeSoundParameterChange() throws MidiException {
    byte[] data = {
      (byte) 0xF0, 0x3E, 0x13, 0x7F, 0x20, 0x00, 0x00, 0x01, 0x3E, (byte) 0xF7
    };

    MidiMessage msg = protocol.decode(data);

    assertInstanceOf(SoundParameterChange.class, msg, "Should decode to SoundParameterChange");
    SoundParameterChange sndp = (SoundParameterChange) msg;
    assertEquals(0x7F, sndp.deviceId(), "Device ID mismatch");
    assertEquals(0x00, sndp.location(), "Location mismatch");
    assertEquals(1, sndp.paramId(), "Parameter ID mismatch");
    assertEquals(62, sndp.value(), "Value mismatch");
  }

  @Test
  void testDecodeSoundDumpRequest() throws MidiException {
    byte[] data = {(byte) 0xF0, 0x3E, 0x13, 0x7F, 0x00, 0x00, 0x0E, 0x7F, (byte) 0xF7};

    MidiMessage msg = protocol.decode(data);

    assertInstanceOf(SoundDumpRequest.class, msg, "Should decode to SoundDumpRequest");
    SoundDumpRequest sndr = (SoundDumpRequest) msg;
    assertEquals(0x7F, sndr.deviceId(), "Device ID mismatch");
    assertEquals(0, sndr.bank(), "Bank mismatch");
    assertEquals(14, sndr.program(), "Program mismatch");
  }

  @Test
  void testDecodeSoundDumpData() throws MidiException {
    byte[] params = new byte[385];
    for (int i = 0; i < 385; i++) {
      params[i] = 64;
    }
    int checksum = 0x7F; // wildcard
    
    byte[] data = new byte[392];
    data[0] = (byte) 0xF0;
    data[1] = 0x3E;
    data[2] = 0x13;
    data[3] = 0x7F;
    data[4] = 0x10;
    data[5] = 0x00;
    data[6] = 0x0E;
    // Copy 383 params to SysEx (skipping first 2 padding bytes of internal array)
    System.arraycopy(params, 2, data, 7, 383);
    data[390] = (byte) checksum;
    data[391] = (byte) 0xF7;

    MidiMessage msg = protocol.decode(data);

    assertInstanceOf(SoundDumpData.class, msg, "Should decode to SoundDumpData");
    SoundDumpData sndd = (SoundDumpData) msg;
    assertEquals(0x7F, sndd.deviceId(), "Device ID mismatch");
    assertEquals(0, sndd.bank(), "Bank mismatch");
    assertEquals(14, sndd.program(), "Program mismatch");
    
    // The decoded parameters should have indices 0 and 1 as zero (default)
    assertEquals(0, sndd.parameters()[0]);
    assertEquals(0, sndd.parameters()[1]);
    // And indices 2-384 should match the data we sent
    for(int i=2; i<385; i++) {
        assertEquals(64, sndd.parameters()[i], "Parameter at " + i + " mismatch");
    }
  }

  @Test
  void testDecodeInvalidMessageThrowsException() {
    byte[] data = {(byte) 0xF0, 0x00, 0x00, (byte) 0xF7};

    assertThrows(MidiException.class, () -> protocol.decode(data), 
        "Should throw MidiException for invalid message");
  }
}

