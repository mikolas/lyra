package net.mikolas.lyra.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

/**
 * MIDI file reader for extracting SysEx messages.
 *
 * <p>Reads .mid files and extracts all SysEx messages, typically containing sound dumps.
 */
public class MidiFileReader {

  /**
   * Read all SysEx messages from a MIDI file.
   *
   * @param file MIDI file (.mid or .syx)
   * @return list of SysEx message byte arrays
   * @throws IOException if file cannot be read
   * @throws InvalidMidiDataException if file is not valid MIDI
   */
  public static List<byte[]> readSysExMessages(File file)
      throws IOException, InvalidMidiDataException {
    if (file.getName().toLowerCase().endsWith(".syx")) {
      return readRawSysEx(file);
    }

    List<byte[]> sysexMessages = new ArrayList<>();
    Sequence sequence = MidiSystem.getSequence(file);

    for (Track track : sequence.getTracks()) {
      for (int i = 0; i < track.size(); i++) {
        MidiMessage message = track.get(i).getMessage();
        if (message instanceof SysexMessage sysex) {
          // getMessage() returns the full SysEx including F0
          sysexMessages.add(sysex.getMessage());
        }
      }
    }

    return sysexMessages;
  }

  private static List<byte[]> readRawSysEx(File file) throws IOException {
    List<byte[]> messages = new ArrayList<>();
    byte[] allBytes = java.nio.file.Files.readAllBytes(file.toPath());
    
    int start = -1;
    for (int i = 0; i < allBytes.length; i++) {
      if ((allBytes[i] & 0xFF) == 0xF0) {
        start = i;
      } else if ((allBytes[i] & 0xFF) == 0xF7 && start != -1) {
        int length = i - start + 1;
        byte[] msg = new byte[length];
        System.arraycopy(allBytes, start, msg, 0, length);
        messages.add(msg);
        start = -1;
      }
    }
    return messages;
  }
}
