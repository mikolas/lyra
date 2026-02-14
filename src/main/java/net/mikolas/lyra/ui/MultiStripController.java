package net.mikolas.lyra.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.mikolas.lyra.db.SoundRepository;
import net.mikolas.lyra.model.MultiPart;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.ui.control.Dial;
import net.mikolas.lyra.util.FxUtils;

public class MultiStripController {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final DataFormat PART_INDEX_FORMAT = new DataFormat("application/x-lyra-part-index");

    @FXML private VBox rootContainer;
    @FXML private HBox stripHeader;
    @FXML private CheckBox selectCheck;
    @FXML private Label partIdLabel;
    @FXML private ToggleButton muteButton;
    @FXML private ComboBox<String> channelCombo;
    @FXML private ComboBox<String> bankCombo;
    @FXML private ComboBox<String> programCombo;
    @FXML private Slider volumeSlider;
    @FXML private Slider panSlider;
    @FXML private Dial transposeDial;
    @FXML private Dial detuneDial;
    @FXML private Dial keyLowSlider;
    @FXML private Dial keyHighSlider;
    @FXML private Dial velLowSlider;
    @FXML private Dial velHighSlider;
    @FXML private ToggleButton midiInCheck;
    @FXML private ToggleButton usbInCheck;
    @FXML private ToggleButton localInCheck;
    @FXML private ToggleButton modInCheck;
    @FXML private ToggleButton pitchInCheck;
    @FXML private ToggleButton susInCheck;
    @FXML private ToggleButton pressInCheck;
    @FXML private ToggleButton editsInCheck;
    @FXML private ToggleButton prgInCheck;

    private MultiPart multiPart;

    @FXML
    private void handleJumpToVelocity() {
        FxUtils.findParent(rootContainer, MultiEditorController.class).ifPresent(MultiEditorController::showVelocityView);
    }

    @FXML
    private void handleJumpToKeys() {
        FxUtils.findParent(rootContainer, MultiEditorController.class).ifPresent(MultiEditorController::showKeysView);
    }

    @FXML
    public void initialize() {
        FxUtils.setSliderRange(volumeSlider, 0, 127, 100);
        FxUtils.setSliderRange(panSlider, 0, 127, 64);
        
        channelCombo.setItems(FXCollections.observableArrayList(ParameterValues.getValues(ParameterValueType.MIDI_CHANNELS)));
        bankCombo.setItems(FXCollections.observableArrayList(ParameterValues.getValues(ParameterValueType.BANKS)));

        // Searchable Program logic
        programCombo.setEditable(true);
        TextField editor = programCombo.getEditor();
        editor.textProperty().addListener((obs, old, newVal) -> {
            if (programCombo.isShowing()) {
                // Perform filtering logic if needed, 
                // but usually standard editable Combo is enough if we handle selection
            }
        });

        // Handle selection visuals (Cyan border/tint)
        selectCheck.selectedProperty().addListener((obs, old, newVal) -> {
            rootContainer.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, newVal);
            stripHeader.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, newVal);
        });
        
        // Handle Mute Button Text
        muteButton.selectedProperty().addListener((obs, old, newVal) -> {
            muteButton.setText(newVal ? "MUTE" : "PLAY");
        });

        // Default population until sounds are loaded
        updateProgramList();
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        stripHeader.setOnDragDetected(e -> {
            if (multiPart == null) return;
            Dragboard db = stripHeader.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(PART_INDEX_FORMAT, multiPart.getPartIndex());
            db.setContent(content);
            rootContainer.setOpacity(0.5);
            e.consume();
        });

        rootContainer.setOnDragOver(e -> {
            if (e.getGestureSource() != stripHeader && e.getDragboard().hasContent(PART_INDEX_FORMAT)) {
                e.acceptTransferModes(TransferMode.MOVE);
                rootContainer.setStyle("-fx-border-color: #00FFFF; -fx-border-width: 2; -fx-background-color: #222222;");
            }
            e.consume();
        });

        rootContainer.setOnDragExited(e -> {
            rootContainer.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
            e.consume();
        });

        rootContainer.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(PART_INDEX_FORMAT)) {
                int sourceIdx = (int) db.getContent(PART_INDEX_FORMAT);
                int targetIdx = multiPart.getPartIndex();
                
                multiPart.getPatch().swapParts(sourceIdx, targetIdx);
                e.setDropCompleted(true);
            }
            e.consume();
        });

        stripHeader.setOnDragDone(e -> {
            rootContainer.setOpacity(1.0);
            e.consume();
        });
    }

    private void updateProgramList() {
        int selectedBank = 0;
        if (bankCombo.getSelectionModel().getSelectedIndex() >= 0) {
            selectedBank = bankCombo.getSelectionModel().getSelectedIndex();
        }

        String[] slotNames = new String[128];
        for (int i = 0; i < 128; i++) {
            slotNames[i] = String.format("%03d: <Empty>", i + 1);
        }

        ObservableList<Sound> sounds = SoundRepository.getInstance().getAllSounds();
        for (Sound s : sounds) {
            if (s.getBank() != null && s.getBank() == selectedBank && s.getProgram() != null) {
                int p = s.getProgram();
                if (p >= 0 && p < 128) {
                    slotNames[p] = String.format("%03d: %s", p + 1, s.getName());
                }
            }
        }
        
        int currentSel = programCombo.getSelectionModel().getSelectedIndex();
        programCombo.setItems(FXCollections.observableArrayList(slotNames));
        if (currentSel >= 0) programCombo.getSelectionModel().select(currentSel);
    }

    public void bind(MultiPart multiPart) {
        this.multiPart = multiPart;
        partIdLabel.setText(String.format("%02d", multiPart.getPartIndex() + 1));

        // Bindings
        selectCheck.selectedProperty().bindBidirectional(multiPart.selectedProperty());
        muteButton.selectedProperty().bindBidirectional(multiPart.getMuteProperty());
        
        // Use custom bidirectional binding for Double <-> Integer
        bindBi(volumeSlider.valueProperty(), multiPart.getVolumeProperty());
        bindBi(panSlider.valueProperty(), multiPart.getPanProperty());
        bindBi(transposeDial.valueProperty(), multiPart.getTransposeProperty());
        bindBi(detuneDial.valueProperty(), multiPart.getDetuneProperty());
        bindBi(keyLowSlider.valueProperty(), multiPart.getKeyLowProperty());
        bindBi(keyHighSlider.valueProperty(), multiPart.getKeyHighProperty());
        bindBi(velLowSlider.valueProperty(), multiPart.getVelLowProperty());
        bindBi(velHighSlider.valueProperty(), multiPart.getVelHighProperty());

        midiInCheck.selectedProperty().bindBidirectional(multiPart.getMidiInProperty());
        usbInCheck.selectedProperty().bindBidirectional(multiPart.getUsbInProperty());
        localInCheck.selectedProperty().bindBidirectional(multiPart.getLocalInProperty());
        modInCheck.selectedProperty().bindBidirectional(multiPart.getModInProperty());
        pitchInCheck.selectedProperty().bindBidirectional(multiPart.getPitchInProperty());
        susInCheck.selectedProperty().bindBidirectional(multiPart.getSusInProperty());
        pressInCheck.selectedProperty().bindBidirectional(multiPart.getPressInProperty());
        editsInCheck.selectedProperty().bindBidirectional(multiPart.getEditInProperty());
        prgInCheck.selectedProperty().bindBidirectional(multiPart.getPrgInProperty());
        
        // ComboBox bindings
        channelCombo.getSelectionModel().select(multiPart.getChannelProperty().get());
        multiPart.getChannelProperty().addListener((obs, old, newVal) -> channelCombo.getSelectionModel().select(newVal.intValue()));
        channelCombo.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.intValue() >= 0) multiPart.getChannelProperty().set(newVal.intValue());
        });

        bankCombo.getSelectionModel().select(multiPart.getBankProperty().get());
        
        multiPart.getBankProperty().addListener((obs, old, newVal) -> {
            bankCombo.getSelectionModel().select(newVal.intValue());
            updateProgramList();
        });
        
        bankCombo.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.intValue() >= 0) {
                multiPart.getBankProperty().set(newVal.intValue());
                updateProgramList();
            }
        });

        programCombo.getSelectionModel().select(multiPart.getProgramProperty().get());
        multiPart.getProgramProperty().addListener((obs, old, newVal) -> programCombo.getSelectionModel().select(newVal.intValue()));
        programCombo.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.intValue() >= 0) multiPart.getProgramProperty().set(newVal.intValue());
        });
        
        updateProgramList();
    }
    
    /**
     * Helper to bind a JavaFX DoubleProperty (UI control) to an IntegerProperty (Model).
     */
    private void bindBi(DoubleProperty uiProp, IntegerProperty modelProp) {
        // Initial sync from Model to UI
        uiProp.set(modelProp.get());

        // UI -> Model
        uiProp.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int intVal = newVal.intValue();
                if (modelProp.get() != intVal) {
                    modelProp.set(intVal);
                }
            }
        });

        // Model -> UI
        modelProp.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double dVal = newVal.doubleValue();
                if (Math.abs(uiProp.get() - dVal) > 0.001) {
                    uiProp.set(dVal);
                }
            }
        });
    }
}