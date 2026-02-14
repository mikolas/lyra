package net.mikolas.lyra.service;

import net.mikolas.lyra.model.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundSyncServiceTest {

  private SoundSyncService service;

  @BeforeEach
  void setUp() {
    service = new SoundSyncService();
  }

  @Test
  void testShouldSyncSound() {
    Sound currentSound = Sound.builder().bank(0).program(5).build();
    Sound incomingSound = Sound.builder().bank(0).program(5).build();
    
    assertTrue(service.shouldSyncSound(currentSound, incomingSound));
  }

  @Test
  void testShouldNotSyncDifferentBank() {
    Sound currentSound = Sound.builder().bank(0).program(5).build();
    Sound incomingSound = Sound.builder().bank(1).program(5).build();
    
    assertFalse(service.shouldSyncSound(currentSound, incomingSound));
  }

  @Test
  void testShouldNotSyncDifferentProgram() {
    Sound currentSound = Sound.builder().bank(0).program(5).build();
    Sound incomingSound = Sound.builder().bank(0).program(6).build();
    
    assertFalse(service.shouldSyncSound(currentSound, incomingSound));
  }

  @Test
  void testShouldNotSyncNullBank() {
    Sound currentSound = Sound.builder().bank(null).program(5).build();
    Sound incomingSound = Sound.builder().bank(0).program(5).build();
    
    assertFalse(service.shouldSyncSound(currentSound, incomingSound));
  }

  @Test
  void testShouldNotSyncNullProgram() {
    Sound currentSound = Sound.builder().bank(0).program(null).build();
    Sound incomingSound = Sound.builder().bank(0).program(5).build();
    
    assertFalse(service.shouldSyncSound(currentSound, incomingSound));
  }

  @Test
  void testShouldNotSyncNullCurrentSound() {
    Sound incomingSound = Sound.builder().bank(0).program(5).build();
    
    assertFalse(service.shouldSyncSound(null, incomingSound));
  }

  @Test
  void testShouldNotSyncNullIncomingSound() {
    Sound currentSound = Sound.builder().bank(0).program(5).build();
    
    assertFalse(service.shouldSyncSound(currentSound, null));
  }
}
