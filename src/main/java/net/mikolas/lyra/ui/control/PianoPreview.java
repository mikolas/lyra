package net.mikolas.lyra.ui.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import net.mikolas.lyra.model.MultiPart;

/**
 * 88-Key Piano Roll visualization for Multi Mode splits.
 * Draws overlapping regions for each active part.
 */
public class PianoPreview extends Control {

    private final ObjectProperty<ObservableList<MultiPart>> parts = new SimpleObjectProperty<>(this, "parts", FXCollections.observableArrayList());

    public PianoPreview() {
        getStyleClass().add("piano-preview");
    }

    public ObjectProperty<ObservableList<MultiPart>> partsProperty() {
        return parts;
    }

    public ObservableList<MultiPart> getParts() {
        return parts.get();
    }

    public void setParts(ObservableList<MultiPart> parts) {
        this.parts.set(parts);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PianoPreviewSkin(this);
    }
}
