package net.mikolas.lyra.midi;

import static org.junit.jupiter.api.Assertions.*;
import net.mikolas.lyra.model.MultiPatch;
import org.junit.jupiter.api.Test;

class MultiSysExTest {

    @Test
    void testParseMultiDump425() {
        byte[] payload = new byte[418];
        payload[0] = 0; // Bank
        payload[1] = 42; // Program 43
        
        byte[] sysex = new byte[425];
        sysex[0] = (byte)0xF0;
        sysex[1] = 0x3E;
        sysex[2] = 0x13;
        sysex[3] = 0x7F;
        sysex[4] = 0x11; // MULD
        System.arraycopy(payload, 0, sysex, 5, 418);
        sysex[423] = 0x00; // Checksum placeholder
        sysex[424] = (byte)0xF7;

        MultiPatch parsed = SysExParser.parseMultiDump(sysex);

        assertNotNull(parsed, "Parser should accept 425-byte message");
        assertEquals(42, parsed.getData()[1] & 0xFF, "Program index should match");
    }
}
