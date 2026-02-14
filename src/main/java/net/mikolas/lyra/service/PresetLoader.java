package net.mikolas.lyra.service;

import java.io.InputStream;
import java.util.List;
import net.mikolas.lyra.db.WavetableRepository;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.Wavetable;

/**
 * Loads factory wavetables from the extracted .bin files in resources.
 */
public class PresetLoader {

    public static void loadFactoryPresets(WavetableRepository repo) {
        // Only load if factory presets don't exist
        boolean alreadyLoaded = repo.getAllWavetables().stream().anyMatch(Wavetable::isFactory);
        if (alreadyLoaded) return;

        List<String> oscShapes = ParameterValues.getValues(ParameterValueType.OSC_SHAPES);
        if (oscShapes == null) {
            System.err.println("Could not load oscShapes for wavetable initialization.");
            return;
        }

        System.out.println("Initializing factory wavetables from binary resources...");
        int loadedCount = 0;

        // Factory wavetables are indices 1 to 72 in oscShapes
        for (int i = 1; i <= 72; i++) {
            String name = oscShapes.get(i);
            // Sanitize name for filename: replace non-alphanumeric (except - and .) with _
            String sanitizedName = name.replaceAll("[^a-zA-Z0-9\\-.]", "_");
            String fileName = String.format("/presets/wavetables/%03d_%s.bin", i, sanitizedName);
            
            try (InputStream is = PresetLoader.class.getResourceAsStream(fileName)) {
                if (is == null) {
                    System.err.println("Factory wavetable resource not found: " + fileName);
                    continue;
                }

                byte[] data = is.readAllBytes();
                if (data.length != 32768) {
                    System.err.println("Invalid data size for " + fileName + ": " + data.length);
                    continue;
                }

                Wavetable wt = new Wavetable();
                wt.setName(name);
                wt.setSlot(i); // Internal slot matches oscShape index for factory
                wt.setFactory(true);
                wt.setBinaryData(data);
                
                repo.save(wt);
                loadedCount++;
            } catch (Exception e) {
                System.err.println("Failed to load wavetable " + name + " (" + fileName + "): " + e.getMessage());
            }
        }
        System.out.println("Successfully loaded " + loadedCount + " factory wavetables into database.");
    }
}
