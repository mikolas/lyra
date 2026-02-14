package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import net.mikolas.lyra.model.*;
import net.mikolas.lyra.ui.control.Dial;
import java.util.List;
import java.util.Random;

public class ModMatrixController {

    @FXML private GridPane modGrid;
    @FXML private VBox modifiersContainer;
    @FXML private Label activeCountLabel;

    private Sound currentSound;
    private ModSlotRow[] modRows = new ModSlotRow[16];
    private ModifierRow[] modifierRows = new ModifierRow[4];
    
    // Global Clipboard for Copy/Paste
    private static int clipboardSource = 0;
    private static int clipboardDest = 0;
    private static int clipboardAmount = 64;
    private static boolean hasClipboard = false;

    @FXML
    public void initialize() {
        // Initialize 16 Modulation Slot Rows
        for (int i = 0; i < 16; i++) {
            modRows[i] = new ModSlotRow(i);
            modRows[i].addToGrid(modGrid, i + 1); // Row 0 is header
        }

        // Initialize 4 Modifier Rows
        for (int i = 0; i < 4; i++) {
            modifierRows[i] = new ModifierRow(i);
            modifiersContainer.getChildren().add(modifierRows[i].getRoot());
        }
    }

    public void setSound(Sound sound) {
        this.currentSound = sound;
        
        ModulationSlot[] slots = sound.getModulationSlots();
        for (int i = 0; i < 16; i++) {
            modRows[i].bind(slots[i]);
        }
        
        Modifier[] modifiers = sound.getModifiers();
        for (int i = 0; i < 4; i++) {
            modifierRows[i].bind(modifiers[i]);
        }
        
        updateActiveCount();
    }

    private void updateActiveCount() {
        if (currentSound == null) return;
        
        activeCountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int count = 0;
            for (ModulationSlot slot : currentSound.getModulationSlots()) {
                if (slot.sourceProperty().get() > 0) count++;
            }
            return String.valueOf(count);
        }, getSlotSourceProperties()));
    }

    private javafx.beans.Observable[] getSlotSourceProperties() {
        ModulationSlot[] slots = currentSound.getModulationSlots();
        javafx.beans.Observable[] props = new javafx.beans.Observable[16];
        for (int i = 0; i < 16; i++) {
            props[i] = slots[i].sourceProperty();
        }
        return props;
    }

    @FXML
    private void handleClose() {
        modGrid.getScene().getWindow().hide();
    }

    @FXML
    private void handleClearAll() {
        if (currentSound == null) return;
        for (ModulationSlot slot : currentSound.getModulationSlots()) {
            slot.sourceProperty().set(0);
            slot.destinationProperty().set(0);
            slot.amountProperty().set(64);
        }
    }

    @FXML
    private void handleRandomize() {
        if (currentSound == null) return;
        Random rnd = new Random();
        List<String> sources = ParameterValues.getValues(ParameterValueType.MOD_SOURCE);
        List<String> dests = ParameterValues.getValues(ParameterValueType.MOD_DESTINATION);
        
        for (ModulationSlot slot : currentSound.getModulationSlots()) {
            // 20% chance to be off, otherwise random
            if (rnd.nextFloat() > 0.2) {
                slot.sourceProperty().set(rnd.nextInt(sources.size()));
                slot.destinationProperty().set(rnd.nextInt(dests.size()));
                slot.amountProperty().set(rnd.nextInt(128));
            } else {
                slot.sourceProperty().set(0);
            }
        }
    }

    private class ModSlotRow {
        private final int index;
        private final Label indexLabel;
        private final ComboBox<String> sourceCombo;
        private final Dial amountDial;
        private final ComboBox<String> destCombo;
        private final HBox actionsBox;
        private final Button copyBtn;
        private final Button pasteBtn;
        private ModulationSlot slot;

        public ModSlotRow(int index) {
            this.index = index;
            this.indexLabel = new Label(String.format("%02d", index + 1));
            this.indexLabel.getStyleClass().add("tactical-label");
            
            this.sourceCombo = new ComboBox<>();
            this.sourceCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_SOURCE));
            this.sourceCombo.setMaxWidth(Double.MAX_VALUE);
            this.sourceCombo.setStyle("-fx-font-size: 10px;");
            
            this.amountDial = new Dial("", 0, 127, 64);
            this.amountDial.setPrefSize(32, 32);
            
            this.destCombo = new ComboBox<>();
            this.destCombo.getItems().addAll(ParameterValues.getValues(ParameterValueType.MOD_DESTINATION));
            this.destCombo.setMaxWidth(Double.MAX_VALUE);
            this.destCombo.setStyle("-fx-font-size: 10px;");

            this.copyBtn = new Button("C");
            this.copyBtn.getStyleClass().add("compact-button");
            this.copyBtn.setTooltip(new Tooltip("Copy Slot"));
            this.copyBtn.setOnAction(e -> handleCopy());

            this.pasteBtn = new Button("P");
            this.pasteBtn.getStyleClass().add("compact-button");
            this.pasteBtn.setTooltip(new Tooltip("Paste Slot"));
            this.pasteBtn.setDisable(!hasClipboard);
            this.pasteBtn.setOnAction(e -> handlePaste());

            this.actionsBox = new HBox(4, copyBtn, pasteBtn);
            this.actionsBox.setAlignment(Pos.CENTER);
            
            // Interaction: disable amount/dest if source is off
            sourceCombo.valueProperty().addListener((obs, old, val) -> {
                boolean active = sourceCombo.getSelectionModel().getSelectedIndex() > 0;
                amountDial.setDisable(!active);
                destCombo.setDisable(!active);
            });
        }

        public void addToGrid(GridPane grid, int row) {
            grid.add(indexLabel, 0, row);
            grid.add(sourceCombo, 1, row);
            grid.add(amountDial, 2, row);
            grid.add(destCombo, 3, row);
            grid.add(actionsBox, 4, row);
        }

        public void bind(ModulationSlot slot) {
            this.slot = slot;
            sourceCombo.getSelectionModel().select(slot.sourceProperty().get());
            sourceCombo.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> {
                slot.sourceProperty().set(val.intValue());
            });
            slot.sourceProperty().addListener((obs, old, val) -> {
                sourceCombo.getSelectionModel().select(val.intValue());
            });

            destCombo.getSelectionModel().select(slot.destinationProperty().get());
            destCombo.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> {
                slot.destinationProperty().set(val.intValue());
            });
            slot.destinationProperty().addListener((obs, old, val) -> {
                destCombo.getSelectionModel().select(val.intValue());
            });

            amountDial.setValue(slot.amountProperty().get());
            amountDial.valueProperty().addListener((obs, old, val) -> {
                slot.amountProperty().set(val.intValue());
            });
            slot.amountProperty().addListener((obs, old, val) -> {
                amountDial.setValue(val.doubleValue());
            });
            
            // Initial state
            boolean active = slot.sourceProperty().get() > 0;
            amountDial.setDisable(!active);
            destCombo.setDisable(!active);
        }

        private void handleCopy() {
            if (slot == null) return;
            clipboardSource = slot.sourceProperty().get();
            clipboardDest = slot.destinationProperty().get();
            clipboardAmount = slot.amountProperty().get();
            hasClipboard = true;
            
            // Enable all paste buttons
            for (ModSlotRow row : modRows) {
                row.pasteBtn.setDisable(false);
            }
        }

        private void handlePaste() {
            if (slot == null || !hasClipboard) return;
            slot.sourceProperty().set(clipboardSource);
            slot.destinationProperty().set(clipboardDest);
            slot.amountProperty().set(clipboardAmount);
        }
    }

    private class ModifierRow {
        private final VBox root;
        private final ComboBox<String> sourceA;
        private final ComboBox<String> sourceB;
        private final ComboBox<String> operation;
        private final Dial constantDial;

        public ModifierRow(int index) {
            this.root = new VBox(5);
            this.root.getStyleClass().add("details-panel");
            this.root.setStyle("-fx-border-color: #333; -fx-padding: 8;");
            
            Label title = new Label("MODIFIER " + (index + 1));
            title.getStyleClass().add("tactical-label");
            title.setStyle("-fx-text-fill: #FFA500;");
            
            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(4);
            
            sourceA = createCombo(ParameterValueType.MOD_SOURCE);
            sourceB = createCombo(ParameterValueType.MOD_SOURCE);
            operation = createCombo(ParameterValueType.MODIFIER_OPERATIONS);
            constantDial = new Dial("CONST", 0, 127, 64);
            constantDial.setPrefSize(36, 40);
            
            grid.add(new Label("SRC A"), 0, 0);
            grid.add(sourceA, 1, 0);
            grid.add(new Label("SRC B"), 0, 1);
            grid.add(new Label("OP"), 2, 0);
            grid.add(operation, 3, 0);
            grid.add(constantDial, 3, 1);
            
            // Set tactical labels for grid labels
            grid.getChildren().forEach(n -> {
                if (n instanceof Label && n != title) ((Label)n).getStyleClass().add("tactical-label");
            });

            this.root.getChildren().addAll(title, grid);
        }

        private ComboBox<String> createCombo(ParameterValueType type) {
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll(ParameterValues.getValues(type));
            cb.setMaxWidth(Double.MAX_VALUE);
            cb.setStyle("-fx-font-size: 9px;");
            return cb;
        }

        public VBox getRoot() { return root; }

        public void bind(Modifier mod) {
            sourceA.getSelectionModel().select(mod.sourceAProperty().get());
            sourceA.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> mod.sourceAProperty().set(val.intValue()));
            mod.sourceAProperty().addListener((obs, old, val) -> sourceA.getSelectionModel().select(val.intValue()));

            sourceB.getSelectionModel().select(mod.sourceBProperty().get());
            sourceB.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> mod.sourceBProperty().set(val.intValue()));
            mod.sourceBProperty().addListener((obs, old, val) -> sourceB.getSelectionModel().select(val.intValue()));

            operation.getSelectionModel().select(mod.operationProperty().get());
            operation.getSelectionModel().selectedIndexProperty().addListener((obs, old, val) -> mod.operationProperty().set(val.intValue()));
            mod.operationProperty().addListener((obs, old, val) -> operation.getSelectionModel().select(val.intValue()));

            constantDial.setValue(mod.constantProperty().get());
            constantDial.valueProperty().addListener((obs, old, val) -> mod.constantProperty().set(val.intValue()));
            mod.constantProperty().addListener((obs, old, val) -> constantDial.setValue(val.doubleValue()));
        }
    }
}