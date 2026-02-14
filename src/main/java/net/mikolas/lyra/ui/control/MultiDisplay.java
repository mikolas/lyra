package net.mikolas.lyra.ui.control;

import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Custom header display for Multi Mode.
 * Shows Slot ID (001-128), Name, Collection, and MIDI Activity LED.
 */
public class MultiDisplay extends Control {

    private final IntegerProperty slotId = new SimpleIntegerProperty(this, "slotId", 1);
    private final StringProperty name = new SimpleStringProperty(this, "name", "INIT MULTI");
    private final StringProperty collection = new SimpleStringProperty(this, "collection", "User Library");
    private final BooleanProperty midiActive = new SimpleBooleanProperty(this, "midiActive", false);
    private final BooleanProperty isLoading = new SimpleBooleanProperty(this, "isLoading", false);
    private final IntegerProperty activityBitmask = new SimpleIntegerProperty(this, "activityBitmask", 0);

    public MultiDisplay() {
        getStyleClass().add("multi-display");
    }

    // Properties
    public IntegerProperty activityBitmaskProperty() { return activityBitmask; }
    public int getActivityBitmask() { return activityBitmask.get(); }
    public void setActivityBitmask(int value) { activityBitmask.set(value); }

    public IntegerProperty slotIdProperty() { return slotId; }
    public int getSlotId() { return slotId.get(); }
    public void setSlotId(int value) { slotId.set(value); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }

    public StringProperty collectionProperty() { return collection; }
    public String getCollection() { return collection.get(); }
    public void setCollection(String value) { collection.set(value); }

    public BooleanProperty midiActiveProperty() { return midiActive; }
    public boolean isMidiActive() { return midiActive.get(); }
    public void setMidiActive(boolean value) { midiActive.set(value); }

    public BooleanProperty isLoadingProperty() { return isLoading; }
    public boolean isLoading() { return isLoading.get(); }
    public void setLoading(boolean value) { isLoading.set(value); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MultiDisplaySkin(this);
    }
}
