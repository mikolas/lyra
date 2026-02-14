package net.mikolas.lyra.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages a set of 128 Multi patches, similar to the legacy MultiSetObject.
 */
public class MultiSet {
    @Getter private final String collectionName;
    private final Map<Integer, MultiPatch> multis = new HashMap<>();
    
    @Getter private final BooleanProperty cleanProperty = new SimpleBooleanProperty(true);

    public MultiSet(String collectionName) {
        this.collectionName = collectionName;
    }

    public MultiPatch get(int index) {
        if (index < 0 || index > 127) return null;
        return multis.computeIfAbsent(index, i -> {
            MultiPatch multi = MultiPatch.builder()
                    .multiIndex(i)
                    .name("Empty slot")
                    .data(new byte[418])
                    .build();
            multi.getParts(); // Force init
            multi.setEdited(false);
            multi.editedProperty().addListener((obs, old, newVal) -> updateCleanStatus());
            return multi;
        });
    }

    public void put(int index, MultiPatch patch) {
        multis.put(index, patch);
        patch.editedProperty().addListener((obs, old, newVal) -> updateCleanStatus());
        updateCleanStatus();
    }

    public List<MultiPatch> getDirtyMultis() {
        return multis.values().stream()
                .filter(MultiPatch::isEdited)
                .collect(Collectors.toList());
    }

    public boolean isClean() {
        return cleanProperty.get();
    }

    private void updateCleanStatus() {
        cleanProperty.set(getDirtyMultis().isEmpty());
    }

    public int count() {
        return multis.size();
    }

    public List<Integer> getExistingIndexes() {
        return multis.keySet().stream().sorted().collect(Collectors.toList());
    }
}
