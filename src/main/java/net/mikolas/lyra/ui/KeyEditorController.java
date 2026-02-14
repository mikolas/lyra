package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.mikolas.lyra.model.MultiPatch;
import net.mikolas.lyra.model.MultiPart;
import net.mikolas.lyra.ui.control.RangeSlider;

public class KeyEditorController {

    @FXML private VBox partsContainer;
    @FXML private net.mikolas.lyra.ui.control.PianoPreview pianoPreview;
    
    private MultiPatch currentMulti;

    public void setMultiPatch(MultiPatch multi) {
        this.currentMulti = multi;
        partsContainer.getChildren().clear();
        pianoPreview.setParts(javafx.collections.FXCollections.observableArrayList(multi.getParts()));
        
        for (int i = 0; i < 16; i++) {
            MultiPart part = multi.getParts().get(i);
            partsContainer.getChildren().add(createPartRow(i + 1, part));
        }
        
        // Auto-activate first part
        if (!multi.getParts().isEmpty()) {
            multi.getParts().get(0).setActive(true);
        }
    }

    private HBox createPartRow(int id, MultiPart part) {
        HBox row = new HBox(15);
        row.getStyleClass().add("multi-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 10, 4, 10));
        row.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 2;");

        // Active styling listener
        part.activeProperty().addListener((obs, old, active) -> {
            if (active) {
                row.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 2; -fx-border-color: #00FFFF; -fx-border-width: 1;");
            } else {
                row.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 2; -fx-border-color: transparent;");
            }
        });
        
        // Sync initial state
        if (part.isActive()) {
            row.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 2; -fx-border-color: #00FFFF; -fx-border-width: 1;");
        }

        Label idLabel = new Label(String.format("%02d", id));
        idLabel.setPrefWidth(30);
        idLabel.getStyleClass().add("tactical-label");
        idLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-weight: bold;");

        Label nameLabel = new Label("PART " + id);
        nameLabel.setPrefWidth(120);
        nameLabel.getStyleClass().add("tactical-label");
        nameLabel.setStyle("-fx-text-fill: #D4D4D4;");

        Label chanLabel = new Label();
        chanLabel.setPrefWidth(40);
        chanLabel.getStyleClass().add("tactical-label");
        chanLabel.setStyle("-fx-text-fill: #808080;");
        chanLabel.textProperty().bind(part.channelProperty().asString("CH%d"));

        RangeSlider slider = new RangeSlider(0, 127, part.getKeyLow(), part.getKeyHigh());
        HBox.setHgrow(slider, Priority.ALWAYS);
        slider.setPrefHeight(24);

        // Bindings
        slider.lowValueProperty().bindBidirectional(part.keyLowProperty());
        slider.highValueProperty().bindBidirectional(part.keyHighProperty());

        // Activation on click
        row.setOnMousePressed(e -> {
            deactivateAll();
            part.setActive(true);
        });

        row.getChildren().addAll(idLabel, nameLabel, chanLabel, slider);
        return row;
    }

    private void deactivateAll() {
        if (currentMulti != null) {
            for (MultiPart p : currentMulti.getParts()) {
                p.setActive(false);
            }
        }
    }
}