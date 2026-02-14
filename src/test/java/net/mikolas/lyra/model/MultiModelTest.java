package net.mikolas.lyra.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiModelTest {

    @Test
    public void testMultiPatchInitialization() {
        MultiPatch multi = new MultiPatch();
        multi.getParts();
        multi.refreshProperties();
        assertNotNull(multi.getParts());
        assertEquals(16, multi.getParts().size());
        assertEquals("Init Multi", multi.nameProperty().get());
        assertEquals(127, multi.volumeProperty().get());
    }

    @Test
    public void testMultiPartBitmaskDecoding() {
        MultiPatch multi = new MultiPatch();
        multi.getParts();
        MultiPart part = multi.getParts().get(0);
        
        // Initial state from initDefaultData: 0x07 (MIDI, USB, Local on)
        assertTrue(part.getMidiInProperty().get());
        assertTrue(part.getUsbInProperty().get());
        assertTrue(part.getLocalInProperty().get());
        assertFalse(part.getMuteProperty().get());
        
        // Change via property
        part.getMuteProperty().set(true);
        byte[] data = multi.getData();
        int absOffset = 34 + 12; // Part 0, byte 12
        assertEquals(0x47, data[absOffset] & 0xFF);
        
        // Change via data array
        data[absOffset] = 0x00; // All off
        part.updateFromData();
        assertFalse(part.getMidiInProperty().get());
        assertFalse(part.getMuteProperty().get());
    }

    @Test
    public void testMultiPartControlBitmask() {
        MultiPatch multi = new MultiPatch();
        multi.getParts();
        MultiPart part = multi.getParts().get(0);
        
        // Initial state from initDefaultData: 0x3F (All on)
        assertTrue(part.getPitchInProperty().get());
        assertTrue(part.getPrgInProperty().get());
        
        part.getPitchInProperty().set(false);
        int absOffset = 34 + 13; // Part 0, byte 13
        assertEquals(0x3E, multi.getData()[absOffset] & 0xFF);
    }

    @Test
    public void testPartParameterMapping() {
        MultiPatch multi = new MultiPatch();
        multi.getParts();
        MultiPart part = multi.getParts().get(0);
        
        part.getVolumeProperty().set(88);
        assertEquals((byte)88, multi.getData()[34 + 2]);
        
        part.getTransposeProperty().set(64);
        assertEquals((byte)64, multi.getData()[34 + 5]);
    }

    @Test
    public void testUpdateFromMidi() {
        MultiPatch multi = new MultiPatch();
        byte[] data = new byte[418];
        
        // Set some distinct values
        data[2] = 'T'; // Name start
        data[19] = 100; // Global Volume
        data[34 + 1] = 55; // Part 0 Program
        
        multi.updateFromMidi(data);
        
        assertEquals("T", multi.nameProperty().get().substring(0, 1));
        assertEquals(100, multi.volumeProperty().get());
        assertEquals(55, multi.getParts().get(0).getProgramProperty().get());
        assertFalse(multi.isEdited());
    }
}
