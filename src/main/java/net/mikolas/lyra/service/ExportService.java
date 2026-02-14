package net.mikolas.lyra.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import net.mikolas.lyra.midi.SysExGenerator;
import net.mikolas.lyra.model.Sound;

/**
 * Service for exporting sounds to MIDI (.mid) and SysEx (.syx) files.
 */
public class ExportService {

  /**
   * Export a list of sounds to a raw SysEx file.
   *
   * @param sounds sounds to export
   * @param file target file
   * @throws IOException if writing fails
   */
  public void exportToSyx(List<Sound> sounds, File file) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      for (Sound sound : sounds) {
        byte[] sysex = SysExGenerator.generateSoundDump(sound);
        fos.write(sysex);
      }
    }
  }

  /**
   * Export a list of sounds to a Standard MIDI File (.mid).
   *
   * @param sounds sounds to export
   * @param file target file
   * @throws IOException if writing fails
   * @throws InvalidMidiDataException if MIDI data is invalid
   */
  public void exportToMid(List<Sound> sounds, File file)
      throws IOException, InvalidMidiDataException {
    // Type 0 sequence (single track)
    Sequence sequence = new Sequence(Sequence.PPQ, 480);
    Track track = sequence.createTrack();

    long tick = 0;
    for (Sound sound : sounds) {
      byte[] sysex = SysExGenerator.generateSoundDump(sound);
      SysexMessage msg = new SysexMessage();
      // SysexMessage.setMessage(byte[] data, int length) 
      // Note: javax.sound.midi SysexMessage expects the data WITHOUT the leading F0
      // because it is added automatically, OR it expects the whole thing.
      // Actually, setMessage(byte[] data, int length) takes the WHOLE message including F0 and F7.
      msg.setMessage(sysex, sysex.length);
      track.add(new MidiEvent(msg, tick));
      
      // Add a small delay between messages (e.g. 1 tick) to be safe
      tick += 1;
    }

    MidiSystem.write(sequence, 0, file);
  }
}
