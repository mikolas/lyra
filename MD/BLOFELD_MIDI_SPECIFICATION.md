# Blofeld MIDI Protocol Specification

This document provides a complete, "clean room" specification of the Waldorf Blofeld MIDI protocol, derived from analysis of legacy driver code. It covers all known functionality including Sound, Multi, Global, and Wavetable operations.

## 1. General Protocol Definitions

### 1.1 Constants

| Constant | Value | Description |
| :--- | :--- | :--- |
| **INIT** | `0xF0` | System Exclusive Start |
| **IDW** | `0x3E` | Waldorf Manufacturer ID |
| **IDE** | `0x13` | Blofeld Model ID |
| **END** | `0xF7` | System Exclusive End |
| **CHK** | `0x7F` | Universal Valid Checksum (used by sender) |
| **DevID** | `0x00`-`0x7E` | Device ID (Broadcast: `0x7F`) |

### 1.2 Command IDs

| ID | Name | Description |
| :--- | :--- | :--- |
| `0x00` | **SNDR** | Sound Request |
| `0x01` | **MULR** | Multi Request |
| `0x10` | **SNDD** | Sound Dump |
| `0x11` | **MULD** | Multi Dump |
| `0x12` | **WTBD** | Wavetable Dump |
| `0x14` | **GLBD** | Global Dump |
| `0x20` | **SNDP** | Sound Parameter Change |
| `0x05` | **GLBP** | Global Parameter Change |
| `0x04` | **GLBR** | Global Request |

### 1.3 General Notes

*   **Checksum**: The Blofeld accepts `0x7F` as a valid "wildcard" checksum for all received messages. Real checksum calculation is not required for sending data to the device.
*   **Multi-byte Values**: Unless specified otherwise, values > 127 are transmitted as multiple 7-bit bytes (MSB first).
*   **Blofeld ASCII**: The Blofeld uses a modified 7-bit ASCII set for all name fields. 
    *   `0x00-0x1F`: Rendered as Space (` `).
    *   `0x20-0x7E`: Standard printable ASCII.
    *   `0x7F`: Degree symbol (`°`).

---

## 2. Sound Operations

### 2.1 Fetch Sound (Request)

Requests a single sound dump (or bulk dump) from the device.

**Message**: `SNDR` (`0x00`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 00` |
| 5 | `Bank` | Bank Number (0-7: A-H). See Note below. |
| 6 | `Prog` | Program Number (0-127). |
| 7 | `0x7F` | Checksum (Placeholder) |
| 8 | `0xF7` | End |

**Special Banks**:
*   **Edit Buffer**: `Bank 0x7F`.
    *   If in **Sound Mode**: `Prog` is ignored (use 0).
    *   If in **Multi Mode**: `Prog` (0-15) specifies the **Part Number** to fetch from.
*   **Bulk Dump**: `Bank 0x40`. Triggers a sequential dump of all internal sounds.

### 2.2 Push Sound (Dump)

Transmits a full sound structure to the device. Used for restoring presets or updating the edit buffer.

**Message**: `SNDD` (`0x10`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 10` |
| 5 | `Bank` | Target Bank (0-7). Use `0x7F` for Edit Buffer. |
| 6 | `Prog` | Target Program (0-127). Part Index if Bank is `0x7F`. |
| 7-391 | `Data` | **385 bytes** of Sound Data. (383 Params + 2 Padding). |
| 392 | `0x7F` | Checksum (Placeholder) |
| 393 | `0xF7` | End |

**Total Length**: 394 bytes.

#### Sound Parameter Bitfields
| Offset | Parameter | Bits | Values |
| :--- | :--- | :--- | :--- |
| **49** | Osc 2 Sync to O3 | 0 | 0: Off, 1: On |
| **53** | Glide | 0 | 0: Off, 1: On |
| **58** | Allocation/Unisono| 0 | Allocation Mode (0: Poly, 1: Mono) |
| | | 4-6 | Unisono (0: Off, 1: Dual, 2: 3, 3: 4, 4: 5, 5: 6) |
| **117** | Filter Routing | 0 | 0: Parallel, 1: Serial |
| **196** | Filter Env Mode | 0-2 | 0: ADSR, 1: ADS1DS2R, 2: One Shot, 3: Loop S1S2, 4: Loop All |
| | | 5 | Trigger (0: Normal, 1: Single) |
| **208** | Amp Env Mode | 0-2 | (Same as Filter Env) |
| | | 5 | (Same as Filter Env) |
| **220** | Env 3 Mode | 0-2 | (Same as Filter Env) |
| | | 5 | (Same as Filter Env) |
| **232** | Env 4 Mode | 0-2 | (Same as Filter Env) |
| | | 5 | (Same as Filter Env) |
| **327-342**| Arp Step SGA 1-16 | 0-2 | Accent (0: Silent, 4: *1, 7: *4) |
| | | 3 | Glide (0: Off, 1: On) |
| | | 4-6 | Step (0: Normal, 1: Pause, 2: Prev, 3: First, 4: Last, 6: Chord, 7: Rand) |
| **343-358**| Arp Step T/L 1-16 | 0-2 | Timing (0: Rand, 4: +0, 7: +3) |
| | | 4-6 | Length (0: Legato, 4: +0, 7: +3) |
| **363-378**| Sound Name | 0-6 | 16 characters (Blofeld ASCII, 127 = `°`) |
| **379** | Category | 0-6 | Sound Category (0: Init, 1: Arp, 3: Bass, 7: Lead, 9: Pad, etc.) |

### 2.3 Parameter Change

Real-time update of a single sound parameter. Works for both Sound Mode and Multi Parts.

**Message**: `SNDP` (`0x20`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 20` |
| 5 | `Loc` | Location. `0x00` for Sound Mode. `0x01-0x10` for Multi Parts 1-16. |
| 6 | `ID_MSB` | Parameter ID High Byte (bit 7 of 14-bit ID) |
| 7 | `ID_LSB` | Parameter ID Low Byte (bits 0-6 of 14-bit ID) |
| 8 | `Value` | New Value (0-127) |
| 9 | `0xF7` | End |

**Note**: No checksum in `SNDP`.

---

## 3. Multi Operations

### 3.1 Fetch Multi (Request)

Requests a Multi dump or bulk dump.

**Message**: `MULR` (`0x01`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 01` |
| 5 | `Bank` | `0x00` for Memory, `0x7F` for Edit Buffer, `0x40` for Bulk Dump. |
| 6 | `Prog` | Multi Number (0-127). Ignored if Bank is `0x7F` or `0x40`. |
| 7 | `0xF7` | End |

### 3.2 Push Multi (Dump)

Transmits a full Multi structure.

**Message**: `MULD` (`0x11`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 11` |
| 5-38 | `Meta` | Multi Metadata (34 bytes). See below. |
| 39-422| `Parts` | **384 bytes**. 16 Parts × 24 bytes each. |
| 423 | `0x7F` | Checksum |
| 424 | `0xF7` | End |

#### Multi Metadata (Bytes 0-33 of Payload)
| Offset | Field | Description |
| :--- | :--- | :--- |
| 0 | Mode | 0: Memory, 127: Edit Buffer |
| 1 | Multi # | Multi Number (0-127) |
| 2-17 | Name | Multi Name (16 Characters). |
| 18 | Unk | Unknown Parameter (0). |
| 19 | Vol | Master Volume (0-127). |
| 20 | Tempo | BPM encoded (40-300 range). |
| 21-33 | Unk | Unknown Data: `[1, 0, 2, 4, 11, 12, 0, 0, 0, 0, 0, 0, 0]` |

### 3.3 Multi Part Data Structure (24 Bytes)

| Offset | Parameter | Range/Description |
| :--- | :--- | :--- |
| 0 | Bank | Sound Bank (0-7, 127: Edit Buffer) |
| 1 | Program | Sound Program (0-127) |
| 2 | Volume | Part Volume (0-127) |
| 3 | Pan | Part Pan (0: L64, 64: Center, 127: R63) |
| 4 | Unk | Unknown (0) |
| 5 | Transpose | Part Transpose (Relative to 64) |
| 6 | Detune | Part Detune (Relative to 64) |
| 7 | Channel | MIDI Channel (0-15, 127: Omni/Global) |
| 8 | Low Key | Key Range Low (0-127) |
| 9 | High Key | Key Range High (0-127) |
| 10 | Low Vel | Velocity Range Low (1-127) |
| 11 | High Vel | Velocity Range High (1-127) |
| **12** | **Receive Mask** | Bit 0: MIDI, 1: USB, 2: Local, 6: Mute |
| **13** | **Control Mask** | Bit 0: Pitch, 1: Mod, 2: Press, 3: Sus, 4: Edits, 5: ProgChg |
| 14-23 | Tail | Tail Data: `[1, 63, 0, 0, 0, 0, 0, 0, 0, 0]` |

---

## 4. Global Operations

### 4.1 Fetch Globals (Request)

**Message**: `GLBR` (`0x04`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 04` |
| 5 | `0xF7` | End |

### 4.2 Push Globals (Dump)

**Message**: `GLBD` (`0x14`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 14` |
| 5-66 | `Data` | Global Parameters (typically 62 bytes) |
| 67 | `0x7F` | Checksum |
| 68 | `0xF7` | End |

**Notable Parameters (Offsets in Data block)**:
| Offset | Parameter | Description |
| :--- | :--- | :--- |
| 35 | Auto Edit | 0: Off, 1: On |
| 36 | Master Channel | 0-15 |
| 37 | Device ID | 0-126 (127 for Broadcast) |
| 38 | Popup Time | 0-127 |
| 39 | Contrast | 0-127 |
| 40 | Master Tune | 440Hz relative (376-504) |
| 41 | Transpose | Global Transpose (Relative to 64) |
| 44 | Ctrl Send | 0: Off, 1: MIDI, 2: USB, 3: MIDI+USB |
| 45 | Ctrl Receive | 0: Off, 1: On |
| 46 | Prog Change Send| 0: Off, 1: On |
| 48 | Clock | 0: Internal, 1: Auto, 2: Ext |
| 50 | Velocity Curve | 0-127 |
| 51-54 | Ctrl W, X, Y, Z | MIDI CC Numbers (0-127) |
| 55 | Master Volume | 0-127 |
| 56 | Category | Last selected category |
| 57 | Local Control | 0: Off, 1: On |
| 59 | Free Button | assignment |
| 60 | Pedal | 0: Sustain, 1: Sostenuto, 2: Soft |

### 4.3 Global Parameter Change

Updates a single global parameter using 14-bit encoding for both ID and Value.

**Message**: `GLBP` (`0x05`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 05` |
| 5 | `ID_MSB` | Global Param ID High Byte |
| 6 | `ID_LSB` | Global Param ID Low Byte |
| 7 | `Val_MSB` | Value High Byte |
| 8 | `Val_LSB` | Value Low Byte |
| 9 | `0xF7` | End |

---

## 5. Wavetable Operations

### 5.1 Push Wavetable (Dump)

A Wavetable Dump consists of **64 separate messages**, one for each wave in the table.

**Message**: `WTBD` (`0x12`)

| Byte | Value | Description |
| :--- | :--- | :--- |
| 0-4 | `Header` | `F0 3E 13 <DevID> 12` |
| 5 | `Slot` | Target Slot Number (80-118 for User Slots) |
| 6 | `Wave#` | Wave Number (0-63) |
| 7 | `Fmt` | Format (Always 0) |
| 8-391 | `Data` | **384 bytes** of encoded sample data (128 samples × 3 bytes) |
| 392-405| `Name` | Wavetable Name (14 Characters). |
| 406-407| `Res` | Reserved (0, 0) |
| 408 | `0x7F` | Checksum |
| 409 | `0xF7` | End |

### 5.2 Sample Encoding (21-bit Signed)

Each sample is a signed 21-bit integer. It is transmitted as 3 MIDI data bytes (7-bit).

**Encoding Algorithm**:
1.  Input: `value` (Signed Integer).
2.  If `value < 0`: `value += 2097152` (`2^21`). (Two's complement for 21-bit).
3.  **Byte 1 (MSB)**: `(value >> 14) & 0x7F`
4.  **Byte 2 (Mid)**: `(value >> 7) & 0x7F`
5.  **Byte 3 (LSB)**: `value & 0x7F`

---

## 6. Device Operations

### 6.1 Identity Request

Standard MIDI Identity Request.

**Message**:
`F0 7E 7F 06 01 F7`

### 6.2 Identity Reply

**Message**:
`F0 7E <DevID> 06 02 <ManID> <FamID> <ModelID> <Ver> ... F7`

*   **ManID**: `0x3E` (Waldorf)
*   **FamID/ModelID**: `0x13` (Blofeld)
*   **Ver**: Firmware Version string.

---

## 7. Checksum Strategy

The Blofeld uses a summation-based checksum: `sum(payload_bytes) & 0x7F`.

*   **Legacy Behavior**: Analysis of the Bigglesworth codebase reveals that the legacy driver **never calculates** real checksums. Instead, it uses a constant placeholder `0x7F` for all outgoing messages.
*   **Resilience**: The Blofeld hardware is designed to accept `0x7F` as a valid "wildcard" checksum for all incoming SysEx messages. On receipt, the device calculates the real checksum but considers `0x7F` a pass to ensure compatibility with simpler controllers.
*   **Payload Definition**: 
    - For `SNDD`, the payload is the 385 parameter bytes.
    - For `MULD`, it is the 418 metadata and part bytes.
    - For `GLBD`, it is the 62 global data bytes.
*   **Exceptions**: `SNDP`, `GLBP`, `MULR`, and `GLBR` messages **do not** include a checksum byte.

---

## 8. Control Change (CC) Mapping

The Blofeld supports real-time parameter control via standard MIDI Control Change messages. The following table maps CC numbers to internal Sound Parameter IDs.

| CC | Param ID | Parameter Name |
| :--- | :--- | :--- |
| 5 | 57 | Glide Rate |
| 12 | 316 | Arp Octave |
| 13 | 323 | Arp Pattern Length |
| 14 | 311 | Arp Mode |
| 15 | 160 | LFO 1 Shape |
| 16 | 161 | LFO 1 Speed |
| 17 | 163 | LFO 1 Sync |
| 18 | 166 | LFO 1 Delay |
| 19 | 172 | LFO 2 Shape |
| 20 | 173 | LFO 2 Speed |
| 21 | 175 | LFO 2 Sync |
| 22 | 178 | LFO 2 Delay |
| 23 | 184 | LFO 3 Shape |
| 24 | 185 | LFO 3 Speed |
| 25 | 187 | LFO 3 Sync |
| 26 | 190 | LFO 3 Delay |
| 27 | 1 | Osc 1 Octave |
| 28 | 2 | Osc 1 Semitone |
| 29 | 3 | Osc 1 Detune |
| 30 | 7 | Osc 1 FM Amount |
| 31 | 8 | Osc 1 Shape |
| 33 | 9 | Osc 1 Pulsewidth |
| 34 | 11 | Osc 1 PWM Amount |
| 35 | 17 | Osc 2 Octave |
| 36 | 18 | Osc 2 Semitone |
| 37 | 19 | Osc 2 Detune |
| 38 | 23 | Osc 2 FM Amount |
| 39 | 24 | Osc 2 Shape |
| 40 | 25 | Osc 2 Pulsewidth |
| 41 | 27 | Osc 2 PWM Amount |
| 42 | 33 | Osc 3 Octave |
| 43 | 34 | Osc 3 Semitone |
| 44 | 35 | Osc 3 Detune |
| 45 | 39 | Osc 3 FM Amount |
| 46 | 40 | Osc 3 Shape |
| 47 | 41 | Osc 3 Pulsewidth |
| 48 | 43 | Osc 3 PWM Amount |
| 49 | 49 | Sync (Osc 2 to O3) |
| 50 | 51 | Pitch Mod Amount |
| 51 | 56 | Glide Mode |
| 52 | 61 | Mixer Osc 1 Level |
| 53 | 62 | Mixer Osc 1 Balance |
| 54 | 71 | Mixer Ringmod Level |
| 55 | 72 | Mixer Ringmod Balance |
| 56 | 63 | Mixer Osc 2 Level |
| 57 | 64 | Mixer Osc 2 Balance |
| 58 | 65 | Mixer Osc 3 Level |
| 59 | 66 | Mixer Osc 3 Balance |
| 60 | 67 | Mixer Noise Level |
| 61 | 68 | Mixer Noise Balance |
| 62 | 69 | Mixer Noise Colour |
| 65 | 53 | Glide Active |
| 67 | 117 | Filter Routing |
| 68 | 77 | Filter 1 Type |
| 69 | 78 | Filter 1 Cutoff |
| 70 | 80 | Filter 1 Resonance |
| 71 | 81 | Filter 1 Drive |
| 72 | 86 | Filter 1 Keytrack |
| 73 | 87 | Filter 1 Env Amount |
| 74 | 88 | Filter 1 Env Velocity |
| 75 | 90 | Filter 1 Mod Amount |
| 76 | 92 | Filter 1 FM Amount |
| 77 | 93 | Filter 1 Pan |
| 78 | 95 | Filter 1 Pan Amount |
| 79 | 97 | Filter 2 Type |
| 80 | 98 | Filter 2 Cutoff |
| 81 | 100 | Filter 2 Resonance |
| 82 | 101 | Filter 2 Drive |
| 83 | 106 | Filter 2 Keytrack |
| 84 | 107 | Filter 2 Env Amount |
| 85 | 108 | Filter 2 Env Velocity |
| 86 | 110 | Filter 2 Mod Amount |
| 87 | 112 | Filter 2 FM Amount |
| 88 | 113 | Filter 2 Pan |
| 89 | 115 | Filter 2 Pan Amount |
| 90 | 121 | Amplifier Volume |
| 91 | 122 | Amplifier Velocity |
| 92 | 124 | Amplifier Mod Amount |
| 93 | 129 | Effect 1 Mix |
| 94 | 145 | Effect 2 Mix |
| 95 | 199 | Filter Env Attack |
| 96 | 201 | Filter Env Decay |
| 97 | 202 | Filter Env Sustain |
| 98 | 203 | Filter Env Decay 2 |
| 99 | 204 | Filter Env Sustain 2 |
| 100 | 205 | Filter Env Release |
| 101 | 211 | Amp Env Attack |
| 102 | 213 | Amp Env Decay |
| 103 | 214 | Amp Env Sustain |
| 104 | 215 | Amp Env Decay 2 |
| 105 | 216 | Amp Env Sustain 2 |
| 106 | 217 | Amp Env Release |
| 107 | 223 | Env 3 Attack |
| 108 | 225 | Env 3 Decay |
| 109 | 226 | Env 3 Sustain |
| 110 | 227 | Env 3 Decay 2 |
| 111 | 228 | Env 3 Sustain 2 |
| 112 | 229 | Env 3 Release |
| 113 | 235 | Env 4 Attack |
| 114 | 237 | Env 4 Decay |
| 115 | 238 | Env 4 Sustain |
| 116 | 239 | Env 4 Decay 2 |
| 117 | 240 | Env 4 Sustain 2 |
| 118 | 241 | Env 4 Release |

---

## 9. Practical Examples (How-To)

These examples use the Broadcast Device ID (`0x7F`). Replace with a specific ID (e.g., `0x00`) if addressing a specific unit.

### 9.1 Fetch Sound A001 (Request)
To request the first sound in Bank A:
- **Bank**: 0, **Program**: 0
- **Message**: `F0 3E 13 7F 00 00 00 7F F7`

### 9.2 Fetch Sound H128 (Request)
To request the last sound in Bank H:
- **Bank**: 7, **Program**: 127 (`0x7F`)
- **Message**: `F0 3E 13 7F 00 07 7F 7F F7`

### 9.3 Fetch Current Sound Edit Buffer
Useful for synchronizing the UI with the hardware's current state:
- **Bank**: 127 (`0x7F`), **Program**: 0
- **Message**: `F0 3E 13 7F 00 7F 00 7F F7`

### 9.4 Change Filter 1 Cutoff (Real-time)
To set Filter 1 Cutoff (ID 78) to 100 in the Sound Edit Buffer:
- **Location**: 0, **Param ID**: 78 (`0x00 0x4E`), **Value**: 100 (`0x64`)
- **Message**: `F0 3E 13 7F 20 00 00 4E 64 F7`

### 9.5 Change Arp Mode in Multi Part 3
To set Arpeggiator Mode (ID 311) to "Hold" (Value 3) for Part 3:
- **Location**: 3, **Param ID**: 311 (`0x02 0x37`), **Value**: 3
- **Message**: `F0 3E 13 7F 20 03 02 37 03 F7`

### 9.6 Change Global Master Volume
To set Global Master Volume (Global ID 55) to 127:
- **ID**: 55 (`0x00 0x37`), **Value**: 127 (`0x00 0x7F`)
- **Message**: `F0 3E 13 7F 05 00 37 00 7F F7`

### 9.7 Push Sound Data to Edit Buffer
To overwrite the current sound in the hardware with a local structure:
- **Message**: `F0 3E 13 7F 10 7F 00 [385 bytes of data] 7F F7`

### 9.8 Request Bulk Dump of All Sounds
- **Bank**: 64 (`0x40`), **Program**: 0
- **Message**: `F0 3E 13 7F 00 40 00 7F F7`

---

## 10. Implementation Guidelines

These guidelines are based on long-term analysis of legacy drivers and hardware behavior.

### 10.1 Timing and Performance

*   **Request Interleaving**: Maintain a minimum delay of **150ms** between sequential SysEx requests. Rapid requests can overflow the hardware's MIDI buffer.
*   **Bulk Dump Benchmarks**: A full dump of 1024 sounds via the `0x40` bank takes approximately **3.5 minutes**.
*   **Response Latency**: The hardware typically responds to a request within **150ms**.

### 10.2 Implementation Safeguards

*   **Forward Blocking (Echo Prevention)**: Software must implement a mechanism to block MIDI events originating from the hardware from being echoed back to it. This prevents infinite MIDI feedback loops during real-time editing.
*   **Channel Filtering**: In setups with multiple MIDI devices, ensure the software filters incoming traffic by the configured **Master Channel** or monitored Part channels.
*   **SysEx Fragmentation**: Implement a robust buffering strategy to handle fragmented SysEx messages. Do not assume a full message will arrive in a single MIDI packet.

### 10.3 Connection Management

*   **Liveness Check**: Use the Standard Identity Request (`F0 7E 7F 06 01 F7`) as a periodic "heartbeat" to verify the device is still connected and responsive.
*   **Timeout Threshold**: Use a **1-second timeout** for individual request-response cycles before reporting a connection failure.

---

## 11. Implementation TODO (Analysis Findings)

The following discrepancies between the current Java implementation and this specification have been identified and need to be addressed:

### 11.1 Message & Protocol Corrections
- [ ] **Standardize Sound Dump Length**: Align `SysExParser` and `SysExGenerator` with the 394-byte `SNDD` format (currently 392 bytes).
- [ ] **SNDR Checksum**: Add the missing checksum byte to `encodeSoundDumpRequest` in `BlofeldProtocol`.
- [ ] **GLBP Decoding**: Implement `CMD_GLOBAL_PARAMETER` (0x05) decoding in `BlofeldProtocol.decode`.
- [ ] **Wavetable Dumps**: Refactor `WavetableDump` to handle the 64-message sequence and correct slot range (80-118).

### 11.2 Sound Parameter Alignment
- [ ] **Remove +2 Padding**: Remove the manual padding in `SysExParser.parseSoundDump` to align with the 385-byte specification.
- [ ] **Correct Name Offset**: Shift Sound Name indices from 365-380 to **363-378**.
- [ ] **Correct Category Offset**: Shift Sound Category index from 381 to **379**.
- [ ] **Correct Envelope 3 Offset**: Change Envelope 3 base offset from 223 to **220**.
- [ ] **Correct Envelope 4 Offset**: Change Envelope 4 base offset from 235 to **232**.

### 11.3 Miscellaneous
- [ ] **Global Dump Length**: Fix array allocation in `encodeGlobalParametersData` (currently allocates 1 byte too many).