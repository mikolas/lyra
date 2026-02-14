package net.mikolas.lyra.ui.control;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class DialSkin extends SkinBase<Dial> {

    private final Canvas canvas;
    private double lastY;
    private double accumulatorY = 0;
    
    // Constants for detented feel
    private static final double DISCRETE_THRESHOLD = 64; // steps or fewer = discrete
    private static final double PIXELS_PER_STEP = 12.0;
    private static final int MAX_TICKS = 16;

    public DialSkin(Dial control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        canvas.setOnMousePressed(e -> {
            lastY = e.getSceneY();
            accumulatorY = 0;
        });

        canvas.setOnMouseDragged(e -> {
            double deltaY = lastY - e.getSceneY();
            lastY = e.getSceneY();
            
            double range = control.getMax() - control.getMin();
            double step = control.getStep();
            double totalSteps = range / step;
            boolean isDiscrete = totalSteps <= DISCRETE_THRESHOLD;

            if (isDiscrete) {
                accumulatorY += deltaY;
                double threshold = e.isShiftDown() ? PIXELS_PER_STEP / 2.0 : PIXELS_PER_STEP;
                
                if (Math.abs(accumulatorY) >= threshold) {
                    int stepsToMove = (int) (accumulatorY / threshold);
                    double valueDelta = stepsToMove * step;
                    control.setValue(Math.clamp(control.getValue() + valueDelta, control.getMin(), control.getMax()));
                    accumulatorY -= stepsToMove * threshold;
                }
            } else {
                // Continuous mode (Original smooth logic)
                double multiplier = e.isShiftDown() ? 0.1 : 1.0;
                double valueDelta = (deltaY / 100.0) * range * multiplier;
                control.setValue(Math.clamp(control.getValue() + valueDelta, control.getMin(), control.getMax()));
            }
        });

        canvas.setOnScroll(e -> {
            double dy = e.getDeltaY();
            if (dy == 0) return;
            
            double range = control.getMax() - control.getMin();
            double step = control.getStep();
            double totalSteps = range / step;
            boolean isDiscrete = totalSteps <= DISCRETE_THRESHOLD;

            double delta;
            if (isDiscrete) {
                // One wheel notch = one full step, ignore shift
                delta = (dy > 0 ? 1 : -1) * step;
            } else {
                // Continuous scroll with shift precision
                double multiplier = e.isShiftDown() ? 0.1 : 1.0;
                delta = (dy > 0 ? 1 : -1) * step * multiplier;
            }
            
            control.setValue(Math.clamp(control.getValue() + delta, control.getMin(), control.getMax()));
            e.consume();
        });

        control.valueProperty().addListener(o -> draw());
        control.labelProperty().addListener(o -> draw());
        control.widthProperty().addListener(o -> draw());
        control.heightProperty().addListener(o -> draw());
        
        draw();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        Dial control = getSkinnable();
        double min = control.getMin();
        double max = control.getMax();
        double step = control.getStep();
        double range = max - min;
        double totalSteps = range / step;
        boolean isDiscrete = totalSteps <= DISCRETE_THRESHOLD;

        // Snap value for drawing if discrete
        double displayVal = control.getValue();
        if (isDiscrete) {
            displayVal = Math.round((displayVal - min) / step) * step + min;
        }
        
        double percentage = (displayVal - min) / range;

        // Visual Calibration
        Font font = control.getFont();
        double labelFontSize = font.getSize();
        double labelH = labelFontSize + 4; 
        double cx = w / 2;
        double cy = (h - labelH) / 2;
        double radius = Math.min(w * 0.85, (h - labelH) * 0.85) / 2;
        if (radius < 4) radius = 4;

        // 1. Knob Body
        gc.setFill(Color.web("#171717"));
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        
        // 2. Outer Track
        gc.setStroke(Color.web("#333333"));
        double trackWidth = radius < 20 ? 2.5 : 3.5;
        gc.setLineWidth(trackWidth);
        gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, -135, -270, ArcType.OPEN);

        // 3. Tick Marks (only for very small ranges)
        if (totalSteps <= MAX_TICKS && totalSteps > 0) {
            gc.setStroke(Color.web("#444444"));
            gc.setLineWidth(1.0);
            for (int i = 0; i <= totalSteps; i++) {
                double angle = 135 + (i * (270.0 / totalSteps));
                double innerR = radius * 0.85;
                double outerR = radius * 1.05;
                double tx1 = cx + Math.cos(Math.toRadians(angle)) * innerR;
                double ty1 = cy + Math.sin(Math.toRadians(angle)) * innerR;
                double tx2 = cx + Math.cos(Math.toRadians(angle)) * outerR;
                double ty2 = cy + Math.sin(Math.toRadians(angle)) * outerR;
                gc.strokeLine(tx1, ty1, tx2, ty2);
            }
        }

        // 4. Pro Active Arc (Cyan)
        gc.setStroke(Color.web("#00FFFF"));
        gc.setLineWidth(trackWidth);
        gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, -135, -270 * percentage, ArcType.OPEN);

        // 5. Indicator Line (White)
        double drawAngle = 135 + (270 * percentage);
        double indInnerR = radius * 0.45;
        double indOuterR = radius * 0.95;
        double x1 = cx + Math.cos(Math.toRadians(drawAngle)) * indInnerR;
        double y1 = cy + Math.sin(Math.toRadians(drawAngle)) * indInnerR;
        double x2 = cx + Math.cos(Math.toRadians(drawAngle)) * indOuterR;
        double y2 = cy + Math.sin(Math.toRadians(drawAngle)) * indOuterR;
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeLine(x1, y1, x2, y2);

        // 6. Label
        gc.setFill(Color.web("#808080"));
        gc.setFont(control.getFont());
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(control.getLabel().toUpperCase(), cx, h - 2);
    }
}