package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import net.mikolas.lyra.model.Harmonic;
import net.mikolas.lyra.model.Keyframe;
import net.mikolas.lyra.model.Wavetable;

public class WavetableEditorController {

    @FXML private ScrollPane timelineScrollPane;
    @FXML private Canvas timelineCanvas;
    @FXML private Canvas miniViewCanvas;
    
    @FXML private TabPane editorTabPane;
    
    @FXML private Canvas full3DCanvas;
    @FXML private Slider volumeSlider;
    @FXML private Pane pianoContainer;
    
    @FXML private TextField nameField;
    @FXML private ComboBox<String> slotCombo;
    @FXML private Canvas currentWaveCanvas;
    @FXML private Label waveInfoLabel;
    
    @FXML private Label statusLabel;

    @FXML private Canvas waveEditCanvas;
    @FXML private ToggleGroup morphGroup;
    @FXML private RadioButton constantRadio, curveRadio, translateRadio, spectralRadio;
    @FXML private ComboBox<String> curveFuncCombo;
    @FXML private Spinner<Integer> translateSpinner;
    @FXML private Button applyMorphBtn;

    // Tool Buttons
    @FXML private Button normalizeBtn, smoothBtn, invertBtn, reverseBtn;
    @FXML private ToggleButton freehandModeBtn, lineModeBtn;
    
    // Drawing Mode
    private enum DrawMode { FREEHAND, LINE }
    private DrawMode drawMode = DrawMode.FREEHAND;
    private double lineStartX = -1, lineStartY = -1;

    private Wavetable wavetable = Wavetable.createNew();
    private int selectedWaveIndex = 0;
    private int hoveredWaveIndex = -1;

    // Drawing & Interaction State
    private boolean isDrawing = false;
    private boolean isSliding = false;
    private int originalSlideIndex = -1;
    private Keyframe slidingKeyframe = null;
    private double lastMouseX, lastMouseY;
    
    // Interactive 3D Parameters (Defaults from Spec)
    private double viewScale = 1.0;
    private double viewShearY = 0.23;
    private double viewScaleY = 0.55;

    // Projection Constants from Spec
    private static final double NORM_Y = 1048576.0; // 2^20
    private final net.mikolas.lyra.service.WaveToolService waveToolService = new net.mikolas.lyra.service.WaveToolService();
    private final net.mikolas.lyra.service.HarmonicsService harmonicsService = new net.mikolas.lyra.service.HarmonicsService();

    @FXML private HBox harmonicsContainer;
    @FXML private ComboBox<String> harmonicsWaveTypeCombo;
    @FXML private CheckBox addHarmonicsChk;
    
    @FXML private Canvas audioPreviewCanvas;
    @FXML private ListView<String> audioFileListView;
    @FXML private Spinner<Integer> audioOffsetSpinner;
    @FXML private Label audioSelectionLabel;
    @FXML private ListView<Wavetable> localWavetableList;
    @FXML private ListView<Wavetable> factoryWavetableList;
    @FXML private VBox wavetableDock;
    
    private final Slider[] harmonicSliders = new Slider[50];
    private float[] loadedAudioData;
    private int audioSampleRate;

    @FXML
    public void initialize() {
        setupSlotCombo();
        setupCanvasResizing();
        setupCurveFuncCombo();
        setupSpinners();
        setupDrawingInteraction();
        setupTimelineInteraction();
        setup3DInteraction();
        setupMorphBindings();
        setupHarmonicsEditor();
        setupAudioImport();
        setupLibraryDock();
        setupBindings();
        
        // Enforce 14-character limit for name
        nameField.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.length() > 14) {
                nameField.setText(val.substring(0, 14));
            }
        });

        wavetable.slotProperty().addListener((obs, old, val) -> {
            slotCombo.getSelectionModel().select(val.intValue() - 80);
        });
        
        // Initial render
        javafx.application.Platform.runLater(() -> {
            renderAll();
        });
    }

    private void setupAudioImport() {
        audioOffsetSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000000, 0));
        audioOffsetSpinner.valueProperty().addListener((obs, old, val) -> renderAudioPreview());
        
        audioPreviewCanvas.widthProperty().bind(((Pane)audioPreviewCanvas.getParent()).widthProperty());
        audioPreviewCanvas.heightProperty().bind(((Pane)audioPreviewCanvas.getParent()).heightProperty());
        audioPreviewCanvas.widthProperty().addListener(e -> renderAudioPreview());
        audioPreviewCanvas.heightProperty().addListener(e -> renderAudioPreview());
    }

    private void setupLibraryDock() {
        net.mikolas.lyra.db.WavetableRepository repo = net.mikolas.lyra.db.WavetableRepository.getInstance();
        
        System.out.println("setupLibraryDock - total wavetables: " + repo.getAllWavetables().size());
        
        javafx.collections.transformation.FilteredList<Wavetable> localItems = new javafx.collections.transformation.FilteredList<>(repo.getAllWavetables(), wt -> !wt.isFactory());
        javafx.collections.transformation.FilteredList<Wavetable> factoryItems = new javafx.collections.transformation.FilteredList<>(repo.getAllWavetables(), Wavetable::isFactory);
        
        System.out.println("Local wavetables: " + localItems.size());
        System.out.println("Factory wavetables: " + factoryItems.size());
        
        localWavetableList.setItems(localItems);
        factoryWavetableList.setItems(factoryItems);
        
        System.out.println("ListView items set - local: " + localWavetableList.getItems().size() + ", factory: " + factoryWavetableList.getItems().size());
        System.out.println("First 5 factory wavetables:");
        for (int i = 0; i < Math.min(5, factoryItems.size()); i++) {
            Wavetable wt = factoryItems.get(i);
            System.out.println("  " + i + ": " + wt.getName() + " (slot " + wt.getSlot() + ")");
        }
        
        // Check if wavetableDock is visible
        javafx.application.Platform.runLater(() -> {
            System.out.println("wavetableDock visible: " + wavetableDock.isVisible());
            System.out.println("wavetableDock width: " + wavetableDock.getWidth());
            System.out.println("localWavetableList visible: " + localWavetableList.isVisible());
            System.out.println("factoryWavetableList visible: " + factoryWavetableList.isVisible());
        });

        // Set cell factories to show names
        localWavetableList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Wavetable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        factoryWavetableList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Wavetable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        localWavetableList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            System.out.println("Local list selection changed: " + (val != null ? val.getName() : "null"));
            if (val != null) {
                factoryWavetableList.getSelectionModel().clearSelection();
                loadWavetable(val);
            }
        });

        factoryWavetableList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            System.out.println("Factory list selection changed: " + (val != null ? val.getName() : "null"));
            if (val != null) {
                localWavetableList.getSelectionModel().clearSelection();
                loadWavetable(val);
            }
        });
    }

    private void loadWavetable(Wavetable wt) {
        if (wt == null) return;
        System.out.println("=== Loading Wavetable ===");
        System.out.println("Name: " + wt.getName());
        System.out.println("Slot: " + wt.getSlot());
        System.out.println("IsFactory: " + wt.isFactory());
        System.out.println("BinaryData length: " + (wt.getBinaryData() != null ? wt.getBinaryData().length : "null"));
        System.out.println("Keyframes count: " + wt.getKeyframes().size());
        
        wavetable = wt;
        wavetable.loadFromData();
        
        System.out.println("After loadFromData - wave 0 sample[0]: " + wavetable.getBouncedWaves()[0][0]);
        System.out.println("After loadFromData - wave 0 sample[64]: " + wavetable.getBouncedWaves()[0][64]);
        System.out.println("After loadFromData - wave 32 sample[0]: " + wavetable.getBouncedWaves()[32][0]);
        System.out.println("After loadFromData - wave 63 sample[0]: " + wavetable.getBouncedWaves()[63][0]);
        
        setupBindings();
        renderAll();
        
        System.out.println("Canvas sizes - timeline: " + timelineCanvas.getWidth() + "x" + timelineCanvas.getHeight());
        System.out.println("Canvas sizes - 3D: " + full3DCanvas.getWidth() + "x" + full3DCanvas.getHeight());
        System.out.println("=========================");
    }

    @FXML
    private void handleBrowseAudio() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Open Audio File");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Audio Files", "*.wav")
        );
        java.io.File file = fileChooser.showOpenDialog(audioPreviewCanvas.getScene().getWindow());
        if (file != null) {
            loadAudioFile(file);
        }
    }

    private void loadAudioFile(java.io.File file) {
        try {
            javax.sound.sampled.AudioInputStream ais = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            javax.sound.sampled.AudioFormat format = ais.getFormat();
            
            byte[] bytes = ais.readAllBytes();
            int numSamples = bytes.length / format.getFrameSize();
            loadedAudioData = new float[numSamples];
            
            // Simple 16-bit PCM mono loading
            java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < numSamples; i++) {
                if (format.getSampleSizeInBits() == 16) {
                    loadedAudioData[i] = bb.getShort() / 32768.0f;
                } else {
                    loadedAudioData[i] = (bb.get() & 0xFF) / 128.0f - 1.0f;
                }
            }
            
            audioSampleRate = (int) format.getSampleRate();
            audioFileListView.getItems().add(file.getName());
            audioFileListView.getSelectionModel().select(file.getName());
            
            renderAudioPreview();
            statusLabel.setText("Loaded: " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Error loading audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAudioPreview() {
        GraphicsContext gc = audioPreviewCanvas.getGraphicsContext2D();
        double w = audioPreviewCanvas.getWidth();
        double h = audioPreviewCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        
        if (loadedAudioData == null) return;
        
        int offset = audioOffsetSpinner.getValue();
        gc.setStroke(Color.web("#00FF00"));
        gc.setLineWidth(1.0);
        
        double xStep = w / 8192.0; // Show exactly 64 chunks of 128 samples
        gc.beginPath();
        for (int i = 0; i < 8192; i++) {
            int idx = offset + i;
            if (idx >= loadedAudioData.length) break;
            
            double val = loadedAudioData[idx];
            double px = i * xStep;
            double py = (h/2) - (val * (h/2.2));
            if (i == 0) gc.moveTo(px, py);
            else gc.lineTo(px, py);
        }
        gc.stroke();
        
        // Draw chunk markers (every 128 samples)
        gc.setStroke(Color.web("#FFFFFF", 0.3));
        for (int i = 0; i <= 64; i++) {
            double gx = i * 128 * xStep;
            gc.strokeLine(gx, 0, gx, h);
        }
    }

    @FXML
    private void handleImportFullAudio() {
        if (loadedAudioData == null) return;
        
        int offset = audioOffsetSpinner.getValue();
        wavetable.getKeyframes().clear();
        
        for (int i = 0; i < 64; i++) {
            Keyframe kf = new Keyframe(i);
            for (int s = 0; s < 128; s++) {
                int idx = offset + (i * 128) + s;
                float val = (idx < loadedAudioData.length) ? loadedAudioData[idx] : 0;
                kf.setSample(s, (int) (val * NORM_Y));
            }
            wavetable.getKeyframes().add(kf);
        }
        
        wavetable.markDirty();
        renderAll();
        statusLabel.setText("Imported 64 waves from audio.");
    }

    @FXML
    private void handleImportSelectedAudio() {
        // For now, same as full import starting at selectedWaveIndex
        if (loadedAudioData == null) return;
        
        int offset = audioOffsetSpinner.getValue();
        ensureKeyframeAtSelected();
        Keyframe kf = getSelectedKeyframe();
        
        if (kf != null) {
            for (int s = 0; s < 128; s++) {
                int idx = offset + s;
                float val = (idx < loadedAudioData.length) ? loadedAudioData[idx] : 0;
                kf.setSample(s, (int) (val * NORM_Y));
            }
            wavetable.markDirty();
            renderAll();
            statusLabel.setText("Imported wave from audio offset.");
        }
    }

    private void setupHarmonicsEditor() {
        harmonicsWaveTypeCombo.getItems().addAll("Sine", "Square", "Triangle", "Saw", "InvSaw");
        harmonicsWaveTypeCombo.getSelectionModel().selectFirst();

        for (int i = 0; i < 50; i++) {
            Slider slider = new Slider(0, 100, 0);
            slider.setOrientation(javafx.geometry.Orientation.VERTICAL);
            slider.setShowTickMarks(false);
            slider.setShowTickLabels(false);
            
            // Highlight fundamental (slider 0)
            if (i == 0) {
                slider.setStyle("-fx-control-inner-background: #008888;");
            }
            
            int hIndex = i;
            slider.valueProperty().addListener((obs, old, val) -> {
                // Real-time preview could be added here
            });
            
            harmonicSliders[i] = slider;
            harmonicsContainer.getChildren().add(slider);
        }
    }

    @FXML
    private void handleApplyHarmonics() {
        ensureKeyframeAtSelected();
        Keyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        
        // Clear existing harmonics
        kf.getHarmonics().clear();
        
        // Parse wave type
        String shapeStr = harmonicsWaveTypeCombo.getValue();
        Harmonic.WaveType waveType = harmonicsService.parseWaveType(shapeStr);
        
        // Collect harmonics from sliders
        for (int h = 0; h < 50; h++) {
            double amp = harmonicSliders[h].getValue() / 100.0;
            if (amp >= 0.01) {
                kf.getHarmonics().add(new Harmonic(h + 1, waveType, amp));
            }
        }
        
        // Apply harmonics
        boolean additive = addHarmonicsChk.isSelected();
        int[] result = harmonicsService.applyHarmonics(kf.getSamples(), kf.getHarmonics(), additive);
        
        // Update keyframe samples
        for (int i = 0; i < 128; i++) {
            kf.setSample(i, result[i]);
        }
        
        wavetable.markDirty();
        renderAll();
        statusLabel.setText("Applied " + kf.getHarmonics().size() + " harmonics");
    }

    @FXML
    private void handleResetHarmonics() {
        for (Slider s : harmonicSliders) s.setValue(0);
    }

    private void setupMorphBindings() {
        // Morph Type selection
        constantRadio.setOnAction(e -> updateSelectedMorphMode(Keyframe.TransformMode.CONSTANT));
        curveRadio.setOnAction(e -> updateSelectedMorphMode(Keyframe.TransformMode.CURVE));
        translateRadio.setOnAction(e -> updateSelectedMorphMode(Keyframe.TransformMode.TRANSLATE));
        spectralRadio.setOnAction(e -> updateSelectedMorphMode(Keyframe.TransformMode.SPECTRAL));

        curveFuncCombo.setOnAction(e -> {
            Keyframe kf = getSelectedKeyframe();
            if (kf != null) {
                kf.setCurveFunction(curveFuncCombo.getValue());
                wavetable.markDirty();
                renderAll();
            }
        });

        translateSpinner.valueProperty().addListener((obs, old, val) -> {
            Keyframe kf = getSelectedKeyframe();
            if (kf != null) {
                kf.setTranslateOffset(val);
                wavetable.markDirty();
                renderAll();
            }
        });
    }

    private void updateSelectedMorphMode(Keyframe.TransformMode mode) {
        Keyframe kf = getSelectedKeyframe();
        if (kf != null) {
            kf.setTransformMode(mode);
            wavetable.markDirty();
            renderAll();
        }
    }
    
    @FXML
    private void handleApplyMorph() {
        // Morph mode is already set by radio buttons
        // This button triggers bounce and provides user feedback
        wavetable.markDirty();
        wavetable.bounce();
        renderAll();
        
        Keyframe kf = getSelectedKeyframe();
        if (kf != null) {
            statusLabel.setText("Applied " + kf.getTransformMode() + " morph from wave " + selectedWaveIndex);
        }
    }

    private Keyframe getSelectedKeyframe() {
        return wavetable.getKeyframes().stream()
                .filter(k -> k.getIndex() == selectedWaveIndex)
                .findFirst().orElse(null);
    }

    @FXML
    private void handleNormalize() {
        Keyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        
        int[] result = waveToolService.normalize(kf.getSamples(), (int) NORM_Y);
        for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        
        wavetable.markDirty();
        renderAll();
    }

    @FXML
    private void handleSmooth() {
        Keyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        
        int[] result = waveToolService.smooth(kf.getSamples());
        for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        
        wavetable.markDirty();
        renderAll();
    }

    @FXML
    private void handleInvert() {
        Keyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        
        int[] result = waveToolService.invert(kf.getSamples());
        for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        
        wavetable.markDirty();
        renderAll();
    }

    @FXML
    private void handleReverse() {
        Keyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        
        int[] result = waveToolService.reverse(kf.getSamples());
        for (int i = 0; i < 128; i++) kf.setSample(i, result[i]);
        
        wavetable.markDirty();
        renderAll();
    }
    
    @FXML
    private void handleFreehandMode() {
        drawMode = DrawMode.FREEHAND;
        freehandModeBtn.setSelected(true);
        lineModeBtn.setSelected(false);
        lineStartX = -1;
        lineStartY = -1;
        statusLabel.setText("Freehand mode");
    }
    
    @FXML
    private void handleLineMode() {
        drawMode = DrawMode.LINE;
        freehandModeBtn.setSelected(false);
        lineModeBtn.setSelected(true);
        lineStartX = -1;
        lineStartY = -1;
        statusLabel.setText("Line mode: Click start point");
    }

    private void setupTimelineInteraction() {
        timelineCanvas.setOnMouseClicked(e -> {
            int index = (int) (e.getX() / 128.0);
            if (index >= 0 && index < 64) {
                selectedWaveIndex = index;
                renderAll();
            }
        });
    }

    private void setup3DInteraction() {
        full3DCanvas.setOnMouseMoved(e -> {
            int oldHover = hoveredWaveIndex;
            hoveredWaveIndex = getWaveIndexAt(e.getX(), e.getY());
            if (hoveredWaveIndex != oldHover) {
                render3D();
                renderCurrentWave();
            }
            // Set cursor: SizeBDiag if hovering over a keyframe (to indicate sliding)
            boolean isKeyframe = wavetable.getKeyframes().stream().anyMatch(kf -> kf.getIndex() == hoveredWaveIndex);
            if (isKeyframe) {
                full3DCanvas.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else {
                full3DCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        full3DCanvas.setOnMouseExited(e -> {
            hoveredWaveIndex = -1;
            render3D();
            renderCurrentWave();
        });

        full3DCanvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            
            if (hoveredWaveIndex != -1) {
                selectedWaveIndex = hoveredWaveIndex;
                
                // Check if we are starting a slide
                slidingKeyframe = wavetable.getKeyframes().stream()
                    .filter(kf -> kf.getIndex() == selectedWaveIndex)
                    .findFirst().orElse(null);
                
                if (slidingKeyframe != null) {
                    isSliding = true;
                    originalSlideIndex = selectedWaveIndex;
                }
                
                renderAll();
            }
        });

        full3DCanvas.setOnMouseDragged(e -> {
            if (isSliding && slidingKeyframe != null) {
                double w = full3DCanvas.getWidth();
                double h = full3DCanvas.getHeight();
                
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                
                // Map 2D mouse move to 3D wave index change.
                // Diagonal move (down-right or up-left) maps to index change.
                // Constants based on project() offsets.
                int indexDelta = (int) ((dx / (w * 0.05)) - (dy / (h * 0.08)));
                int newIndex = Math.clamp(originalSlideIndex + indexDelta, 0, 63);
                
                if (newIndex != slidingKeyframe.getIndex()) {
                    // Basic "Push" logic: remove if another exists? 
                    // No, let's just update index for now.
                    slidingKeyframe.setIndex(newIndex);
                    selectedWaveIndex = newIndex;
                    wavetable.markDirty();
                    renderAll();
                }
            }
        });

        full3DCanvas.setOnMouseReleased(e -> {
            if (isSliding) {
                isSliding = false;
                slidingKeyframe = null;
                wavetable.markDirty();
                renderAll();
            }
        });

        full3DCanvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && hoveredWaveIndex != -1) {
                ensureKeyframeAtSelected();
                editorTabPane.getSelectionModel().select(1); // Switch to Wave Edit tab
            }
        });

        full3DCanvas.setOnScroll(e -> {
            viewScale = Math.clamp(viewScale + (e.getDeltaY() * 0.001), 0.5, 3.0);
            render3D();
        });
    }

    private int getWaveIndexAt(double x, double y) {
        double w = full3DCanvas.getWidth();
        double h = full3DCanvas.getHeight();
        
        // Iterate back-to-front to find which wave slice the mouse is over.
        for (int i = 0; i < 64; i++) {
            double[] p0 = project(0, -1.0, i, w, h);   // Bottom-left of wave slice
            double[] p1 = project(127, -1.0, i, w, h); // Bottom-right
            double[] pTop = project(0, 1.0, i, w, h);  // Top-left
            
            // Check bounding box of the slice's spatial area in the axonometric view
            if (x >= p0[0] && x <= p1[0] && y <= p0[1] && y >= pTop[1]) {
                return i;
            }
        }
        return -1;
    }

    private void setupCurveFuncCombo() {
        curveFuncCombo.getItems().addAll("Linear", "InQuad", "OutQuad", "InOutQuad", "Sine", "Exp");
        curveFuncCombo.getSelectionModel().selectFirst();
    }

    private void setupSpinners() {
        translateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-128, 128, 0));
    }

    private void setupDrawingInteraction() {
        waveEditCanvas.setOnMousePressed(e -> {
            if (drawMode == DrawMode.LINE) {
                if (lineStartX < 0) {
                    // First click - set start point
                    lineStartX = e.getX();
                    lineStartY = e.getY();
                    statusLabel.setText("Line mode: Click end point");
                } else {
                    // Second click - draw line
                    ensureKeyframeAtSelected();
                    drawLine(lineStartX, lineStartY, e.getX(), e.getY());
                    lineStartX = -1;
                    lineStartY = -1;
                    wavetable.markDirty();
                    renderAll();
                    statusLabel.setText("Line drawn");
                }
            } else {
                // Freehand mode
                isDrawing = true;
                ensureKeyframeAtSelected();
                handleDraw(e.getX(), e.getY());
            }
        });
        
        waveEditCanvas.setOnMouseDragged(e -> {
            if (isDrawing && drawMode == DrawMode.FREEHAND) {
                handleDraw(e.getX(), e.getY());
            }
        });
        
        waveEditCanvas.setOnMouseReleased(e -> {
            if (drawMode == DrawMode.FREEHAND) {
                isDrawing = false;
                wavetable.markDirty();
                renderAll();
            }
        });
        
        waveEditCanvas.setOnMouseMoved(e -> {
            if (drawMode == DrawMode.LINE && lineStartX >= 0) {
                // Show preview line
                renderEditCanvas();
                GraphicsContext gc = waveEditCanvas.getGraphicsContext2D();
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1);
                gc.strokeLine(lineStartX, lineStartY, e.getX(), e.getY());
            }
        });
    }

    private void ensureKeyframeAtSelected() {
        boolean exists = wavetable.getKeyframes().stream()
                .anyMatch(kf -> kf.getIndex() == selectedWaveIndex);
        if (!exists) {
            wavetable.getKeyframes().add(new Keyframe(selectedWaveIndex));
        }
    }

    private void setupSlotCombo() {
        slotCombo.getItems().clear();
        slotCombo.getItems().add("Unassigned (Library Only)");
        // Factory slots
        for (int i = 0; i <= 72; i++) {
            slotCombo.getItems().add(String.format("%d: Factory/Sys", i));
        }
        // User slots
        for (int i = 80; i <= 118; i++) {
            slotCombo.getItems().add(String.format("%d: User Wt %d", i, i - 79));
        }
        
        updateSlotSelection();
        
        slotCombo.setOnAction(e -> {
            int index = slotCombo.getSelectionModel().getSelectedIndex();
            if (index <= 0) {
                wavetable.setSlot(null);
                return;
            }
            
            int actualSlot;
            if (index <= 73) { // 1 (Unassigned) + 73 (0-72 Factory)
                actualSlot = index - 1;
            } else {
                actualSlot = (index - 74) + 80;
            }
            wavetable.setSlot(actualSlot);
        });
    }

    private void updateSlotSelection() {
        Integer s = wavetable.getSlot();
        if (s == null) {
            slotCombo.getSelectionModel().select(0);
        } else if (s >= 0 && s <= 72) {
            slotCombo.getSelectionModel().select(s + 1);
        } else if (s >= 80 && s <= 118) {
            slotCombo.getSelectionModel().select(s - 80 + 74);
        }
    }

    private void handleDraw(double x, double y) {
        double w = waveEditCanvas.getWidth();
        double h = waveEditCanvas.getHeight();
        
        int sampleIdx = (int) ((x / w) * 128);
        if (sampleIdx < 0 || sampleIdx >= 128) return;
        
        // Convert screen Y to 21-bit sample value
        double normY = (h / 2.0 - y) / (h / 2.5);
        int sampleValue = (int) (normY * NORM_Y);
        
        Keyframe kf = wavetable.getKeyframes().stream()
                .filter(k -> k.getIndex() == selectedWaveIndex)
                .findFirst().orElse(null);
        
        if (kf != null) {
            kf.setSample(sampleIdx, sampleValue);
            renderEditCanvas();
            renderCurrentWave();
        }
    }
    
    private void drawLine(double x1, double y1, double x2, double y2) {
        double w = waveEditCanvas.getWidth();
        double h = waveEditCanvas.getHeight();
        
        int sample1 = (int) ((x1 / w) * 128);
        int sample2 = (int) ((x2 / w) * 128);
        
        if (sample1 < 0) sample1 = 0;
        if (sample1 >= 128) sample1 = 127;
        if (sample2 < 0) sample2 = 0;
        if (sample2 >= 128) sample2 = 127;
        
        // Convert screen Y to 21-bit sample values
        double normY1 = (h / 2.0 - y1) / (h / 2.5);
        double normY2 = (h / 2.0 - y2) / (h / 2.5);
        int value1 = (int) (normY1 * NORM_Y);
        int value2 = (int) (normY2 * NORM_Y);
        
        Keyframe kf = wavetable.getKeyframes().stream()
                .filter(k -> k.getIndex() == selectedWaveIndex)
                .findFirst().orElse(null);
        
        if (kf == null) return;
        
        // Draw line between two points
        int start = Math.min(sample1, sample2);
        int end = Math.max(sample1, sample2);
        
        for (int i = start; i <= end; i++) {
            double ratio = (double)(i - sample1) / (sample2 - sample1);
            int value = (int)(value1 + ratio * (value2 - value1));
            kf.setSample(i, value);
        }
        
        renderEditCanvas();
        renderCurrentWave();
    }

    private void setupCanvasResizing() {
        full3DCanvas.widthProperty().bind(((Pane)full3DCanvas.getParent()).widthProperty());
        full3DCanvas.heightProperty().bind(((Pane)full3DCanvas.getParent()).heightProperty());
        
        waveEditCanvas.widthProperty().bind(((Pane)waveEditCanvas.getParent()).widthProperty());
        waveEditCanvas.heightProperty().bind(((Pane)waveEditCanvas.getParent()).heightProperty());

        full3DCanvas.widthProperty().addListener(e -> render3D());
        full3DCanvas.heightProperty().addListener(e -> render3D());
        waveEditCanvas.widthProperty().addListener(e -> renderEditCanvas());
        waveEditCanvas.heightProperty().addListener(e -> renderEditCanvas());
    }

    private void renderAll() {
        System.out.println("renderAll() called");
        wavetable.bounce();
        System.out.println("After bounce - first wave sample[0]: " + wavetable.getBouncedWaves()[0][0]);
        updateMorphUI();
        renderTimeline();
        render3D();
        renderCurrentWave();
        renderEditCanvas();
        System.out.println("renderAll() complete");
    }

    private void updateMorphUI() {
        Keyframe kf = getSelectedKeyframe();
        if (kf != null) {
            // Block signals during sync
            constantRadio.setOnAction(null);
            curveRadio.setOnAction(null);
            translateRadio.setOnAction(null);
            spectralRadio.setOnAction(null);
            curveFuncCombo.setOnAction(null);

            switch (kf.getTransformMode()) {
                case CONSTANT -> constantRadio.setSelected(true);
                case CURVE -> curveRadio.setSelected(true);
                case TRANSLATE -> translateRadio.setSelected(true);
                case SPECTRAL -> spectralRadio.setSelected(true);
            }
            curveFuncCombo.setValue(kf.getCurveFunction());
            translateSpinner.getValueFactory().setValue(kf.getTranslateOffset());

            // Restore signals
            setupMorphBindings();
        }
    }

    /**
     * Renders the 2D Waveform Editor canvas.
     */
    private void renderEditCanvas() {
        GraphicsContext gc = waveEditCanvas.getGraphicsContext2D();
        double w = waveEditCanvas.getWidth();
        double h = waveEditCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        
        // Draw Grid
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1.0);
        gc.strokeLine(0, h/2, w, h/2); // Zero line
        for (int i = 1; i < 8; i++) {
            double gx = (i / 8.0) * w;
            gc.strokeLine(gx, 0, gx, h);
        }

        // Get the keyframe if it exists, otherwise use bounced wave
        Keyframe kf = wavetable.getKeyframes().stream()
                .filter(k -> k.getIndex() == selectedWaveIndex)
                .findFirst().orElse(null);
        
        int[] samples = (kf != null) ? kf.getSamples() : wavetable.getWave(selectedWaveIndex);
        
        gc.setStroke(Color.web("#00FFFF"));
        gc.setLineWidth(2.5);
        gc.beginPath();
        for (int s = 0; s < 128; s++) {
            double val = samples[s] / NORM_Y;
            double px = (s / 128.0) * w;
            double py = (h/2) - (val * (h/2.5));
            if (s == 0) gc.moveTo(px, py);
            else gc.lineTo(px, py);
        }
        gc.stroke();
    }

    @FXML private void handleNew() { 
        wavetable = Wavetable.createNew(); 
        setupBindings();
        renderAll(); 
    }
    
    @FXML private void handleOpen() {
        net.mikolas.lyra.db.WavetableRepository repo = net.mikolas.lyra.db.WavetableRepository.getInstance();
        ChoiceDialog<Wavetable> dialog = new ChoiceDialog<>(null, repo.getAllWavetables());
        dialog.setTitle("Open Wavetable");
        dialog.setHeaderText("Select a wavetable from your library");
        dialog.setContentText("Wavetable:");
        
        dialog.showAndWait().ifPresent(selected -> {
            wavetable = selected;
            wavetable.loadFromData();
            nameField.textProperty().unbindBidirectional(oldWavetableNameProperty); // Logic needed to handle re-binding
            setupBindings(); 
            renderAll();
        });
    }

    private Property<String> oldWavetableNameProperty;
    
    private void setupBindings() {
        if (oldWavetableNameProperty != null) {
            nameField.textProperty().unbindBidirectional(oldWavetableNameProperty);
        }
        nameField.textProperty().bindBidirectional(wavetable.nameProperty());
        oldWavetableNameProperty = wavetable.nameProperty();
        
        updateSlotSelection();
    }

    @FXML private void handleSave() {
        try {
            wavetable.prepareForSave();
            net.mikolas.lyra.db.WavetableRepository.getInstance().save(wavetable);
            statusLabel.setText("Saved: " + wavetable.getName());
        } catch (Exception e) {
            statusLabel.setText("Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleSaveAs() {
        // Simple implementation: clone and save
        Wavetable clone = new Wavetable();
        clone.setName(wavetable.getName() + " Copy");
        clone.setSlot(wavetable.getSlot());
        wavetable.prepareForSave(); // Use current binary data
        // TODO: Deep clone keyframes instead of relying on binaryData
        handleSave();
    }
    @FXML private void handleImportAudio() { /* TODO */ }
    @FXML
    private void handleImportMidi() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Import Wavetables from MIDI/SysEx");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("MIDI and SysEx Files", "*.mid", "*.syx", "*.midi")
        );
        
        java.io.File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());
        if (file != null) {
            net.mikolas.lyra.service.WavetableImportService importService = 
                new net.mikolas.lyra.service.WavetableImportService(net.mikolas.lyra.db.WavetableRepository.getInstance());
            
            statusLabel.setText("Importing wavetables from " + file.getName() + "...");
            
            javafx.concurrent.Task<net.mikolas.lyra.service.WavetableImportService.ImportResult> task = new javafx.concurrent.Task<>() {
                @Override protected net.mikolas.lyra.service.WavetableImportService.ImportResult call() {
                    return importService.importFromFile(file);
                }
            };
            
            task.setOnSucceeded(e -> {
                net.mikolas.lyra.service.WavetableImportService.ImportResult result = task.getValue();
                statusLabel.setText(result.getSummary());
                if (result.imported() > 0) {
                    // Force refresh lists if necessary, though Repository uses ObservableList
                }
            });
            
            task.setOnFailed(e -> statusLabel.setText("Import failed: " + task.getException().getMessage()));
            new Thread(task).start();
        }
    }

    @FXML private void handleClose() { 
        ((javafx.stage.Stage)statusLabel.getScene().getWindow()).close(); 
    }

    @FXML private void handleUndo() {
        if (wavetable != null && wavetable.canUndo()) {
            wavetable.undo();
            renderAll();
        }
    }
    
    @FXML private void handleRedo() {
        if (wavetable != null && wavetable.canRedo()) {
            wavetable.redo();
            renderAll();
        }
    }
    
    @FXML private void handleCopyWave() { /* TODO */ }
    @FXML private void handlePasteWave() { /* TODO */ }

    @FXML private void handleToggleMiniView() {
        miniViewCanvas.setVisible(!miniViewCanvas.isVisible());
    }

    @FXML private void handleZoomIn() { viewScale = Math.clamp(viewScale + 0.1, 0.5, 3.0); render3D(); }
    @FXML private void handleZoomOut() { viewScale = Math.clamp(viewScale - 0.1, 0.5, 3.0); render3D(); }

    @FXML
    private void handleDump() {
        net.mikolas.lyra.midi.MidiService midi = net.mikolas.lyra.midi.MidiManager.getInstance().getService();
        if (midi == null || !midi.isConnected()) {
            statusLabel.setText("Error: MIDI not connected.");
            return;
        }

        try {
            statusLabel.setText("Dumping wavetable to Blofeld slot " + wavetable.getSlot() + "...");
            java.util.List<net.mikolas.lyra.midi.WavetableDump> messages = wavetable.generateDumpMessages(midi.getDeviceId());
            
            // Run in background to avoid freezing the UI
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override protected Void call() throws Exception {
                    midi.sendWavetableDump(messages);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> statusLabel.setText("Dump complete."));
            task.setOnFailed(e -> statusLabel.setText("Dump failed: " + task.getException().getMessage()));
            new Thread(task).start();
            
        } catch (Exception e) {
            statusLabel.setText("Dump failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddKeyframe() {
        ensureKeyframeAtSelected();
        wavetable.markDirty();
        renderAll();
    }

    @FXML
    private void handleRemoveKeyframe() {
        wavetable.getKeyframes().removeIf(kf -> kf.getIndex() == selectedWaveIndex && selectedWaveIndex != 0);
        wavetable.markDirty();
        renderAll();
    }

    /**
     * Projects a 3D point (s, val, i) to 2D screen coordinates.
     */
    private double[] project(int s, double val, int i, double w, double h) {
        // 1. Base 2D position (0..128, -1..1)
        // Scaled to fit within 55% of width and 35% of height to leave room for depth
        double xBase = (s / 128.0) * w * 0.55 * viewScale; 
        double yBase = -val * (h * 0.35 * viewScale * viewScaleY);

        // 2. Depth Offset (Axonometric)
        // Stagger waves to create depth perception. 
        // Reduced from 10x to 3x to stay within bounds.
        double xOffset = i * (w * 0.005) * 3 * viewScale; 
        double yOffset = -i * (h * 0.008) * 3 * viewScale * viewScaleY;

        // 3. Center in Canvas (Translate)
        double xFinal = xBase + xOffset + (w * 0.12);
        double yFinal = yBase + yOffset + (h * 0.75);
        
        // Apply Shear (Vertical tilt)
        yFinal += xBase * viewShearY;
        
        return new double[]{xFinal, yFinal};
    }

    /**
     * Renders the 3D axonometric view of 64 waves.
     */
    private void render3D() {
        GraphicsContext gc = full3DCanvas.getGraphicsContext2D();
        double w = full3DCanvas.getWidth();
        double h = full3DCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        // 1. Draw Perspective Box (The "Cube")
        gc.setStroke(Color.web("#222222"));
        gc.setLineWidth(1.0);
        double[] p00 = project(0, 0, 0, w, h);
        double[] p01 = project(127, 0, 0, w, h);
        double[] p10 = project(0, 0, 63, w, h);
        double[] p11 = project(127, 0, 63, w, h);
        gc.strokeLine(p00[0], p00[1], p01[0], p01[1]);
        gc.strokeLine(p10[0], p10[1], p11[0], p11[1]);
        gc.strokeLine(p00[0], p00[1], p10[0], p10[1]);
        gc.strokeLine(p01[0], p01[1], p11[0], p11[1]);

        // 2. Draw Motion Lines (every 32 samples)
        gc.setStroke(Color.web("#114444"));
        for (int s : new int[]{0, 32, 64, 96, 127}) {
            gc.beginPath();
            for (int i = 0; i < 64; i++) {
                double[] p = project(s, wavetable.getWave(i)[s] / NORM_Y, i, w, h);
                if (i == 0) gc.moveTo(p[0], p[1]);
                else gc.lineTo(p[0], p[1]);
            }
            gc.stroke();
        }

        // 3. Render waves back-to-front
        for (int i = 63; i >= 0; i--) {
            // Draw visual slice if hovered OR selected
            if (i == hoveredWaveIndex || i == selectedWaveIndex) {
                drawVisualSlice(gc, i, w, h, i == selectedWaveIndex);
            }

            int[] samples = wavetable.getWave(i);
            boolean isKeyframe = false;
            for (Keyframe kf : wavetable.getKeyframes()) {
                if (kf.getIndex() == i) { isKeyframe = true; break; }
            }

            if (i == selectedWaveIndex) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.5);
            } else {
                gc.setStroke(isKeyframe ? Color.CYAN : Color.web("#008888", 0.4));
                gc.setLineWidth(isKeyframe ? 1.5 : 0.8);
            }
            
            gc.beginPath();
            for (int s = 0; s < 128; s++) {
                double[] p = project(s, samples[s] / NORM_Y, i, w, h);
                if (s == 0) gc.moveTo(p[0], p[1]);
                else gc.lineTo(p[0], p[1]);
            }
            gc.stroke();
        }
    }

    private void drawVisualSlice(GraphicsContext gc, int index, double w, double h, boolean isSelected) {
        // Draw a semi-transparent plane at the "floor" level (-1.0)
        double[] p0 = project(0, -1.0, index, w, h);
        double[] p1 = project(127, -1.0, index, w, h);
        double[] p2 = project(127, 1.0, index, w, h); // Top right
        double[] p3 = project(0, 1.0, index, w, h);   // Top left
        
        // Check if it's a keyframe
        boolean isKeyframe = wavetable.getKeyframes().stream().anyMatch(kf -> kf.getIndex() == index);
        
        Color sliceColor = isKeyframe ? Color.web("#00FFFF", 0.15) : Color.web("#FFFFFF", 0.08);
        if (isSelected) sliceColor = sliceColor.deriveColor(0, 1.2, 1.2, 1.5);
        
        gc.setFill(sliceColor);
        gc.fillPolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, 
                       new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
                       
        // Draw Floating Label
        gc.setFill(isSelected ? Color.YELLOW : Color.WHITE);
        String prefix = isKeyframe ? "✎ " : "+ ";
        if (isSliding && index == selectedWaveIndex) prefix = "⇳ ";
        gc.fillText(prefix + (index + 1), p1[0] + 10, p1[1]);
    }

    private void renderTimeline() {
        GraphicsContext gc = timelineCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, timelineCanvas.getWidth(), timelineCanvas.getHeight());
        
        double waveWidth = 128.0;
        for (int i = 0; i < 64; i++) {
            int[] samples = wavetable.getWave(i);
            double x = i * waveWidth;
            
            // Draw thumbnail
            if (i == selectedWaveIndex) {
                gc.setFill(Color.web("#333333"));
                gc.fillRect(x, 0, waveWidth, timelineCanvas.getHeight());
            }

            gc.setStroke(Color.web("#00FFFF", 0.8));
            gc.setLineWidth(1.0);
            gc.beginPath();
            for (int s = 0; s < 128; s++) {
                double val = samples[s] / NORM_Y;
                double py = 50 - (val * 25);
                if (s == 0) gc.moveTo(x + s, py);
                else gc.lineTo(x + s, py);
            }
            gc.stroke();
            
            // Draw Index
            gc.setFill(i == selectedWaveIndex ? Color.WHITE : Color.GRAY);
            gc.fillText(String.valueOf(i + 1), x + 5, 95);
        }
        
        // Draw Keyframe Markers
        for (Keyframe kf : wavetable.getKeyframes()) {
            double x = kf.getIndex() * waveWidth + 64;
            gc.setFill(Color.YELLOW);
            gc.fillPolygon(new double[]{x-5, x+5, x}, new double[]{5, 5, 15}, 3);
        }
    }

    private void renderCurrentWave() {
        GraphicsContext gc = currentWaveCanvas.getGraphicsContext2D();
        double w = currentWaveCanvas.getWidth();
        double h = currentWaveCanvas.getHeight();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        
        int previewIndex = (hoveredWaveIndex != -1) ? hoveredWaveIndex : selectedWaveIndex;
        int[] samples = wavetable.getWave(previewIndex);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2.0);
        gc.beginPath();
        for (int s = 0; s < 128; s++) {
            double val = samples[s] / NORM_Y;
            double px = (s / 128.0) * w;
            double py = (h/2) - (val * (h/2.5));
            if (s == 0) gc.moveTo(px, py);
            else gc.lineTo(px, py);
        }
        gc.stroke();
        
        waveInfoLabel.setText(String.format("Wave: %d  Samples: 128", previewIndex + 1));
    }
}
