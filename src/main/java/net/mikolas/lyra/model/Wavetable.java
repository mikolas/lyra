package net.mikolas.lyra.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Manages a Waldorf Blofeld wavetable consisting of 64 waves.
 * Uses a sparse keyframe system for interpolation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "wavetables")
public class Wavetable {
    @DatabaseField(generatedId = true)
    private Integer id;

    @Builder.Default
    @DatabaseField(canBeNull = false)
    private String name = "New Wavetable";

    @Builder.Default
    @DatabaseField
    private Integer slot = 80;

    @Builder.Default
    @DatabaseField
    private boolean isFactory = false;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] binaryData;

    private final ObservableList<Keyframe> keyframes = FXCollections.observableArrayList();
    
    // Transient JavaFX properties for UI binding
    private transient StringProperty nameProperty;
    private transient IntegerProperty slotProperty;

    /**
     * Get or create the name property for JavaFX binding.
     */
    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
            nameProperty.addListener((_, _, newVal) -> name = newVal);
        }
        return nameProperty;
    }

    /**
     * Get or create the slot property for JavaFX binding.
     */
    public IntegerProperty slotProperty() {
        if (slotProperty == null) {
            slotProperty = new SimpleIntegerProperty(slot == null ? 80 : slot);
            slotProperty.addListener((_, _, newVal) -> slot = newVal.intValue());
        }
        return slotProperty;
    }

    public void prepareForSave() {
        bounce(); // Ensure bouncedWaves is up to date
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
            
            // Write 64 waves × 128 samples as raw integers
            for (int i = 0; i < 64; i++) {
                for (int s = 0; s < 128; s++) {
                    dos.writeInt(bouncedWaves[i][s]);
                }
            }
            dos.flush();
            binaryData = baos.toByteArray();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromData() {
        if (binaryData == null || binaryData.length == 0) return;
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(binaryData);
            java.io.DataInputStream dis = new java.io.DataInputStream(bais);
            
            // Read 64 waves × 128 samples as raw integers
            for (int i = 0; i < 64; i++) {
                for (int s = 0; s < 128; s++) {
                    bouncedWaves[i][s] = dis.readInt();
                }
            }
            needsRebounce = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cached bounced waves (64 waves * 128 samples)
    private final int[][] bouncedWaves = new int[64][128];
    @Builder.Default
    private transient boolean needsRebounce = true;

    // Custom constructor for default initialization since Lombok's Builder/NoArgs might conflict with FX initialization
    public static Wavetable createNew() {
        Wavetable wt = new Wavetable();
        wt.keyframes.add(new Keyframe(0));
        return wt;
    }

    /**
     * Calculates all 64 waves based on keyframes and their transform modes.
     */
    public void bounce() {
        if (!needsRebounce) return;
        
        keyframes.sort(Comparator.comparingInt(Keyframe::getIndex));
        
        for (int i = 0; i < keyframes.size(); i++) {
            Keyframe current = keyframes.get(i);
            int startIdx = current.getIndex();
            
            // Fill current keyframe data
            System.arraycopy(current.getSamples(), 0, bouncedWaves[startIdx], 0, 128);
            
            if (i < keyframes.size() - 1) {
                Keyframe next = keyframes.get(i + 1);
                int endIdx = next.getIndex();
                interpolate(current, next, startIdx, endIdx);
            } else {
                // Fill remaining waves with last keyframe (Constant)
                for (int j = startIdx + 1; j < 64; j++) {
                    System.arraycopy(current.getSamples(), 0, bouncedWaves[j], 0, 128);
                }
            }
        }
        needsRebounce = false;
    }

    private void interpolate(Keyframe a, Keyframe b, int start, int end) {
        int range = end - start;
        for (int i = start + 1; i < end; i++) {
            double ratio = (double)(i - start) / range;
            
            switch (a.getTransformMode()) {
                case CONSTANT -> System.arraycopy(a.getSamples(), 0, bouncedWaves[i], 0, 128);
                case CURVE -> {
                    double p = applyEasing(a.getCurveFunction(), ratio);
                    for (int s = 0; s < 128; s++) {
                        bouncedWaves[i][s] = (int)((1 - p) * a.getSamples()[s] + p * b.getSamples()[s]);
                    }
                }
                case TRANSLATE -> {
                    int offset = (int)(a.getTranslateOffset() * ratio);
                    // Circular roll implementation
                    for (int s = 0; s < 128; s++) {
                        int sourceIdx = (s - offset) % 128;
                        if (sourceIdx < 0) sourceIdx += 128;
                        // Linear crossfade after roll
                        bouncedWaves[i][s] = (int)((1 - ratio) * a.getSamples()[s] + ratio * b.getSamples()[sourceIdx]);
                    }
                }
                case SPECTRAL -> {
                    // Additive Synthesis: Base wave + harmonics
                    // Linear interpolate base wave
                    for (int s = 0; s < 128; s++) {
                        bouncedWaves[i][s] = (int)((1 - ratio) * a.getSamples()[s] + ratio * b.getSamples()[s]);
                    }
                    
                    // Add harmonics from source keyframe
                    for (Harmonic h : a.getHarmonics()) {
                        int[] harmonicWave = h.generateWave();
                        for (int s = 0; s < 128; s++) {
                            // Scale harmonic by ratio (fade out as we approach next keyframe)
                            int harmonicValue = (int)(harmonicWave[s] * (1 - ratio));
                            bouncedWaves[i][s] = Math.clamp(
                                bouncedWaves[i][s] + harmonicValue,
                                -1048576, 1048575
                            );
                        }
                    }
                }
            }
        }
    }

    private double applyEasing(String function, double t) {
        return switch (function) {
            case "InQuad" -> t * t;
            case "OutQuad" -> t * (2 - t);
            case "InOutQuad" -> t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
            case "Sine" -> (1 - Math.cos(t * Math.PI)) / 2;
            case "Exp" -> Math.pow(2, 10 * (t - 1));
            default -> t; // Linear
        };
    }

    public int[] getWave(int index) {
        if (needsRebounce) bounce();
        return bouncedWaves[index];
    }
    
    public List<net.mikolas.lyra.midi.WavetableDump> generateDumpMessages(int deviceId) {
        if (needsRebounce) bounce();
        List<net.mikolas.lyra.midi.WavetableDump> messages = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            messages.add(new net.mikolas.lyra.midi.WavetableDump(
                deviceId,
                getSlot(),
                i,
                bouncedWaves[i],
                getName()
            ));
        }
        return messages;
    }

    public void markDirty() {
        needsRebounce = true;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), slot == null ? "Unassigned" : "Slot " + slot);
    }
}
