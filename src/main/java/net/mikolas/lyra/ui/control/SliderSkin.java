package net.mikolas.lyra.ui.control;

import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

/**
 * Custom Skin for Sliders to provide the same "Detented" clicky feel as Dials.
 * Supports both Horizontal and Vertical orientations.
 */
public class SliderSkin extends SkinBase<Slider> {

    private final Canvas canvas;
    private double lastMousePos;
    private double accumulator = 0;

    private static final double DISCRETE_THRESHOLD = 64;
    private static final double PIXELS_PER_STEP_V = 12.0;
    private static final double PIXELS_PER_STEP_H = 8.0;

    public SliderSkin(Slider control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        canvas.setOnMousePressed(e -> {
            lastMousePos = control.getOrientation() == Orientation.VERTICAL ? e.getY() : e.getX();
            accumulator = 0;
        });

        canvas.setOnMouseDragged(e -> {
            double currentPos = control.getOrientation() == Orientation.VERTICAL ? e.getY() : e.getX();
            // Drag UP (dec Y) or RIGHT (inc X) increases value
            double delta = control.getOrientation() == Orientation.VERTICAL ? lastMousePos - currentPos : currentPos - lastMousePos;
            lastMousePos = currentPos;

            double range = control.getMax() - control.getMin();
            boolean isDiscrete = range <= DISCRETE_THRESHOLD;

            if (isDiscrete) {
                accumulator += delta;
                double baseThreshold = control.getOrientation() == Orientation.VERTICAL ? PIXELS_PER_STEP_V : PIXELS_PER_STEP_H;
                double threshold = e.isShiftDown() ? baseThreshold / 2.0 : baseThreshold;

                if (Math.abs(accumulator) >= threshold) {
                    int stepsToMove = (int) (accumulator / threshold);
                    control.setValue(Math.clamp(control.getValue() + stepsToMove, control.getMin(), control.getMax()));
                    accumulator -= stepsToMove * threshold;
                }
            } else {
                // Continuous mode
                double multiplier = e.isShiftDown() ? 0.1 : 1.0;
                double factor = control.getOrientation() == Orientation.VERTICAL ? 150.0 : 300.0;
                double valueDelta = (delta / factor) * range * multiplier;
                control.setValue(Math.clamp(control.getValue() + valueDelta, control.getMin(), control.getMax()));
            }
        });

        canvas.setOnScroll(e -> {
            double scrollDelta = e.getDeltaY();
            if (scrollDelta == 0) return;

            double range = control.getMax() - control.getMin();
            boolean isDiscrete = range <= DISCRETE_THRESHOLD;

            double valueDelta;
            if (isDiscrete) {
                valueDelta = (scrollDelta > 0 ? 1 : -1);
            } else {
                double multiplier = e.isShiftDown() ? 0.1 : 1.0;
                valueDelta = (scrollDelta > 0 ? 1 : -1) * (range / 100.0) * multiplier;
            }

            control.setValue(Math.clamp(control.getValue() + valueDelta, control.getMin(), control.getMax()));
            e.consume();
        });

        control.valueProperty().addListener(o -> draw());
        control.widthProperty().addListener(o -> draw());
        control.heightProperty().addListener(o -> draw());

        draw();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return 140 + leftInset + rightInset;
        } else {
            return 18 + leftInset + rightInset;
        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return 18 + topInset + bottomInset;
        } else {
            return 140 + topInset + bottomInset;
        }
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        Slider control = getSkinnable();
        double min = control.getMin();
        double max = control.getMax();
        double range = max - min;
        boolean isVertical = control.getOrientation() == Orientation.VERTICAL;
        boolean isDiscrete = range <= DISCRETE_THRESHOLD;

        double displayVal = control.getValue();
        if (isDiscrete) {
            displayVal = Math.round(displayVal);
        }
        double percentage = (displayVal - min) / range;

        // Visual layout
        double trackThickness = 6;
        double margin = 5;
        
        boolean isAccent = control.getStyleClass().contains("accent-slider");
        Color activeColor = isAccent ? Color.web("#FFA500") : Color.web("#00FFFF");

        if (isVertical) {
            double trackX = (w - trackThickness) / 2;
            double trackH = h - (margin * 2);
            
            // 1. Background Track
            gc.setFill(Color.web("#171717"));
            gc.fillRoundRect(trackX, margin, trackThickness, trackH, 3, 3);
            gc.setStroke(Color.web("#333333"));
            gc.strokeRoundRect(trackX, margin, trackThickness, trackH, 3, 3);

            // 2. Active Area
            double activeH = trackH * percentage;
            double activeY = margin + trackH - activeH;
            gc.setFill(activeColor);
            gc.fillRoundRect(trackX + 1, activeY, trackThickness - 2, activeH, 2, 2);

            // 3. Ticks
            if (isDiscrete && range > 0 && range <= 16) {
                gc.setStroke(Color.web("#444444"));
                for (int i = 0; i <= (int)range; i++) {
                    double ty = margin + trackH - (i * (trackH / range));
                    gc.strokeLine(trackX - 3, ty, trackX + trackThickness + 3, ty);
                }
            }

            // 4. Thumb
            double thumbH = 8;
            double thumbW = trackThickness + 6;
            double thumbY = margin + trackH - (trackH * percentage) - (thumbH / 2);
            double thumbX = (w - thumbW) / 2;
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(thumbX, thumbY, thumbW, thumbH, 2, 2);
        } else {
            // Horizontal
            double trackY = (h - trackThickness) / 2;
            double trackW = w - (margin * 2);

            // 1. Background
            gc.setFill(Color.web("#171717"));
            gc.fillRoundRect(margin, trackY, trackW, trackThickness, 3, 3);
            gc.setStroke(Color.web("#333333"));
            gc.strokeRoundRect(margin, trackY, trackW, trackThickness, 3, 3);

            // 2. Active
            double activeW = trackW * percentage;
            gc.setFill(activeColor);
            gc.fillRoundRect(margin, trackY + 1, activeW, trackThickness - 2, 2, 2);

            // 3. Ticks
            if (isDiscrete && range > 0 && range <= 16) {
                gc.setStroke(Color.web("#444444"));
                for (int i = 0; i <= (int)range; i++) {
                    double tx = margin + (i * (trackW / range));
                    gc.strokeLine(tx, trackY - 3, tx, trackY + trackThickness + 3);
                }
            }

            // 4. Thumb
            double thumbW = 8;
            double thumbH = trackThickness + 6;
            double thumbX = margin + (trackW * percentage) - (thumbW / 2);
            double thumbY = (h - thumbH) / 2;
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(thumbX, thumbY, thumbW, thumbH, 2, 2);
        }
    }
}
