package net.mikolas.lyra.ui.control;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.mikolas.lyra.model.MultiPart;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.db.SoundRepository;

import java.util.List;

public class VelocityTriangleSkin extends SkinBase<VelocityTriangle> {

    private final Canvas canvas;
    private int draggingPartIndex = -1;
    private boolean draggingLow = false;
    private boolean draggingHigh = false;
    private boolean draggingRange = false;
    private int dragOffset = 0;

    private static final Color COLOR_BG_TRIANGLE = Color.web("#444444"); // LightGray in legacy
    private static final Color COLOR_SELECTION = Color.web("#00FFFF", 0.4); // DarkGray in legacy
    private static final Color COLOR_VALID = Color.web("#00FFFF");
    private static final Color COLOR_INVALID = Color.web("#FF0000");
    private static final Color COLOR_SHADOW = Color.rgb(0, 0, 0, 0.7);

    public VelocityTriangleSkin(VelocityTriangle control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        setupListeners(control);
        setupInteraction(control);
        
        Platform.runLater(this::draw);
    }

    private void setupListeners(VelocityTriangle control) {
        if (control.getParts() != null) {
            attachPartListeners(control.getParts());
        }

        control.partsProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                attachPartListeners(newVal);
            }
            draw();
        });
        canvas.widthProperty().addListener(o -> draw());
        canvas.heightProperty().addListener(o -> draw());
    }

    private void attachPartListeners(List<MultiPart> parts) {
        for (MultiPart part : parts) {
            part.velLowProperty().addListener(o -> draw());
            part.velHighProperty().addListener(o -> draw());
            part.activeProperty().addListener(o -> draw());
            part.channelProperty().addListener(o -> draw());
            part.bankProperty().addListener(o -> draw());
            part.programProperty().addListener(o -> draw());
        }
    }

    private void setupInteraction(VelocityTriangle control) {
        canvas.setOnMouseMoved(e -> {
            MultiPart active = getActivePart();
            if (active == null) {
                canvas.setCursor(Cursor.DEFAULT);
                return;
            }
            double w = canvas.getWidth();
            double xLow = (active.getVelLow() / 127.0) * w;
            double xHigh = (active.getVelHigh() / 127.0) * w;
            double mx = e.getX();

            if (Math.abs(mx - xLow) < 10 || Math.abs(mx - xHigh) < 10) {
                canvas.setCursor(Cursor.H_RESIZE);
            } else if (mx > xLow && mx < xHigh) {
                canvas.setCursor(Cursor.OPEN_HAND);
            } else {
                canvas.setCursor(Cursor.HAND);
            }
        });

        canvas.setOnMousePressed(e -> {
            MultiPart active = getActivePart();
            if (active == null) return;

            double w = canvas.getWidth();
            double xLow = (active.getVelLow() / 127.0) * w;
            double xHigh = (active.getVelHigh() / 127.0) * w;
            double mx = e.getX();
            int vel = getVelAtX(mx);

            draggingLow = draggingHigh = draggingRange = false;

            if (Math.abs(mx - xLow) < 12) {
                draggingLow = true;
            } else if (Math.abs(mx - xHigh) < 12) {
                draggingHigh = true;
            } else if (mx > xLow && mx < xHigh) {
                draggingRange = true;
                dragOffset = vel - active.getVelLow();
                canvas.setCursor(Cursor.CLOSED_HAND);
            } else {
                if (Math.abs(vel - active.getVelLow()) < Math.abs(vel - active.getVelHigh())) {
                    active.setVelLow(vel);
                    draggingLow = true;
                } else {
                    active.setVelHigh(vel);
                    draggingHigh = true;
                }
            }
            draggingPartIndex = 0;
            draw();
        });

        canvas.setOnMouseDragged(e -> {
            MultiPart active = getActivePart();
            if (active == null || draggingPartIndex == -1) return;

            int vel = getVelAtX(e.getX());
            if (draggingLow) {
                active.setVelLow(Math.clamp(vel, 0, 127));
            } else if (draggingHigh) {
                active.setVelHigh(Math.clamp(vel, 0, 127));
            } else if (draggingRange) {
                int width = active.getVelHigh() - active.getVelLow();
                int newLow = Math.clamp(vel - dragOffset, 0, 127 - width);
                active.setVelLow(newLow);
                active.setVelHigh(newLow + width);
            }
            draw();
        });

        canvas.setOnMouseReleased(e -> {
            draggingPartIndex = -1;
            canvas.setCursor(Cursor.DEFAULT);
            draw();
        });
        
        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                MultiPart active = getActivePart();
                if (active != null) {
                    active.setVelLow(0);
                    active.setVelHigh(127);
                    draw();
                }
            }
        });
    }

    private MultiPart getActivePart() {
        if (getSkinnable().getParts() == null) return null;
        for (MultiPart p : getSkinnable().getParts()) {
            if (p.isActive()) return p;
        }
        return null;
    }

    private int getVelAtX(double x) {
        double w = canvas.getWidth();
        return (int) Math.round(Math.clamp((x / w) * 127.0, 0, 127));
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        // 1. Calculate Geometry
        double headerH = 35;
        double contentTop = headerH + 10;
        double contentH = h - contentTop - 30;
        double contentBottom = contentTop + contentH;

        // 2. Draw Background Slope Triangle (Legacy Style)
        // (0, contentBottom) -> (w, contentTop) -> (w, contentBottom)
        gc.setFill(COLOR_BG_TRIANGLE);
        double[] triX = {0, w, w};
        double[] triY = {contentBottom, contentTop, contentBottom};
        gc.fillPolygon(triX, triY, 3);

        // 3. Grid & Identity
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1);
        for (int i = 0; i <= 4; i++) {
            double x = (i / 4.0) * w;
            gc.strokeLine(x, contentTop, x, contentBottom);
            gc.setFill(Color.web("#808080"));
            gc.setFont(Font.font("Geist Mono", 8));
            gc.fillText(String.valueOf(i * 32 == 128 ? 127 : i * 32), x + 2, contentBottom + 12);
        }

        MultiPart active = getActivePart();
        if (active != null) {
            // Focus Shadow
            gc.setFill(COLOR_SHADOW);
            // We draw shadow outside the range
            double xLow = (active.getVelLow() / 127.0) * w;
            double xHigh = (active.getVelHigh() / 127.0) * w;
            
            gc.fillRect(0, contentTop, xLow, contentH);
            gc.fillRect(xHigh, contentTop, w - xHigh, contentH);

            boolean invalid = active.getVelLow() > active.getVelHigh();
            Color themeColor = invalid ? COLOR_INVALID : COLOR_VALID;

            // 4. Header
            gc.setFill(Color.web("#1E1E1E"));
            gc.fillRect(0, 0, w, headerH);
            gc.setStroke(themeColor);
            gc.strokeLine(0, headerH, w, headerH);

            String soundName = "INIT SOUND";
            Sound s = SoundRepository.getInstance().getSoundByBankAndProgram(active.getBankProperty().get(), active.getProgramProperty().get());
            if (s != null) soundName = s.getName();

            gc.setFill(themeColor);
            gc.setFont(Font.font("Geist Mono", FontWeight.BOLD, 14));
            gc.fillText(String.format("EDITING PART %02d | %s", active.getPartIndex() + 1, soundName.toUpperCase()), 10, 22);

            // 5. Selection Area
            if (!invalid) {
                gc.setFill(COLOR_SELECTION);
                // The selection follows the slope: 
                // y at x is linear interp between contentBottom and contentTop
                // y = contentBottom - (x/w * contentH)
                
                double yLow = contentBottom - ( (active.getVelLow() / 127.0) * contentH );
                double yHigh = contentBottom - ( (active.getVelHigh() / 127.0) * contentH );
                
                double[] selX = {xLow, xHigh, xHigh, xLow};
                double[] selY = {contentBottom, contentBottom, yHigh, yLow};
                gc.fillPolygon(selX, selY, 4);
                
                // Vertical Grip Lines
                gc.setStroke(themeColor);
                gc.setLineWidth(2);
                gc.strokeLine(xLow, contentTop, xLow, contentBottom);
                gc.strokeLine(xHigh, contentTop, xHigh, contentBottom);
                
                // Value Labels
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Geist Mono", FontWeight.BOLD, 10));
                gc.fillText(String.valueOf(active.getVelLow()), xLow - 15, contentTop - 5);
                gc.fillText(String.valueOf(active.getVelHigh()), xHigh + 5, contentTop - 5);
            } else {
                gc.setFill(COLOR_INVALID);
                gc.setFont(Font.font("Geist Mono", FontWeight.BOLD, 16));
                gc.fillText("INVALID VELOCITY RANGE", w/2 - 100, contentTop + contentH/2);
            }
        } else {
            // Ghost lanes for other parts
            double laneH = contentH / 16.0;
            if (getSkinnable().getParts() != null) {
                for (int i = 0; i < 16; i++) {
                    MultiPart p = getSkinnable().getParts().get(i);
                    double x1 = (p.getVelLow() / 127.0) * w;
                    double x2 = (p.getVelHigh() / 127.0) * w;
                    if (x2 > x1) {
                        gc.setFill(Color.web("#444444", 0.3));
                        gc.fillRect(x1, contentTop + (i * laneH), x2 - x1, laneH - 1);
                    }
                }
            }
        }
    }
    
    private Color deriveColor(Color base, double opacity) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }
}