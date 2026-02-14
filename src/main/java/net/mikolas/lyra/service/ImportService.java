package net.mikolas.lyra.service;

import com.j256.ormlite.misc.TransactionManager;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sound.midi.InvalidMidiDataException;
import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.midi.MidiFileReader;
import net.mikolas.lyra.midi.SysExParser;
import net.mikolas.lyra.model.Sound;

/**
 * Service for importing sounds from MIDI files.
 *
 * <p>Reads .mid/.syx files, extracts sound dumps, and saves to database.
 */
public class ImportService {

  private final Database database;

  public ImportService(Database database) {
    this.database = database;
  }

  /**
   * Import sounds from a MIDI file.
   *
   * @param file MIDI file containing sound dumps
   * @return import result with counts
   * @throws IOException if file cannot be read
   * @throws InvalidMidiDataException if file is not valid MIDI
   */
  public ImportResult importFromFile(File file) throws IOException, InvalidMidiDataException {
    List<byte[]> sysexMessages = MidiFileReader.readSysExMessages(file);

    int total = 0;
    int imported = 0;
    int skipped = 0;
    List<String> errors = new ArrayList<>();

    for (byte[] sysex : sysexMessages) {
      total++;
      final Sound sound = SysExParser.parseSoundDump(sysex);

      if (sound == null) {
        skipped++;
        errors.add("Invalid SysEx message at index " + total);
        continue;
      }

      try {
        TransactionManager.callInTransaction(database.sounds.getConnectionSource(), 
            (Callable<Void>) () -> {
              if (sound.getBank() != null && sound.getProgram() != null) {
                Sound existing = database.sounds.queryBuilder()
                    .where()
                    .eq("bank", sound.getBank())
                    .and()
                    .eq("program", sound.getProgram())
                    .queryForFirst();
                
                if (existing != null) {
                  database.sounds.delete(existing);
                }
              }
              database.sounds.create(sound);
              return null;
            });
        imported++;
      } catch (SQLException e) {
        skipped++;
        errors.add("Failed to save sound: " + sound.getName() + " - " + e.getMessage());
      }
    }

    return new ImportResult(total, imported, skipped, errors);
  }

  /** Result of an import operation. */
  public record ImportResult(int total, int imported, int skipped, List<String> errors) {
    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public String getSummary() {
      return String.format(
          "Imported %d of %d sounds (%d skipped)", imported, total, skipped);
    }
  }
}
