package net.mikolas.lyra.ui.control;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;

public class EnvelopeEditorSkin extends SkinBase<EnvelopeEditor> {

    private final Canvas canvas = new Canvas();
    private int draggingNode = -1; // -1: none, 0: A, 1: D, 2: S, 3: R

    public EnvelopeEditorSkin(EnvelopeEditor control) {
        super(control);
        getChildren().add(canvas);

        canvas.widthProperty().bind(control.widthProperty());
        canvas.heightProperty().bind(control.heightProperty());

        canvas.setOnMousePressed(e -> {
            draggingNode = findNodeAt(e.getX(), e.getY());
        });

        canvas.setOnMouseDragged(e -> {
            if (draggingNode != -1) {
                updateValueFromMouse(draggingNode, e.getX(), e.getY());
            }
        });

        canvas.setOnMouseReleased(e -> draggingNode = -1);

        // Listeners for redraw
        control.attackProperty().addListener(o -> draw());
        control.decayProperty().addListener(o -> draw());
        control.sustainProperty().addListener(o -> draw());
        control.releaseProperty().addListener(o -> draw());
        control.widthProperty().addListener(o -> draw());
        control.heightProperty().addListener(o -> draw());

        draw();
    }

    private int findNodeAt(double x, double y) {
        Point2D[] points = getPoints();
        for (int i = 0; i < points.length; i++) {
            if (points[i].distance(x, y) < 15) return i;
        }
        return -1;
    }

    private void updateValueFromMouse(int node, double x, double y) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        EnvelopeEditor ctrl = getSkinnable();

        switch (node) {
            case 0 -> ctrl.setAttack(Math.clamp((x / (w / 4.0)) * 127.0, 0, 127));
            case 1 -> ctrl.setDecay(Math.clamp(((x - w/4.0) / (w / 4.0)) * 127.0, 0, 127));
            case 2 -> ctrl.setSustain(Math.clamp((1.0 - (y / h)) * 127.0, 0, 127));
            case 3 -> ctrl.setRelease(Math.clamp(((x - 3*w/4.0) / (w / 4.0)) * 127.0, 0, 127));
        }
    }

    private Point2D[] getPoints() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        EnvelopeEditor ctrl = getSkinnable();

        double xA = (ctrl.getAttack() / 127.0) * (w / 4.0);
        double yA = 0;

        double xD = w/4.0 + (ctrl.getDecay() / 127.0) * (w / 4.0);
        double yD = h - (ctrl.getSustain() / 127.0) * h;

        double xS = 3 * w / 4.0;
        double yS = yD;

        double xR = 3 * w / 4.0 + (ctrl.getRelease() / 127.0) * (w / 4.0);
        double yR = h;

        return new Point2D[] {
            new Point2D(xA, yA),
            new Point2D(xD, yD),
            new Point2D(xS, yS),
            new Point2D(xR, yR)
        };
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        // Draw grid/bg
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1);
        for (int i = 1; i < 4; i++) {
            gc.strokeLine(i * w / 4.0, 0, i * w / 4.0, h);
        }

        Point2D[] pts = getPoints();

        // Draw Envelope Path
        gc.setStroke(Color.web("#00ff00"));
        gc.setLineWidth(2);
        gc.beginPath();
        gc.moveTo(0, h);
        gc.lineTo(pts[0].getX(), pts[0].getY()); // Attack
        gc.lineTo(pts[1].getX(), pts[1].getY()); // Decay
        gc.lineTo(pts[2].getX(), pts[2].getY()); // Sustain start
        gc.lineTo(pts[3].getX(), pts[3].getY()); // Release
        gc.stroke();

        // Draw area fill
        gc.setFill(Color.web("#00ff00", 0.2));
        gc.beginPath();
        gc.moveTo(0, h);
        gc.lineTo(pts[0].getX(), pts[0].getY());
        gc.lineTo(pts[1].getX(), pts[1].getY());
        gc.lineTo(pts[2].getX(), pts[2].getY());
        gc.lineTo(pts[3].getX(), pts[3].getY());
        gc.lineTo(pts[3].getX(), h);
        gc.closePath();
        gc.fill();

        // Draw Nodes
        gc.setFill(Color.WHITE);
        for (int i = 0; i < pts.length; i++) {
            double r = (i == draggingNode) ? 6 : 4;
            gc.fillOval(pts[i].getX() - r, pts[i].getY() - r, r * 2, r * 2);
        }
    }
}
