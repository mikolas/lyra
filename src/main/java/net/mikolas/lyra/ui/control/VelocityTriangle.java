package net.mikolas.lyra.ui.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import net.mikolas.lyra.model.MultiPart;

/**
 * Interactive visualization of velocity ranges for all 16 parts.
 * Draws parts as overlapping zones to visualize layering.
 */
public class VelocityTriangle extends Control {

    private final ObjectProperty<ObservableList<MultiPart>> parts = new SimpleObjectProperty<>(this, "parts", FXCollections.observableArrayList());

    public VelocityTriangle() {
        getStyleClass().add("velocity-triangle");
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
        return new VelocityTriangleSkin(this);
    }
}
