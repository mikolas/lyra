package net.mikolas.lyra.service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import net.mikolas.lyra.db.WavetableRepository;
import net.mikolas.lyra.midi.BlofeldProtocol;
import net.mikolas.lyra.midi.MidiFileReader;
import net.mikolas.lyra.midi.WavetableDump;
import net.mikolas.lyra.model.Keyframe;
import net.mikolas.lyra.model.Wavetable;

/**
 * Service for importing wavetables from MIDI or SysEx files.
 * Handles grouping 64-wave messages and re-indexing them to avoid collisions.
 */
public class WavetableImportService {

    private final WavetableRepository repository;
    private final BlofeldProtocol protocol = new BlofeldProtocol();

    public WavetableImportService(WavetableRepository repository) {
        this.repository = repository;
    }

    /**
     * Import wavetables from a MIDI or .syx file.
     * 
     * @param file The file to import from
     * @return Import result with counts and names
     */
    public ImportResult importFromFile(File file) {
        try {
            List<byte[]> messages = MidiFileReader.readSysExMessages(file);
            List<WavetableDump> dumps = new ArrayList<>();
            
            for (byte[] data : messages) {
                try {
                    Object decoded = protocol.decode(data);
                    if (decoded instanceof WavetableDump wtDump) {
                        dumps.add(wtDump);
                    }
                } catch (Exception e) {
                    // Ignore non-wavetable messages or decoding errors
                }
            }
            
            if (dumps.isEmpty()) {
                return new ImportResult(0, 0, Collections.emptyList(), "No wavetable messages found.");
            }

            // Group by name + original slot to distinguish between different tables in one file
            Map<String, List<WavetableDump>> groups = dumps.stream()
                .collect(Collectors.groupingBy(d -> d.name() + "_slot_" + d.slot()));
            
            int importedCount = 0;
            List<String> importedNames = new ArrayList<>();
            
            for (List<WavetableDump> group : groups.values()) {
                if (group.size() < 64) {
                    System.err.println("Skipping incomplete wavetable '" + group.get(0).name() + "' - only " + group.size() + " waves found.");
                    continue;
                }
                
                Wavetable wt = reconstructWavetable(group);
                
                // Refinement: Slot Staggering
                Integer nextSlot = repository.findNextAvailableSlot();
                if (nextSlot != null) {
                    wt.setSlot(nextSlot);
                } else {
                    wt.setSlot(null); // Virtual Library / Unassigned
                }
                
                repository.save(wt);
                importedCount++;
                importedNames.add(wt.getName());
            }
            
            return new ImportResult(groups.size(), importedCount, importedNames, null);
            
        } catch (Exception e) {
            return new ImportResult(0, 0, Collections.emptyList(), "Error: " + e.getMessage());
        }
    }

    private Wavetable reconstructWavetable(List<WavetableDump> waves) {
        // Sort waves by their number (0-63)
        waves.sort(Comparator.comparingInt(WavetableDump::waveNumber));
        
        Wavetable wt = new Wavetable();
        wt.setName(waves.get(0).name());
        wt.setFactory(false);
        
        // Create keyframes for all 64 waves to ensure the full table is preserved and editable
        for (WavetableDump wave : waves) {
            if (wave.waveNumber() >= 0 && wave.waveNumber() < 64) {
                Keyframe kf = new Keyframe(wave.waveNumber());
                System.arraycopy(wave.samples(), 0, kf.getSamples(), 0, 128);
                wt.getKeyframes().add(kf);
                
                // Also populate the bounced buffer for immediate use
                System.arraycopy(wave.samples(), 0, wt.getBouncedWaves()[wave.waveNumber()], 0, 128);
            }
        }
        
        // Ensure binaryData is populated for DB persistence
        wt.prepareForSave();
        
        return wt;
    }

    public record ImportResult(int totalFound, int imported, List<String> names, String error) {
        public String getSummary() {
            if (error != null) return error;
            return String.format("Successfully imported %d wavetables: %s", imported, String.join(", ", names));
        }
    }
}
