package net.mikolas.lyra.service;

import net.mikolas.lyra.model.Sound;

/**
 * Service for bidirectional MIDI sound synchronization logic.
 */
public class SoundSyncService {

  /**
   * Determine if incoming sound should sync with current sound.
   * Sounds match if they have the same bank and program.
   *
   * @param currentSound currently edited sound
   * @param incomingSound incoming sound from MIDI
   * @return true if sounds should sync
   */
  public boolean shouldSyncSound(Sound currentSound, Sound incomingSound) {
    if (currentSound == null || incomingSound == null) {
      return false;
    }
    
    Integer currentBank = currentSound.getBank();
    Integer currentProgram = currentSound.getProgram();
    Integer incomingBank = incomingSound.getBank();
    Integer incomingProgram = incomingSound.getProgram();
    
    if (currentBank == null || currentProgram == null) {
      return false;
    }
    
    return currentBank.equals(incomingBank) && currentProgram.equals(incomingProgram);
  }
}
