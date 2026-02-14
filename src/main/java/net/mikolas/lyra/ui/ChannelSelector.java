package net.mikolas.lyra.ui;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom widget for selecting MIDI channels (1-16 + OMNI).
 */
public class ChannelSelector extends HBox {

    private final CheckBox[] channelBoxes = new CheckBox[16];
    private final CheckBox omniBox;

    public ChannelSelector() {
        setSpacing(5);
        setPadding(new Insets(5));
        
        // Create 16 channel checkboxes
        for (int i = 0; i < 16; i++) {
            final int channel = i + 1;
            channelBoxes[i] = new CheckBox(String.valueOf(channel));
            channelBoxes[i].setSelected(true); // All channels by default
            getChildren().add(channelBoxes[i]);
        }
        
        // Create OMNI checkbox
        omniBox = new CheckBox("OMNI");
        omniBox.setStyle("-fx-font-weight: bold;");
        getChildren().add(omniBox);
        
        // OMNI checkbox behavior
        omniBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Select all channels
                for (CheckBox box : channelBoxes) {
                    box.setSelected(true);
                }
            }
        });
        
        // Update OMNI when individual channels change
        for (CheckBox box : channelBoxes) {
            box.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateOmniState();
            });
        }
    }

    private void updateOmniState() {
        boolean allSelected = true;
        for (CheckBox box : channelBoxes) {
            if (!box.isSelected()) {
                allSelected = false;
                break;
            }
        }
        omniBox.setSelected(allSelected);
    }

    public Set<Integer> getSelectedChannels() {
        Set<Integer> channels = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            if (channelBoxes[i].isSelected()) {
                channels.add(i + 1);
            }
        }
        return channels;
    }

    public void setSelectedChannels(Set<Integer> channels) {
        for (int i = 0; i < 16; i++) {
            channelBoxes[i].setSelected(channels.contains(i + 1));
        }
        updateOmniState();
    }
}
