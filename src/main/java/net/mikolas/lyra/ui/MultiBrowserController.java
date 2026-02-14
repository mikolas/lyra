package net.mikolas.lyra.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.mikolas.lyra.db.MultiRepository;
import net.mikolas.lyra.model.MultiPatch;

import java.util.function.Consumer;

public class MultiBrowserController {

    @FXML private TextField searchField;
    @FXML private ListView<MultiPatch> multiList;

    private Consumer<MultiPatch> onSelect;
    private ObservableList<MultiPatch> allMultis;

    @FXML
    public void initialize() {
        allMultis = MultiRepository.getInstance().getAllMultis();
        multiList.setItems(allMultis);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                multiList.setItems(allMultis);
            } else {
                multiList.setItems(allMultis.filtered(m -> 
                    m.getName().toLowerCase().contains(newVal.toLowerCase())));
            }
        });

        multiList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleLoad();
            }
        });
    }

    public void setOnSelect(Consumer<MultiPatch> onSelect) {
        this.onSelect = onSelect;
    }

    @FXML
    private void handleLoad() {
        MultiPatch selected = multiList.getSelectionModel().getSelectedItem();
        if (selected != null && onSelect != null) {
            onSelect.accept(selected);
            close();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) multiList.getScene().getWindow()).close();
    }
}
