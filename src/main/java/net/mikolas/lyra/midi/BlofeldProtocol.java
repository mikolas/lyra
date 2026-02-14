package net.mikolas.lyra.midi;

import net.mikolas.lyra.exception.MidiException;

/**
 * Waldorf Blofeld MIDI protocol encoder/decoder.
 *
 * <p>Handles encoding and decoding of all Blofeld SysEx messages including SNDP, SNDR, SNDD,
 * MULR, MULD, GLBR, GLBD, WTBD, and Device Identity messages.
 */
public class BlofeldProtocol {

  // Manufacturer IDs
  public static final byte WALDORF_ID = 0x3E;
  public static final byte BLOFELD_ID = 0x13;
  public static final byte UNIVERSAL_SYSEX = 0x7E;

  // SysEx delimiters
  public static final byte SYSEX_START = (byte) 0xF0;
  public static final byte SYSEX_END = (byte) 0xF7;

  // Commands
  public static final byte CMD_SOUND_DUMP_REQUEST = 0x00; // SNDR
  public static final byte CMD_MULTI_DUMP_REQUEST = 0x01; // MULR
  public static final byte CMD_GLOBAL_DUMP_REQUEST = 0x04; // GLBR
  public static final byte CMD_SOUND_DUMP = 0x10; // SNDD
  public static final byte CMD_MULTI_DUMP = 0x11; // MULD
  public static final byte CMD_WAVETABLE_DUMP = 0x12; // WTBD
  public static final byte CMD_GLOBAL_DUMP = 0x14; // GLBD
  public static final byte CMD_SOUND_PARAMETER = 0x20; // SNDP
  public static final byte CMD_GLOBAL_PARAMETER = 0x05; // GLBP

  // Special bank values
  public static final int EDIT_BUFFER_BANK = 127;
  public static final int MULTI_MODE_BANK = 127;
  public static final int BROADCAST_DEVICE_ID = 0x7F;

  // Parameter limits
  public static final int MAX_PARAM_ID = 384;
  public static final int MAX_PARAM_VALUE = 127;
  public static final int SOUND_PARAM_COUNT = 385;

  private int deviceId = BROADCAST_DEVICE_ID;

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  /**
   * Encode a MIDI message to SysEx bytes.
   *
   * @param message The message to encode
   * @return SysEx byte array
   * @throws MidiException if encoding fails
   */
  public byte[] encode(MidiMessage message) throws MidiException {
    return switch (message) {
      case SoundParameterChange msg -> encodeSoundParameterChange(msg);
      case SoundDumpRequest msg -> encodeSoundDumpRequest(msg);
      case SoundDumpData msg -> encodeSoundDumpData(msg);
      case MultiDumpRequest msg -> encodeMultiDumpRequest(msg);
      case MultiDumpData msg -> encodeMultiDumpData(msg);
      case GlobalParametersRequest msg -> encodeGlobalParametersRequest(msg);
      case GlobalParametersData msg -> encodeGlobalParametersData(msg);
      case GlobalParameterChange msg -> encodeGlobalParameterChange(msg);
      case WavetableDump msg -> encodeWavetableDump(msg);
      case DeviceIdentityRequest msg -> encodeDeviceIdentityRequest(msg);
    };
  }

  // ... (existing code)

  private byte[] encodeGlobalParameterChange(GlobalParameterChange msg) {
    int idMsb = msg.paramId() >> 7;
    int idLsb = msg.paramId() & 0x7F;
    int valMsb = msg.value() >> 7;
    int valLsb = msg.value() & 0x7F;

    return new byte[] {
      SYSEX_START,
      WALDORF_ID,
      BLOFELD_ID,
      (byte) msg.deviceId(),
      CMD_GLOBAL_PARAMETER,
      (byte) idMsb,
      (byte) idLsb,
      (byte) valMsb,
      (byte) valLsb,
      SYSEX_END
    };
  }

  /**
   * Decode SysEx bytes to a MIDI message.
   *
   * @param data SysEx byte array
   * @return Decoded MIDI message
   * @throws MidiException if decoding fails or message is invalid
   */
  public MidiMessage decode(byte[] data) throws MidiException {
    if (data == null || data.length < 4) {
      throw new MidiException("Invalid SysEx message: too short");
    }

    if (data[0] != SYSEX_START) {
      throw new MidiException("Invalid SysEx message: missing start byte");
    }

    if (data[data.length - 1] != SYSEX_END) {
      throw new MidiException("Invalid SysEx message: missing end byte");
    }

    // Universal SysEx (Device Identity)
    if (data[1] == UNIVERSAL_SYSEX) {
      return decodeUniversalSysEx(data);
    }

    // Waldorf Blofeld SysEx
    if (data[1] != WALDORF_ID || data[2] != BLOFELD_ID) {
      throw new MidiException("Invalid SysEx message: not a Blofeld message");
    }

    int deviceId = data[3] & 0xFF;
    byte command = data[4];

    return switch (command) {
      case CMD_SOUND_PARAMETER -> decodeSoundParameterChange(data, deviceId);
      case CMD_SOUND_DUMP_REQUEST -> decodeSoundDumpRequest(data, deviceId);
      case CMD_SOUND_DUMP -> decodeSoundDumpData(data, deviceId);
      case CMD_MULTI_DUMP_REQUEST -> decodeMultiDumpRequest(data, deviceId);
      case CMD_MULTI_DUMP -> decodeMultiDumpData(data, deviceId);
      case CMD_GLOBAL_DUMP_REQUEST -> decodeGlobalParametersRequest(data, deviceId);
      case CMD_GLOBAL_DUMP -> decodeGlobalParametersData(data, deviceId);
      case CMD_GLOBAL_PARAMETER -> decodeGlobalParameterChange(data, deviceId);
      case CMD_WAVETABLE_DUMP -> decodeWavetableDump(data, deviceId);
      default -> throw new MidiException("Unknown command byte: 0x" + Integer.toHexString(command));
    };
  }

  private MidiMessage decodeGlobalParameterChange(byte[] data, int deviceId) throws MidiException {
    if (data.length != 10) {
      throw new MidiException("Invalid GLBP message length: " + data.length);
    }

    int idMsb = data[5] & 0x7F;
    int idLsb = data[6] & 0x7F;
    int paramId = (idMsb << 7) | idLsb;

    int valMsb = data[7] & 0x7F;
    int valLsb = data[8] & 0x7F;
    int value = (valMsb << 7) | valLsb;

    return new GlobalParameterChange(deviceId, paramId, value);
  }

  /**
   * Calculate checksum for parameter data.
   *
   * @param data Parameter data
   * @return Checksum value (0-127)
   */
  public int calculateChecksum(byte[] data) {
    int sum = 0;
    for (byte b : data) {
      sum += (b & 0xFF);
    }
    return sum & 0x7F;
  }

  // ========== Encoding Methods ==========

  private byte[] encodeSoundParameterChange(SoundParameterChange msg) {
    int msb = msg.paramId() >> 7;
    int lsb = msg.paramId() & 0x7F;

    return new byte[] {
      SYSEX_START,
      WALDORF_ID,
      BLOFELD_ID,
      (byte) msg.deviceId(),
      CMD_SOUND_PARAMETER,
      (byte) msg.location(),
      (byte) msb,
      (byte) lsb,
      (byte) msg.value(),
      SYSEX_END
    };
  }

  private byte[] encodeSoundDumpRequest(SoundDumpRequest msg) {
    return new byte[] {
      SYSEX_START,
      WALDORF_ID,
      BLOFELD_ID,
      (byte) msg.deviceId(),
      CMD_SOUND_DUMP_REQUEST,
      (byte) msg.bank(),
      (byte) msg.program(),
      (byte) 0x7F, // Checksum placeholder
      SYSEX_END
    };
  }

  private byte[] encodeSoundDumpData(SoundDumpData msg) {
    // Hardware standard: 392 bytes (F0 + 3E + 13 + dev + 10 + bank + prog + 383 params + checksum + F7)
    byte[] data = new byte[392]; 
    int pos = 0;

    data[pos++] = SYSEX_START;
    data[pos++] = WALDORF_ID;
    data[pos++] = BLOFELD_ID;
    data[pos++] = (byte) msg.deviceId();
    data[pos++] = CMD_SOUND_DUMP;
    data[pos++] = (byte) msg.bank();
    data[pos++] = (byte) msg.program();

    // Copy 383 parameters (starting from internal index 2)
    System.arraycopy(msg.parameters(), 2, data, pos, 383);
    pos += 383;

    data[pos++] = (byte) 0x7F; // Use wildcard checksum
    data[pos] = SYSEX_END;

    return data;
  }

  private byte[] encodeMultiDumpRequest(MultiDumpRequest msg) {
    return new byte[] {
      SYSEX_START,
      WALDORF_ID,
      BLOFELD_ID,
      (byte) msg.deviceId(),
      CMD_MULTI_DUMP_REQUEST,
      (byte) msg.bank(),
      (byte) msg.multi(),
      SYSEX_END
    };
  }

  private byte[] encodeMultiDumpData(MultiDumpData msg) {
    // MULD (0x11) uses a 5-byte header: F0 3E 13 Dev 11
    // The Bank and Program are embedded in the first 2 bytes of the payload.
    byte[] data = new byte[5 + msg.data().length + 2]; // 5 Header + Data + Chk + F7
    int pos = 0;

    data[pos++] = SYSEX_START;
    data[pos++] = WALDORF_ID;
    data[pos++] = BLOFELD_ID;
    data[pos++] = (byte) msg.deviceId();
    data[pos++] = CMD_MULTI_DUMP;
    // Bank and Prog are NOT in the header for MULD

    System.arraycopy(msg.data(), 0, data, pos, msg.data().length);
    pos += msg.data().length;

    data[pos++] = (byte) calculateChecksum(msg.data());
    data[pos++] = SYSEX_END;

    return data;
  }

  private byte[] encodeGlobalParametersRequest(GlobalParametersRequest msg) {
    return new byte[] {
      SYSEX_START, WALDORF_ID, BLOFELD_ID, (byte) msg.deviceId(), CMD_GLOBAL_DUMP_REQUEST, SYSEX_END
    };
  }

  private byte[] encodeGlobalParametersData(GlobalParametersData msg) {
    byte[] data = new byte[6 + msg.data().length + 2];
    int pos = 0;

    data[pos++] = SYSEX_START;
    data[pos++] = WALDORF_ID;
    data[pos++] = BLOFELD_ID;
    data[pos++] = (byte) msg.deviceId();
    data[pos++] = CMD_GLOBAL_DUMP;

    System.arraycopy(msg.data(), 0, data, pos, msg.data().length);
    pos += msg.data().length;

    data[pos++] = (byte) calculateChecksum(msg.data());
    data[pos++] = SYSEX_END;

    return data;
  }

  private byte[] encodeWavetableDump(WavetableDump msg) {
    // Spec: Total Length 410 bytes
    byte[] data = new byte[410];
    int pos = 0;

    data[pos++] = SYSEX_START;
    data[pos++] = WALDORF_ID;
    data[pos++] = BLOFELD_ID;
    data[pos++] = (byte) msg.deviceId();
    data[pos++] = CMD_WAVETABLE_DUMP;
    data[pos++] = (byte) msg.slot();
    data[pos++] = (byte) msg.waveNumber();
    data[pos++] = 0x00; // Format (always 0)

    // Data: 384 bytes (128 samples * 3 bytes)
    for (int sample : msg.samples()) {
        // Convert to 21-bit unsigned (two's complement)
        int v = (sample < 0) ? (2097152 + sample) : sample;
        data[pos++] = (byte) ((v >> 14) & 0x7F); // MSB
        data[pos++] = (byte) ((v >> 7) & 0x7F);  // Mid
        data[pos++] = (byte) (v & 0x7F);         // LSB
    }

    // Name: 14 bytes (ASCII padded with spaces)
    String name = msg.name() != null ? msg.name() : "";
    for (int i = 0; i < 14; i++) {
        if (i < name.length()) {
            char c = name.charAt(i);
            // Blofeld 7-bit ASCII range (0x20-0x7E)
            data[pos++] = (byte) ((c >= 32 && c <= 126) ? c : 0x7F);
        } else {
            data[pos++] = 0x20; // Space padding
        }
    }

    data[pos++] = 0x00; // Reserved
    data[pos++] = 0x00; // Reserved
    
    // Checksum Placeholder (0x7F per spec)
    data[pos++] = 0x7F; 
    data[pos++] = SYSEX_END;

    return data;
  }

  private byte[] encodeDeviceIdentityRequest(DeviceIdentityRequest msg) {
    return new byte[] {
      SYSEX_START, UNIVERSAL_SYSEX, (byte) msg.deviceId(), 0x06, 0x01, SYSEX_END
    };
  }

  // ========== Decoding Methods ==========

  private MidiMessage decodeSoundParameterChange(byte[] data, int deviceId) throws MidiException {
    if (data.length != 10) {
      throw new MidiException("Invalid SNDP message length: " + data.length);
    }

    int location = data[5] & 0xFF;
    int msb = data[6] & 0xFF;
    int lsb = data[7] & 0xFF;
    int paramId = (msb << 7) | lsb;
    int value = data[8] & 0xFF;

    return new SoundParameterChange(deviceId, location, paramId, value);
  }

  private MidiMessage decodeSoundDumpRequest(byte[] data, int deviceId) throws MidiException {
    if (data.length != 9) {
      throw new MidiException("Invalid SNDR message length: " + data.length);
    }

    int bank = data[5] & 0xFF;
    int program = data[6] & 0xFF;

    return new SoundDumpRequest(deviceId, bank, program);
  }

  private MidiMessage decodeSoundDumpData(byte[] data, int deviceId) throws MidiException {
    if (data.length != 394 && data.length != 392) {
      throw new MidiException("Invalid SNDD message length: " + data.length);
    }

    int bank = data[5] & 0xFF;
    int program = data[6] & 0xFF;

    byte[] parameters = new byte[SOUND_PARAM_COUNT];
    if (data.length == 392) {
        // Copy 383 parameters starting from internal index 2
        System.arraycopy(data, 7, parameters, 2, 383);
    } else {
        // Copy all 385 parameters
        System.arraycopy(data, 7, parameters, 0, SOUND_PARAM_COUNT);
    }

    int receivedChecksum = data[data.length - 2] & 0xFF;
    int calculatedChecksum = calculateChecksum(parameters);

    if (receivedChecksum != calculatedChecksum) {
      System.err.printf("Checksum mismatch: received 0x%02X, calculated 0x%02X (Ignoring per resilient parser spec)%n",
          receivedChecksum, calculatedChecksum);
    }

    return new SoundDumpData(deviceId, bank, program, parameters);
  }

  private MidiMessage decodeMultiDumpRequest(byte[] data, int deviceId) throws MidiException {
    if (data.length != 8) {
      throw new MidiException("Invalid MULR message length: " + data.length);
    }

    int bank = data[5] & 0xFF;
    int multi = data[6] & 0xFF;
    return new MultiDumpRequest(deviceId, bank, multi);
  }

  private MidiMessage decodeMultiDumpData(byte[] data, int deviceId) throws MidiException {
    // MULD length is 425 bytes (5 header + 418 payload + 2 footer)
    if (data.length < 9) { // Basic sanity check, though 425 is expected
      throw new MidiException("Invalid MULD message length: " + data.length);
    }

    int dataLength = data.length - 7; // Header (5) + Checksum + End (2) = 7

    byte[] multiData = new byte[dataLength];
    System.arraycopy(data, 5, multiData, 0, dataLength);

    int receivedChecksum = data[data.length - 2] & 0xFF;
    int calculatedChecksum = calculateChecksum(multiData);

    if (receivedChecksum != calculatedChecksum) {
      System.err.printf("Checksum mismatch: received 0x%02X, calculated 0x%02X (Ignoring per resilient parser spec)%n",
          receivedChecksum, calculatedChecksum);
    }

    // Extract Bank and Multi from payload
    int bank = 0;
    int multi = 0;
    if (multiData.length >= 2) {
        bank = multiData[0] & 0xFF;
        multi = multiData[1] & 0xFF;
    }

    return new MultiDumpData(deviceId, bank, multi, multiData);
  }

  private MidiMessage decodeGlobalParametersRequest(byte[] data, int deviceId)
      throws MidiException {
    if (data.length != 6) {
      throw new MidiException("Invalid GLBR message length: " + data.length);
    }

    return new GlobalParametersRequest(deviceId);
  }

  private MidiMessage decodeGlobalParametersData(byte[] data, int deviceId) throws MidiException {
    if (data.length < 8) {
      throw new MidiException("Invalid GLBD message length: " + data.length);
    }

    int dataLength = data.length - 7;
    byte[] globalData = new byte[dataLength];
    System.arraycopy(data, 5, globalData, 0, dataLength);

    int receivedChecksum = data[data.length - 2] & 0xFF;
    int calculatedChecksum = calculateChecksum(globalData);

    if (receivedChecksum != calculatedChecksum) {
      System.err.printf("Checksum mismatch: received 0x%02X, calculated 0x%02X (Ignoring per resilient parser spec)%n",
          receivedChecksum, calculatedChecksum);
    }

    return new GlobalParametersData(deviceId, globalData);
  }

  private MidiMessage decodeWavetableDump(byte[] data, int deviceId) throws MidiException {
    if (data.length < 410) {
      throw new MidiException("Invalid WTBD message length (expected 410): " + data.length);
    }

    int slot = data[5] & 0xFF;
    int waveNumber = data[6] & 0xFF;
    
    // Decode 128 samples (3 bytes each)
    int[] samples = new int[128];
    for (int i = 0; i < 128; i++) {
        int pos = 8 + (i * 3);
        int v = ((data[pos] & 0x7F) << 14) | ((data[pos+1] & 0x7F) << 7) | (data[pos+2] & 0x7F);
        // Convert from 21-bit unsigned to signed
        if (v >= 1048576) {
            v -= 2097152;
        }
        samples[i] = v;
    }

    // Decode Name (14 bytes)
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 14; i++) {
        char c = (char) (data[392 + i] & 0xFF);
        sb.append(c);
    }
    String name = sb.toString().trim();

    return new WavetableDump(deviceId, slot, waveNumber, samples, name);
  }

  private MidiMessage decodeUniversalSysEx(byte[] data) throws MidiException {
    if (data.length < 6) {
      throw new MidiException("Invalid Universal SysEx message length: " + data.length);
    }

    int deviceId = data[2] & 0xFF;
    int subId1 = data[3] & 0xFF;
    int subId2 = data[4] & 0xFF;

    if (subId1 == 0x06 && subId2 == 0x01) {
      return new DeviceIdentityRequest(deviceId);
    }

    throw new MidiException("Unknown Universal SysEx message");
  }
}
