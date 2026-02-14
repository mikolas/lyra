package net.mikolas.lyra.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Blofeld Multi Patch.
 * Contains global settings and 16 MultiParts.
 */
@Data
@Builder
@AllArgsConstructor
@DatabaseTable(tableName = "multi_patches")
public class MultiPatch {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(uniqueIndex = true)
    private Integer multiIndex; // 0-127

    @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = false)
    private byte[] data; // 418 bytes total

    private transient List<MultiPart> parts;

    private transient StringProperty nameProperty;
    private transient IntegerProperty volumeProperty;
    private transient IntegerProperty tempoProperty;
    private transient BooleanProperty editedProperty = new SimpleBooleanProperty(false);

    private transient ParameterChangeListener parameterChangeListener;
    private transient boolean silentUpdate = false;

    public MultiPatch() {
        this.data = new byte[418];
        initDefaultData();
        getParts(); // Eagerly initialize parts list
        refreshProperties();
    }

    public void setParameterChangeListener(ParameterChangeListener listener) {
        this.parameterChangeListener = listener;
    }

    public void notifyParameterChanged(int offset, int value) {
        if (!silentUpdate && parameterChangeListener != null) {
            parameterChangeListener.onParameterChanged(offset, value);
        }
    }

    public void updateParameterSilently(int offset, int value) {
        if (offset < 0 || offset >= data.length) return;
        boolean oldSilent = this.silentUpdate;
        this.silentUpdate = true;
        try {
            data[offset] = (byte) value;
            if (offset >= 34) {
                int partIdx = (offset - 34) / 24;
                if (partIdx < getParts().size()) {
                    getParts().get(partIdx).updateFromData();
                }
            } else {
                refreshProperties();
            }
        } finally {
            this.silentUpdate = oldSilent;
        }
    }

    /**
     * Swaps the data blocks and internal states of two parts.
     */
    public void swapParts(int idx1, int idx2) {
        if (idx1 < 0 || idx1 >= 16 || idx2 < 0 || idx2 >= 16 || idx1 == idx2) return;
        
        this.silentUpdate = true;
        try {
            int offset1 = 34 + (idx1 * 24);
            int offset2 = 34 + (idx2 * 24);
            
            // Swap 24 bytes of data
            for (int i = 0; i < 24; i++) {
                byte temp = data[offset1 + i];
                data[offset1 + i] = data[offset2 + i];
                data[offset2 + i] = temp;
            }
            
            // Update properties of both parts
            getParts().get(idx1).updateFromData();
            getParts().get(idx2).updateFromData();
            setEdited(true);
            
            // Trigger a full MIDI sync if needed (handled by controller usually)
        } finally {
            this.silentUpdate = false;
        }
    }

    public List<MultiPart> getParts() {
        if (parts == null) {
            if (data == null) {
                data = new byte[418];
                initDefaultData();
            }
            parts = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                parts.add(new MultiPart(this, data, i));
            }
        }
        return parts;
    }

    private void initDefaultData() {
        // Init name "Init Multi" at offset 2
        String initName = "Init Multi";
        for (int i = 0; i < 16; i++) {
            data[2 + i] = (i < initName.length()) ? (byte) initName.charAt(i) : 32;
        }
        data[19] = 127; // Master Volume
        data[20] = 55;  // Default Tempo
        
        // Parts defaults (offset 34 onwards)
        for (int p = 0; partExists(p); p++) {
            int offset = 34 + (p * 24);
            data[offset + 2] = 100; // Volume
            data[offset + 3] = 64;  // Pan
            data[offset + 7] = (byte)(p + 2); // Channel (p+2 matches legacy logic)
            data[offset + 9] = 127; // Key High
            data[offset + 11] = 127; // Vel High
            data[offset + 12] = 0x07; // Receive: MIDI, USB, Local
            data[offset + 13] = 0x3F; // Control: All on
        }
    }

    private boolean partExists(int p) { return p < 16; }

    public StringProperty nameProperty() {
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
            nameProperty.addListener((obs, old, newVal) -> {
                this.name = newVal;
                // Pad to 16 characters with spaces
                String padded = String.format("%-16s", newVal.toUpperCase());
                for (int i = 0; i < 16; i++) {
                    data[2 + i] = (byte) padded.charAt(i);
                }
                setEdited(true);
            });
        }
        return nameProperty;
    }

    public IntegerProperty volumeProperty() {
        if (volumeProperty == null) {
            volumeProperty = new SimpleIntegerProperty(data[19] & 0xFF);
            volumeProperty.addListener((obs, old, newVal) -> {
                data[19] = newVal.byteValue();
                setEdited(true);
                notifyParameterChanged(19, newVal.intValue());
            });
        }
        return volumeProperty;
    }

    public IntegerProperty tempoProperty() {
        if (tempoProperty == null) {
            tempoProperty = new SimpleIntegerProperty(data[20] & 0xFF);
            tempoProperty.addListener((obs, old, newVal) -> {
                data[20] = newVal.byteValue();
                setEdited(true);
                notifyParameterChanged(20, newVal.intValue());
            });
        }
        return tempoProperty;
    }

    public void setEdited(boolean edited) {
        editedProperty.set(edited);
    }

    public boolean isEdited() {
        return editedProperty.get();
    }

    public BooleanProperty editedProperty() {
        return editedProperty;
    }

    public void updateFromMidi(byte[] midiData) {
        if (midiData.length != 418) return;
        this.silentUpdate = true;
        try {
            System.arraycopy(midiData, 0, this.data, 0, 418);
            refreshProperties();
            for (MultiPart part : getParts()) {
                part.updateFromData();
            }
            setEdited(false);
        } finally {
            this.silentUpdate = false;
        }
    }

    public void refreshProperties() {
        // Update name
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int val = data[2 + i] & 0xFF;
            // Blofeld ASCII: 0-31 are rendered as spaces
            sb.append((val < 32 || val > 126) ? ' ' : (char) val);
        }
        this.name = sb.toString().trim();
        if (nameProperty != null) nameProperty.set(this.name);
        
        if (volumeProperty != null) volumeProperty.set(data[19] & 0xFF);
        if (tempoProperty != null) tempoProperty.set(data[20] & 0xFF);
    }
}
