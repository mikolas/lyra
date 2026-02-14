package net.mikolas.lyra.ui;

import javafx.scene.control.CheckBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChannelSelectorTest {

    @BeforeAll
    static void initToolkit() {
        // Initialize JavaFX toolkit
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testDefaultAllChannelsSelected() {
        ChannelSelector selector = new ChannelSelector();
        Set<Integer> channels = selector.getSelectedChannels();
        assertEquals(16, channels.size());
        for (int i = 1; i <= 16; i++) {
            assertTrue(channels.contains(i));
        }
    }

    @Test
    void testSetSelectedChannels() {
        ChannelSelector selector = new ChannelSelector();
        Set<Integer> channels = Set.of(1, 2, 3);
        selector.setSelectedChannels(channels);
        
        Set<Integer> result = selector.getSelectedChannels();
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
        assertFalse(result.contains(4));
    }

    @Test
    void testEmptySelection() {
        ChannelSelector selector = new ChannelSelector();
        selector.setSelectedChannels(Set.of());
        
        Set<Integer> result = selector.getSelectedChannels();
        assertEquals(0, result.size());
    }

    @Test
    void testSingleChannel() {
        ChannelSelector selector = new ChannelSelector();
        selector.setSelectedChannels(Set.of(10));
        
        Set<Integer> result = selector.getSelectedChannels();
        assertEquals(1, result.size());
        assertTrue(result.contains(10));
    }

    @Test
    void testAllChannels() {
        ChannelSelector selector = new ChannelSelector();
        Set<Integer> allChannels = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        selector.setSelectedChannels(allChannels);
        
        Set<Integer> result = selector.getSelectedChannels();
        assertEquals(16, result.size());
    }
}
