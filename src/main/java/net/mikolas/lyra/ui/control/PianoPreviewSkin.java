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
import net.mikolas.lyra.util.MidiUtils;

import java.util.List;

public class PianoPreviewSkin extends SkinBase<PianoPreview> {

    private final Canvas canvas;
    private static final int NUM_KEYS = 128;
    
    // Interaction State
    private int draggingPartIndex = -1;
    private boolean draggingLow = false;
    private boolean draggingHigh = false;
    private boolean draggingRange = false;
    private int dragOffset = 0;

    // Tactical Colors
    private static final Color COLOR_VALID = Color.web("#00FFFF");
    private static final Color COLOR_INVALID = Color.web("#FF0000");
    private static final Color COLOR_SHADOW = Color.rgb(0, 0, 0, 0.6);
    private static final Color COLOR_WHITE_KEY = Color.web("#DDDDDD");
    private static final Color COLOR_BLACK_KEY = Color.web("#121212");
    private static final Color COLOR_KEY_BORDER = Color.web("#333333");

    public PianoPreviewSkin(PianoPreview control) {
        super(control);
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        setupListeners(control);
        setupInteraction(control);
        
        // Initial draw
        Platform.runLater(this::draw);
    }

    private void setupListeners(PianoPreview control) {
        // Handle initial parts
        if (control.getParts() != null) {
            attachPartListeners(control.getParts());
        }

        // Handle part list replacement
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
            part.keyLowProperty().addListener(o -> draw());
            part.keyHighProperty().addListener(o -> draw());
            part.activeProperty().addListener(o -> draw());
            part.channelProperty().addListener(o -> draw());
            part.bankProperty().addListener(o -> draw());
            part.programProperty().addListener(o -> draw());
        }
    }

    private void setupInteraction(PianoPreview control) {
        canvas.setOnMouseMoved(e -> {
            MultiPart active = getActivePart();
            if (active == null) {
                canvas.setCursor(Cursor.DEFAULT);
                return;
            }
            double xLow = getXForNote(active.getKeyLow());
            double xHigh = getXForNote(active.getKeyHigh());
            double mx = e.getX();

            if (Math.abs(mx - xLow) < 12 || Math.abs(mx - xHigh) < 12) {
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

            double xLow = getXForNote(active.getKeyLow());
            double xHigh = getXForNote(active.getKeyHigh());
            double mx = e.getX();
            int note = getNoteAtX(mx);

            draggingLow = draggingHigh = draggingRange = false;

            if (Math.abs(mx - xLow) < 12) {
                draggingLow = true;
            } else if (Math.abs(mx - xHigh) < 12) {
                draggingHigh = true;
            } else if (mx > xLow && mx < xHigh) {
                draggingRange = true;
                dragOffset = note - active.getKeyLow();
                canvas.setCursor(Cursor.CLOSED_HAND);
            } else {
                if (Math.abs(note - active.getKeyLow()) < Math.abs(note - active.getKeyHigh())) {
                    active.setKeyLow(note);
                    draggingLow = true;
                } else {
                    active.setKeyHigh(note);
                    draggingHigh = true;
                }
            }
            draggingPartIndex = 0;
            draw();
        });

        canvas.setOnMouseDragged(e -> {
            MultiPart active = getActivePart();
            if (active == null || draggingPartIndex == -1) return;

            int note = getNoteAtX(e.getX());
            if (draggingLow) {
                active.setKeyLow(Math.clamp(note, 0, 127));
            } else if (draggingHigh) {
                active.setKeyHigh(Math.clamp(note, 0, 127));
            } else if (draggingRange) {
                int width = active.getKeyHigh() - active.getKeyLow();
                int newLow = Math.clamp(note - dragOffset, 0, 127 - width);
                active.setKeyLow(newLow);
                active.setKeyHigh(newLow + width);
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
                    active.setKeyLow(0);
                    active.setKeyHigh(127);
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

    private double getXForNote(int note) {
        double w = canvas.getWidth();
        return (note / 127.0) * w;
    }

    private int getNoteAtX(double x) {
        double w = canvas.getWidth();
        return (int) Math.round(Math.clamp((x / w) * 127.0, 0, 127));
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        MultiPart active = getActivePart();
        
        double headerH = 35;
        double pianoTop = headerH + 5;
        double pianoH = h * 0.45;
        double keyWidth = w / 75.0; 

        drawKeyboard(gc, pianoTop, keyWidth, pianoH, pianoH * 0.6, keyWidth * 0.6, active);

        if (active != null) {
            boolean invalid = active.getKeyLow() > active.getKeyHigh();
            Color themeColor = invalid ? COLOR_INVALID : COLOR_VALID;
            
            gc.setFill(Color.web("#1E1E1E"));
            gc.fillRect(0, 0, w, headerH);
            gc.setStroke(themeColor);
            gc.setLineWidth(1);
            gc.strokeLine(0, headerH, w, headerH);

            // Resolve Sound Name
            String soundName = "INIT SOUND";
            Sound s = SoundRepository.getInstance().getSoundByBankAndProgram(active.getBankProperty().get(), active.getProgramProperty().get());
            if (s != null) soundName = s.getName();

            gc.setFill(themeColor);
            gc.setFont(Font.font("Geist Mono", FontWeight.BOLD, 14));
            String headerText = String.format("EDITING PART %02d | %s | MIDI CH %d", 
                active.getPartIndex() + 1, soundName.toUpperCase(), active.getChannelProperty().get());
            if (invalid) headerText += " [INVALID RANGE]";
            gc.fillText(headerText, 10, 22);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Geist Mono", FontWeight.BOLD, 12));
            String rangeText = String.format("%s - %s (%d keys)", 
                MidiUtils.noteName(active.getKeyLow()), 
                MidiUtils.noteName(active.getKeyHigh()),
                Math.abs(active.getKeyHigh() - active.getKeyLow()) + 1);
            gc.fillText(rangeText, w - 220, 22);

            double xLow = getXForNote(active.getKeyLow());
            double xHigh = getXForNote(active.getKeyHigh());
            gc.setStroke(themeColor);
            gc.setLineWidth(3);
            gc.strokeLine(xLow, pianoTop, xLow, pianoTop + pianoH);
            gc.strokeLine(xHigh, pianoTop, xHigh, pianoTop + pianoH);
            gc.setFill(Color.WHITE);
            gc.fillOval(xLow - 4, pianoTop + pianoH/2 - 4, 8, 8);
            gc.fillOval(xHigh - 4, pianoTop + pianoH/2 - 4, 8, 8);
        }

        double mapTop = pianoTop + pianoH + 15;
        double laneH = (h - mapTop - 5) / 16.0;
        if (getSkinnable().getParts() != null) {
            for (int i = 0; i < 16; i++) {
                MultiPart p = getSkinnable().getParts().get(i);
                double x1 = getXForNote(p.getKeyLow());
                double x2 = getXForNote(p.getKeyHigh());
                if (x2 >= x1) {
                    gc.setFill(p.isActive() ? COLOR_VALID : Color.web("#444444", 0.5));
                    gc.fillRect(x1, mapTop + (i * laneH), Math.max(2, x2 - x1), laneH - 1);
                }
            }
        }
    }

    private void drawKeyboard(GraphicsContext gc, double top, double kw, double wh, double bh, double bw, MultiPart active) {
        int low = active != null ? active.getKeyLow() : -1;
        int high = active != null ? active.getKeyHigh() : -1;
        gc.setStroke(COLOR_KEY_BORDER);
        int wkCount = 0;
        for (int i = 0; i < NUM_KEYS; i++) {
            if (!isBlackKey(i)) {
                double x = wkCount * kw;
                boolean inRange = (i >= low && i <= high);
                gc.setFill(inRange ? Color.web("#00FFFF", 0.3) : COLOR_WHITE_KEY);
                gc.fillRect(x, top, kw, wh);
                gc.strokeRect(x, top, kw, wh);
                if (i % 12 == 0) {
                    gc.setFill(Color.web("#808080"));
                    gc.setFont(Font.font("Geist Mono", 8));
                    gc.fillText("C" + ((i/12)-2), x + 2, top + wh - 5);
                }
                wkCount++;
            }
        }
        wkCount = 0;
        for (int i = 0; i < NUM_KEYS; i++) {
            if (isBlackKey(i)) {
                double x = wkCount * kw - (bw / 2);
                boolean inRange = (i >= low && i <= high);
                gc.setFill(inRange ? Color.web("#008888") : COLOR_BLACK_KEY);
                gc.fillRect(x, top, bw, bh);
            } else {
                wkCount++;
            }
        }
    }

    private boolean isBlackKey(int midi) {
        int n = midi % 12;
        return n == 1 || n == 3 || n == 6 || n == 8 || n == 10;
    }
}
