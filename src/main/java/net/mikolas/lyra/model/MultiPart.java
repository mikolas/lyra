package net.mikolas.lyra.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Getter;

/**
 * Model representing one of the 16 parts in a Blofeld Multi.
 * Each part contains 24 bytes of data.
 */
public class MultiPart {
    @Getter private final int partIndex;
    private final byte[] data; // Reference to the 24-byte slice in the MultiPatch
    private final MultiPatch parent;

    @Getter private final IntegerProperty bankProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty programProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty volumeProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty panProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty transposeProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty detuneProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty channelProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty keyLowProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty keyHighProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty velLowProperty = new SimpleIntegerProperty();
    @Getter private final IntegerProperty velHighProperty = new SimpleIntegerProperty();

    public int getKeyLow() { return keyLowProperty.get(); }
    public void setKeyLow(int value) { keyLowProperty.set(value); }
    public int getKeyHigh() { return keyHighProperty.get(); }
    public void setKeyHigh(int value) { keyHighProperty.set(value); }
    public int getVelLow() { return velLowProperty.get(); }
    public void setVelLow(int value) { velLowProperty.set(value); }
    public int getVelHigh() { return velHighProperty.get(); }
    public void setVelHigh(int value) { velHighProperty.set(value); }

    public IntegerProperty keyLowProperty() { return keyLowProperty; }
    public IntegerProperty keyHighProperty() { return keyHighProperty; }
    public IntegerProperty velLowProperty() { return velLowProperty; }
    public IntegerProperty velHighProperty() { return velHighProperty; }
    public IntegerProperty channelProperty() { return channelProperty; }
    public IntegerProperty bankProperty() { return bankProperty; }
    public IntegerProperty programProperty() { return programProperty; }

    // Byte 12: Receive Bitmask
    @Getter private final BooleanProperty midiInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty usbInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty localInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty muteProperty = new SimpleBooleanProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final BooleanProperty active = new SimpleBooleanProperty(false);

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }

    public BooleanProperty activeProperty() { return active; }
    public boolean isActive() { return active.get(); }
    public void setActive(boolean value) { active.set(value); }

    public MultiPatch getPatch() { return parent; }
    public BooleanProperty getMuteProperty() { return muteProperty; }

    // Byte 13: Control Bitmask
    @Getter private final BooleanProperty pitchInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty modInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty pressInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty susInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty editInProperty = new SimpleBooleanProperty();
    @Getter private final BooleanProperty prgInProperty = new SimpleBooleanProperty();

    private boolean isUpdating = false;

    public MultiPart(MultiPatch parent, byte[] multiData, int partIndex) {
        this.parent = parent;
        this.data = multiData;
        this.partIndex = partIndex;
        setupBindings();
    }

    private int getOffset() {
        // Multi data header: 2 (index) + 16 (name) + 16 (global) = 34 bytes
        return 34 + (partIndex * 24);
    }

    private void setupBindings() {
        // Simple mapping for bytes 0-11
        bindInt(bankProperty, 0);
        bindInt(programProperty, 1);
        bindInt(volumeProperty, 2);
        bindInt(panProperty, 3);
        // Byte 4 is unknown/reserved
        bindInt(transposeProperty, 5);
        bindInt(detuneProperty, 6);
        bindInt(channelProperty, 7);
        bindInt(keyLowProperty, 8);
        bindInt(keyHighProperty, 9);
        bindInt(velLowProperty, 10);
        bindInt(velHighProperty, 11);

        // Bitmask for Byte 12 (Receive)
        receiveByteBinding();

        // Bitmask for Byte 13 (Control)
        controlByteBinding();
    }

    private void bindInt(IntegerProperty prop, int relativeOffset) {
        int absOffset = getOffset() + relativeOffset;
        prop.set(data[absOffset] & 0xFF);
        prop.addListener((obs, old, newVal) -> {
            if (isUpdating) return;
            data[absOffset] = newVal.byteValue();
            parent.setEdited(true);
            parent.notifyParameterChanged(absOffset, newVal.intValue());
        });
    }

    private void receiveByteBinding() {
        int absOffset = getOffset() + 12;
        int val = data[absOffset] & 0xFF;
        midiInProperty.set((val & 0x01) != 0);
        usbInProperty.set((val & 0x02) != 0);
        localInProperty.set((val & 0x04) != 0);
        muteProperty.set((val & 0x40) != 0);

        javafx.beans.value.ChangeListener<Object> listener = (obs, old, newVal) -> {
            if (isUpdating) return;
            int bitmask = 0;
            if (midiInProperty.get()) bitmask |= 0x01;
            if (usbInProperty.get()) bitmask |= 0x02;
            if (localInProperty.get()) bitmask |= 0x04;
            if (muteProperty.get()) bitmask |= 0x40;
            data[absOffset] = (byte) bitmask;
            parent.setEdited(true);
            parent.notifyParameterChanged(absOffset, bitmask);
        };

        midiInProperty.addListener(listener);
        usbInProperty.addListener(listener);
        localInProperty.addListener(listener);
        muteProperty.addListener(listener);
    }

    private void controlByteBinding() {
        int absOffset = getOffset() + 13;
        int val = data[absOffset] & 0xFF;
        pitchInProperty.set((val & 0x01) != 0);
        modInProperty.set((val & 0x02) != 0);
        pressInProperty.set((val & 0x04) != 0);
        susInProperty.set((val & 0x08) != 0);
        editInProperty.set((val & 0x10) != 0);
        prgInProperty.set((val & 0x20) != 0);

        javafx.beans.value.ChangeListener<Object> listener = (obs, old, newVal) -> {
            if (isUpdating) return;
            int bitmask = 0;
            if (pitchInProperty.get()) bitmask |= 0x01;
            if (modInProperty.get()) bitmask |= 0x02;
            if (pressInProperty.get()) bitmask |= 0x04;
            if (susInProperty.get()) bitmask |= 0x08;
            if (editInProperty.get()) bitmask |= 0x10;
            if (prgInProperty.get()) bitmask |= 0x20;
            data[absOffset] = (byte) bitmask;
            parent.setEdited(true);
            parent.notifyParameterChanged(absOffset, bitmask);
        };

        pitchInProperty.addListener(listener);
        modInProperty.addListener(listener);
        pressInProperty.addListener(listener);
        susInProperty.addListener(listener);
        editInProperty.addListener(listener);
        prgInProperty.addListener(listener);
    }

    public void updateFromData() {
        isUpdating = true;
        try {
            int offset = getOffset();
            bankProperty.set(data[offset] & 0xFF);
            programProperty.set(data[offset + 1] & 0xFF);
            volumeProperty.set(data[offset + 2] & 0xFF);
            panProperty.set(data[offset + 3] & 0xFF);
            transposeProperty.set(data[offset + 5] & 0xFF);
            detuneProperty.set(data[offset + 6] & 0xFF);
            channelProperty.set(data[offset + 7] & 0xFF);
            keyLowProperty.set(data[offset + 8] & 0xFF);
            keyHighProperty.set(data[offset + 9] & 0xFF);
            velLowProperty.set(data[offset + 10] & 0xFF);
            velHighProperty.set(data[offset + 11] & 0xFF);

            int rcv = data[offset + 12] & 0xFF;
            midiInProperty.set((rcv & 0x01) != 0);
            usbInProperty.set((rcv & 0x02) != 0);
            localInProperty.set((rcv & 0x04) != 0);
            muteProperty.set((rcv & 0x40) != 0);

            int ctl = data[offset + 13] & 0xFF;
            pitchInProperty.set((ctl & 0x01) != 0);
            modInProperty.set((ctl & 0x02) != 0);
            pressInProperty.set((ctl & 0x04) != 0);
            susInProperty.set((ctl & 0x08) != 0);
            editInProperty.set((ctl & 0x10) != 0);
            prgInProperty.set((ctl & 0x20) != 0);
        } finally {
            isUpdating = false;
        }
    }
}
