package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.midi.MidiService;
import net.mikolas.lyra.midi.MidiManager;

/**
 * Main Controller for the Sound Editor window.
 * Manages the layout and coordination of sub-components.
 */
public class SoundEditorController {

    @FXML private Label bankDisplay;
    @FXML private Label progDisplay;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    
    // Containers for injected sub-views
    @FXML private VBox osc1;
    @FXML private VBox osc2;
    @FXML private VBox osc3;
    @FXML private VBox lfo1;
    @FXML private VBox lfo2;
    @FXML private VBox lfo3;
    @FXML private VBox mixer;
    @FXML private VBox filter1;
    @FXML private VBox filter2;
    @FXML private VBox amp;
    @FXML private VBox common;
    
    @FXML private VBox filterEnv;
    @FXML private VBox ampEnv;
    @FXML private VBox env3;
    @FXML private VBox env4;
    
    @FXML private VBox fx1;
    @FXML private VBox fx2;
    @FXML private VBox arp;

    // References to sub-controllers (injected via FXML)
    @FXML private OscillatorController osc1Controller;
    @FXML private OscillatorController osc2Controller;
    @FXML private OscillatorController osc3Controller;
    @FXML private LfoController lfo1Controller;
    @FXML private LfoController lfo2Controller;
    @FXML private LfoController lfo3Controller;
    @FXML private MixerController mixerController;
    @FXML private FilterController filter1Controller;
    @FXML private FilterController filter2Controller;
    @FXML private AmpController ampController;
    @FXML private CommonSectionController commonController;
    @FXML private EnvelopeController filterEnvController;
    @FXML private EnvelopeController ampEnvController;
    @FXML private EnvelopeController env3Controller;
    @FXML private EnvelopeController env4Controller;
    @FXML private EffectController fx1Controller;
    @FXML private EffectController fx2Controller;
    @FXML private ArpController arpController;

    private Sound sound;
    private boolean isUpdating = false;
    private final net.mikolas.lyra.service.SoundSyncService syncService = new net.mikolas.lyra.service.SoundSyncService();

    @FXML
    public void initialize() {
        setupCategoryCombo();
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.CATEGORIES));
    }

    @FXML
    private void handlePrevBank() { navigateBank(-1); }
    @FXML
    private void handleNextBank() { navigateBank(1); }
    @FXML
    private void handlePrevProg() { navigateProg(-1); }
    @FXML
    private void handleNextProg() { navigateProg(1); }

    private void navigateBank(int delta) {
        if (sound == null || sound.getBank() == null) return;
        int newBank = Math.clamp(sound.getBank() + delta, 0, 7);
        if (newBank != sound.getBank()) {
            sound.setBank(newBank);
            updateHeader();
            syncHardwarePointer();

            // Wait for hardware to switch before requesting dump
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
            pause.setOnFinished(e -> requestCurrentSoundDump());
            pause.play();
        }
    }

    private void navigateProg(int delta) {
        if (sound == null || sound.getProgram() == null) return;
        int newProg = Math.clamp(sound.getProgram() + delta, 0, 127);
        if (newProg != sound.getProgram()) {
            sound.setProgram(newProg);
            updateHeader();
            syncHardwarePointer();

            // Wait for hardware to switch before requesting dump
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
            pause.setOnFinished(e -> requestCurrentSoundDump());
            pause.play();
        }
    }

    private void requestCurrentSoundDump() {
        try {
            MidiService midi = MidiManager.getInstance().getService();
            if (midi != null && sound.getBank() != null && sound.getProgram() != null) {
                midi.requestSoundDump(sound.getBank(), sound.getProgram());
            }
        } catch (Exception e) {
            System.err.println("Failed to request sound dump: " + e.getMessage());
        }
    }

    private void syncHardwarePointer() {
        try {
            MidiService midi = MidiManager.getInstance().getService();
            if (midi != null && sound.getBank() != null && sound.getProgram() != null) {
                midi.sendProgramChange(sound.getBank(), sound.getProgram());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendToSynth() {
        if (sound == null) return;
        try {
            MidiService midi = MidiManager.getInstance().getService();
            if (midi != null) {
                // Send to Edit Buffer (0x00, 0x00)
                midi.sendSoundDump(sound, 0, 0);
            }
        } catch (Exception e) {
            System.err.println("Failed to send sound to synth: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenModMatrix() {
        if (sound == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ModMatrixView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ModMatrixController controller = loader.getController();
            controller.setSound(sound);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Lyra - Modulation Matrix");
            stage.initModality(javafx.stage.Modality.NONE);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the sound to be edited and bind UI properties.
     * @param sound Sound model object
     */
    public void setSound(Sound sound) {
        this.sound = sound;
        updateHeader();
        bindGlobalProperties();
    }

    /**
     * Initialize all sub-controllers with their respective model components.
     */
    public void initializeSubControllers() {
        if (sound == null) return;
        
        osc1Controller.setOscillator(0, sound.getOscillators()[0]);
        osc2Controller.setOscillator(1, sound.getOscillators()[1]);
        osc3Controller.setOscillator(2, sound.getOscillators()[2]);
        
        lfo1Controller.setLfo(0, sound.getLFOs()[0]);
        lfo2Controller.setLfo(1, sound.getLFOs()[1]);
        lfo3Controller.setLfo(2, sound.getLFOs()[2]);
        
        mixerController.setMixer(sound.getMixer());
        filter1Controller.setFilter(0, sound.getFilters()[0], sound.getCommon());
        filter2Controller.setFilter(1, sound.getFilters()[1], sound.getCommon());
        ampController.setCommon(sound.getCommon());
        commonController.setCommon(sound.getCommon());
        
        filterEnvController.setEnvelope(0, sound.getEnvelopes()[0]);
        ampEnvController.setEnvelope(1, sound.getEnvelopes()[1]);
        env3Controller.setEnvelope(2, sound.getEnvelopes()[2]);
        env4Controller.setEnvelope(3, sound.getEnvelopes()[3]);
        
        fx1Controller.setEffect(0, sound.getEffects()[0]);
        fx2Controller.setEffect(1, sound.getEffects()[1]);
        arpController.setArpeggiator(sound.getArpeggiator());

        setupMidiListeners();
    }

    private void setupMidiListeners() {
        // Setup MIDI sync listener (outgoing)
        sound.setParameterChangeListener((paramId, value) -> {
            try {
                MidiService midi = MidiManager.getInstance().getService();
                if (midi != null) {
                    midi.sendParameterChange(paramId, value);
                }
            } catch (Exception e) {
                System.err.println("MIDI Sync Error: " + e.getMessage());
            }
        });

        // Setup incoming MIDI listener (bidirectional)
        MidiService midi = MidiManager.getInstance().getService();
        if (midi != null) {
            midi.setMessageCallback(msg -> {
                if (msg instanceof net.mikolas.lyra.midi.SoundParameterChange sndp) {
                    if (sndp.location() == 0x00) {
                        javafx.application.Platform.runLater(() -> {
                            sound.updateParameterSilently(sndp.paramId(), sndp.value());
                        });
                    }
                }
            });

            midi.setSoundDumpCallback(dumpedSound -> {
                if (syncService.shouldSyncSound(sound, dumpedSound)) {
                    javafx.application.Platform.runLater(() -> {
                        sound.replaceParameters(dumpedSound.getParameters());
                        updateHeader();
                    });
                }
            });
        }
    }

    private void updateHeader() {
        if (sound == null) return;

        if (sound.getBank() != null && bankDisplay != null) {
            bankDisplay.setText(String.valueOf((char) ('A' + sound.getBank())));
        }
        
        if (sound.getProgram() != null && progDisplay != null) {
            progDisplay.setText(String.format("%03d", sound.getProgram() + 1));
        }
        
        nameField.setText(sound.getName());
        
        Integer catIndex = sound.getCategory();
        if (catIndex != null && catIndex >= 0 && catIndex < categoryCombo.getItems().size()) {
            categoryCombo.getSelectionModel().select(catIndex);
        }
    }

    private void bindGlobalProperties() {
        if (sound == null) return;

        // Bidirectional binding for name
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            if (newVal != null && !newVal.equals(sound.getName())) {
                sound.setNameAndSyncParameters(newVal);
            }
        });
        
        sound.nameProperty().addListener((obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    nameField.setText(newVal);
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Category
        setupComboBoxSync(categoryCombo, sound.categoryProperty());
    }

    private void setupComboBoxSync(ComboBox<String> combo, javafx.beans.property.IntegerProperty property) {
        // UI -> Model
        combo.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if (isUpdating) return;
            if (newVal != null && newVal.intValue() != -1) {
                property.set(newVal.intValue());
            }
        });

        // Model -> UI
        property.addListener((obs, old, newVal) -> {
            javafx.application.Platform.runLater(() -> {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    int val = newVal.intValue();
                    if (val >= 0 && val < combo.getItems().size()) {
                        combo.getSelectionModel().select(val);
                    }
                } finally {
                    isUpdating = false;
                }
            });
        });

        // Initial select
        int initial = property.get();
        if (initial >= 0 && initial < combo.getItems().size()) {
            combo.getSelectionModel().select(initial);
        }
    }
}