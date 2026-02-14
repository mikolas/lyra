package net.mikolas.lyra.ui.control;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RangeSliderTest {

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    @Test
    void testInitialValues() {
        RangeSlider slider = new RangeSlider();
        assertEquals(0, slider.getMin());
        assertEquals(127, slider.getMax());
        assertEquals(0, slider.getLowValue());
        assertEquals(127, slider.getHighValue());
    }

    @Test
    void testValueConstraints() {
        RangeSlider slider = new RangeSlider();
        
        // Setting low within range should not affect high
        slider.setLowValue(50);
        assertEquals(50, slider.getLowValue());
        assertEquals(127, slider.getHighValue());
        
        // Setting low > current high should push high up
        slider.setHighValue(80);
        slider.setLowValue(100);
        assertEquals(100, slider.getLowValue());
        assertEquals(100, slider.getHighValue());
        
        // Setting high < current low should pull low down
        slider.setHighValue(40);
        assertEquals(40, slider.getHighValue());
        assertEquals(40, slider.getLowValue());
    }

    @Test
    void testRangeConstructor() {
        RangeSlider slider = new RangeSlider(0, 100, 20, 80);
        assertEquals(0, slider.getMin());
        assertEquals(100, slider.getMax());
        assertEquals(20, slider.getLowValue());
        assertEquals(80, slider.getHighValue());
    }
}
