package net.mikolas.lyra.ui.control;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;

public class RangeSliderSkin extends SkinBase<RangeSlider> {

    private final Canvas canvas;
    private boolean draggingLow = false;
    private boolean draggingHigh = false;
    private boolean draggingRange = false;
    
    // Use doubles for internal drag state to avoid rounding "sticking"
    private double internalLow;
    private double internalHigh;
    private double lastMouseX;

    private static final double THUMB_WIDTH = 12.0; // Larger hit area
    private static final double TRACK_HEIGHT = 4.0;

    public RangeSliderSkin(RangeSlider control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        canvas.setOnMousePressed(e -> {
            double x = e.getX();
            double w = canvas.getWidth();
            double min = control.getMin();
            double max = control.getMax();
            double range = max - min;

            double lowPos = ((control.getLowValue() - min) / range) * w;
            double highPos = ((control.getHighValue() - min) / range) * w;

            draggingLow = false;
            draggingHigh = false;
            draggingRange = false;
            
            internalLow = control.getLowValue();
            internalHigh = control.getHighValue();
            lastMouseX = x;

            if (Math.abs(x - lowPos) <= THUMB_WIDTH) {
                draggingLow = true;
            } else if (Math.abs(x - highPos) <= THUMB_WIDTH) {
                draggingHigh = true;
            } else if (x > lowPos && x < highPos) {
                draggingRange = true;
            } else {
                // Snap nearest
                double clickVal = (x / w) * range + min;
                if (Math.abs(clickVal - internalLow) < Math.abs(clickVal - internalHigh)) {
                    control.setLowValue(Math.round(clickVal));
                    draggingLow = true;
                } else {
                    control.setHighValue(Math.round(clickVal));
                    draggingHigh = true;
                }
                internalLow = control.getLowValue();
                internalHigh = control.getHighValue();
            }
        });

        canvas.setOnMouseDragged(e -> {
            double x = e.getX();
            double w = canvas.getWidth();
            double min = control.getMin();
            double max = control.getMax();
            double range = max - min;
            
            double deltaX = x - lastMouseX;
            double deltaVal = (deltaX / w) * range;
            lastMouseX = x;

            if (draggingLow) {
                internalLow = Math.clamp(internalLow + deltaVal, min, control.getHighValue());
                control.setLowValue(Math.round(internalLow));
            } else if (draggingHigh) {
                internalHigh = Math.clamp(internalHigh + deltaVal, control.getLowValue(), max);
                control.setHighValue(Math.round(internalHigh));
            } else if (draggingRange) {
                double width = internalHigh - internalLow;
                double nextLow = internalLow + deltaVal;
                double nextHigh = nextLow + width;
                
                if (nextLow >= min && nextHigh <= max) {
                    internalLow = nextLow;
                    internalHigh = nextHigh;
                    control.setLowValue(Math.round(internalLow));
                    control.setHighValue(Math.round(internalHigh));
                }
            }
        });

        control.lowValueProperty().addListener(o -> draw());
        control.highValueProperty().addListener(o -> draw());
        control.minProperty().addListener(o -> draw());
        control.maxProperty().addListener(o -> draw());
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

        RangeSlider control = getSkinnable();
        double min = control.getMin();
        double max = control.getMax();
        double range = max - min;

        double lowPos = ((control.getLowValue() - min) / range) * w;
        double highPos = ((control.getHighValue() - min) / range) * w;
        double cy = h / 2;

        // 1. Background Track
        gc.setFill(Color.web("#333333"));
        gc.fillRect(0, cy - TRACK_HEIGHT / 2, w, TRACK_HEIGHT);

        // 2. Highlighted Range (Cyan)
        gc.setFill(Color.web("#00FFFF"));
        gc.fillRect(lowPos, cy - TRACK_HEIGHT / 2, Math.max(1, highPos - lowPos), TRACK_HEIGHT);

        // 3. Thumbs
        gc.setFill(Color.WHITE);
        gc.fillRect(lowPos - 2, cy - h / 3, 4, (h / 3) * 2);
        gc.fillRect(highPos - 2, cy - h / 3, 4, (h / 3) * 2);
        
        // 4. Hit area indicators (subtle)
        gc.setFill(Color.web("#FFFFFF", 0.1));
        gc.fillOval(lowPos - THUMB_WIDTH/2, cy - THUMB_WIDTH/2, THUMB_WIDTH, THUMB_WIDTH);
        gc.fillOval(highPos - THUMB_WIDTH/2, cy - THUMB_WIDTH/2, THUMB_WIDTH, THUMB_WIDTH);
    }
}