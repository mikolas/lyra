package net.mikolas.lyra.ui.control;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MultiDisplaySkin extends SkinBase<MultiDisplay> {

    private final Canvas canvas;
    private static final String FONT_FAMILY = "Geist Mono";

    public MultiDisplaySkin(MultiDisplay control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        control.slotIdProperty().addListener(o -> draw());
        control.nameProperty().addListener(o -> draw());
        control.collectionProperty().addListener(o -> draw());
        control.midiActiveProperty().addListener(o -> draw());
        control.isLoadingProperty().addListener(o -> draw());
        control.activityBitmaskProperty().addListener(o -> draw());
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());

        draw();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        MultiDisplay control = getSkinnable();

        // 1. Background Panel
        gc.setFill(Color.web("#1E1E1E"));
        gc.fillRoundRect(0, 0, w, h, 4, 4);
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(0.5, 0.5, w - 1, h - 1, 4, 4);

        // 2. Slot ID (Large, Left)
        gc.setFill(Color.web("#00FFFF"));
        gc.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        String slotText = String.format("%03d", control.getSlotId());
        gc.fillText(slotText, 10, 30);

        // 3. Name (Main, Center-Top)
        if (control.isLoading()) {
            gc.setFill(Color.web("#00FFFF"));
            gc.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
            gc.fillText("REQUESTING...", 65, 20);
        } else {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
            gc.fillText(control.getName().toUpperCase(), 65, 20);
        }

        // 4. Collection (Sub-label, Center-Bottom)
        gc.setFill(Color.web("#808080"));
        gc.setFont(Font.font(FONT_FAMILY, 10));
        gc.fillText(control.getCollection(), 65, 35);

        // 5. MIDI LED (Main Status - Right Top)
        Color ledColor = control.isMidiActive() ? Color.web("#00FFFF") : Color.web("#333333");
        gc.setFill(ledColor);
        gc.fillOval(w - 20, 8, 8, 8);
        
        // 6. 16 Part Activity LEDs (Bottom Row)
        double ledSpacing = (w - 100) / 16.0;
        double startX = 65;
        double ledY = h - 10;
        int bitmask = control.getActivityBitmask();
        
        for (int i = 0; i < 16; i++) {
            boolean active = (bitmask & (1 << i)) != 0;
            gc.setFill(active ? Color.web("#00FFFF") : Color.web("#333333"));
            gc.fillOval(startX + (i * ledSpacing), ledY, 4, 4);
        }
        
        // LED Glow if active
        if (control.isMidiActive()) {
            gc.setStroke(Color.web("#00FFFF", 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(w - 22, 10, 12, 12);
        }
    }
}
