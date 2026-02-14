package net.mikolas.lyra.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArpeggiatorBindingTest {

  @Test
  void testArpModeBinding() {
    // Create a sound with arp mode = 1 (on)
    byte[] params = new byte[385];
    params[311] = 1; // Arp mode = on
    
    Sound sound = Sound.builder()
        .name("Test")
        .parameters(params)
        .build();
    
    Arpeggiator arp = sound.getArpeggiator();
    
    // Check if mode property reads correctly
    assertEquals(1, arp.modeProperty().get(), "Arp mode should be 1 (on)");
  }

  @Test
  void testArpModeOff() {
    byte[] params = new byte[385];
    params[311] = 0; // Arp mode = off
    
    Sound sound = Sound.builder()
        .name("Test")
        .parameters(params)
        .build();
    
    assertEquals(0, sound.getArpeggiator().modeProperty().get());
  }

  @Test
  void testArpModeHold() {
    byte[] params = new byte[385];
    params[311] = 3; // Arp mode = hold
    
    Sound sound = Sound.builder()
        .name("Test")
        .parameters(params)
        .build();
    
    assertEquals(3, sound.getArpeggiator().modeProperty().get());
  }
}
