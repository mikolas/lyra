package net.mikolas.lyra.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.mikolas.lyra.db.Database;
import net.mikolas.lyra.db.SoundRepository;
import net.mikolas.lyra.exception.MidiException;
import net.mikolas.lyra.midi.MidiManager;
import net.mikolas.lyra.midi.MidiService;
import net.mikolas.lyra.model.ParameterValueType;
import net.mikolas.lyra.model.ParameterValues;
import net.mikolas.lyra.model.Sound;
import net.mikolas.lyra.service.ExportService;
import net.mikolas.lyra.service.ImportService;
import net.mikolas.lyra.service.SoundFilterService;
import net.mikolas.lyra.service.CollectionService;
import net.mikolas.lyra.service.TagService;
import net.mikolas.lyra.service.TreeNavigationService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Librarian view (REV3 - TreeView + Tokenized Search).
 *
 * <p>Features hierarchical tree navigation and filter chip system.
 */
public class LibrarianController {

  @FXML private MenuBar menuBar;
  
  private final ExportService exportService = new ExportService();
  private ImportService importService;
  private final SoundFilterService filterService = new SoundFilterService();
  private final CollectionService collectionService = new CollectionService(Database.getInstance());
  private final TagService tagService = new TagService(Database.getInstance());
  private final TreeNavigationService treeNavigationService = new TreeNavigationService();

  // Tree browser
  @FXML private TreeView<String> libraryTree;

  // Tokenized search
  @FXML private FlowPane filterChipsPane;
  @FXML private TextField searchField;

  // Sound table
  @FXML private TableView<Sound> soundTable;
  @FXML private TableColumn<Sound, Integer> bankColumn;
  @FXML private TableColumn<Sound, Integer> programColumn;
  @FXML private TableColumn<Sound, String> nameColumn;
  @FXML private TableColumn<Sound, String> categoryColumn;

  // Details panel
  @FXML private Label selectedNameLabel;
  @FXML private Label selectedInfoLabel;
  @FXML private FlowPane selectedMetadataPane;

  // Status bar
  @FXML private Label statusLabel;
  @FXML private Label midiInIndicator;
  @FXML private Label midiInNameLabel;
  @FXML private Label midiOutIndicator;
  @FXML private Label midiOutNameLabel;

  // Data
  private Database database;
  private final SoundRepository repository = SoundRepository.getInstance();
  private final ObservableList<Sound> allSounds = repository.getAllSounds();
  private FilteredList<Sound> filteredSounds;
  private SortedList<Sound> sortedSounds;
  private List<SoundFilter> activeFilters = new ArrayList<>();

  @FXML
  public void initialize() {
    database = repository.getDatabase();
    importService = new ImportService(database);
    
    setupTree();
    setupTable();
    setupContextMenu();
    setupSearch();
    setupSelection();
    
    // Setup MIDI connection listener
    MidiManager.getInstance().addConnectionListener(() -> 
        javafx.application.Platform.runLater(this::updateMidiStatus));
    
    updateMidiStatus();
  }

  private void updateMidiStatus() {
    MidiService midi = MidiManager.getInstance().getService();
    if (midi == null) {
      setIndicatorStatus(midiInIndicator, false);
      midiInNameLabel.setText("None");
      setIndicatorStatus(midiOutIndicator, false);
      midiOutNameLabel.setText("None");
      return;
    }

    boolean inConnected = midi.isInputConnected();
    boolean outConnected = midi.isOutputConnected();

    setIndicatorStatus(midiInIndicator, inConnected);
    midiInNameLabel.setText(midi.getInputDeviceName());
    
    setIndicatorStatus(midiOutIndicator, outConnected);
    midiOutNameLabel.setText(midi.getOutputDeviceName());
  }

  private void setIndicatorStatus(Label indicator, boolean connected) {
    indicator.getStyleClass().removeAll("midi-connected", "midi-disconnected");
    indicator.getStyleClass().add(connected ? "midi-connected" : "midi-disconnected");
  }

  public void shutdown() {
    repository.shutdown();
  }

  private void setupTree() {
    // Create tree structure
    TreeItem<String> root = new TreeItem<>("Library");
    root.setExpanded(true);

    // Library section
    TreeItem<String> library = new TreeItem<>("📂 Library");
    library.setExpanded(true);
    TreeItem<String> allSoundsItem = new TreeItem<>("• All Sounds");
    
    // Factory Banks (from constants)
    TreeItem<String> factoryBanks = new TreeItem<>("💾 Factory Banks");
    for (String bank : ParameterValues.getValues(ParameterValueType.BANKS)) {
      factoryBanks.getChildren().add(new TreeItem<>("Bank " + bank));
    }
    
    // Categories (from constants)
    TreeItem<String> categories = new TreeItem<>("🎵 Categories");
    for (String category : ParameterValues.getValues(ParameterValueType.CATEGORIES)) {
      categories.getChildren().add(new TreeItem<>("• " + category));
    }
    
    TreeItem<String> userBanks = new TreeItem<>("👤 User Banks");
    library.getChildren().addAll(allSoundsItem, factoryBanks, categories, userBanks);

    // Collections section
    TreeItem<String> collections = new TreeItem<>("📚 Collections");
    collections.setExpanded(true);
    refreshCollections(collections);

    // Tags section
    TreeItem<String> tags = new TreeItem<>("🏷️ Tags");
    tags.setExpanded(true);
    refreshTags(tags);

    root.getChildren().addAll(library, collections, tags);
    libraryTree.setRoot(root);
    libraryTree.setShowRoot(false);

    // Handle tree selection
    libraryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        handleTreeSelection(newVal);
      }
    });
  }

  private void refreshCollections(TreeItem<String> parent) {
    parent.getChildren().clear();
    if (database == null) return;
    try {
      List<net.mikolas.lyra.model.Collection> colls = database.collections.queryForAll();
      for (net.mikolas.lyra.model.Collection c : colls) {
        parent.getChildren().add(new TreeItem<>("• " + c.getName()));
      }
    } catch (SQLException e) {
      System.err.println("Failed to load collections: " + e.getMessage());
    }
  }

  private void refreshTags(TreeItem<String> parent) {
    parent.getChildren().clear();
    if (database == null) return;
    try {
      List<net.mikolas.lyra.model.Tag> tgs = database.tags.queryForAll();
      for (net.mikolas.lyra.model.Tag t : tgs) {
        parent.getChildren().add(new TreeItem<>("• " + t.getName()));
      }
    } catch (SQLException e) {
      System.err.println("Failed to load tags: " + e.getMessage());
    }
  }

  private void handleTreeSelection(TreeItem<String> item) {
    searchField.clear();

    String value = item.getValue();
    String parent = item.getParent() != null ? item.getParent().getValue() : null;
    
    Optional<SoundFilter> filter = treeNavigationService.parseTreeSelection(value, parent);
    
    if (filter.isEmpty()) {
      activeFilters.clear();
    } else {
      addFilter(filter.get());
    }
    updateFilterChips();
  }

  private void addFilter(SoundFilter filter) {
    // Remove existing filter of same type and value
    activeFilters.removeIf(f -> f.type() == filter.type() && f.value().equals(filter.value()));
    activeFilters.add(filter);
    updateFilterChips();
  }

  private void removeFilter(SoundFilter filter) {
    activeFilters.remove(filter);
    updateFilterChips();
  }

  private void updateFilterChips() {
    // Clear existing chips (keep search icon and text field)
    filterChipsPane.getChildren().removeIf(node -> 
        node.getStyleClass().contains("filter-chip"));

    // Add chips for each active filter
    int insertIndex = 1; // After search icon
    for (SoundFilter filter : activeFilters) {
      Button chip = createFilterChip(filter);
      filterChipsPane.getChildren().add(insertIndex++, chip);
    }

    // Apply filters
    applyFilters();
  }

  private Button createFilterChip(SoundFilter filter) {
    Button chip = new Button();
    chip.getStyleClass().add("filter-chip");
    
    Label textLabel = new Label(filter.getDisplayText());
    textLabel.getStyleClass().add("chip-text");
    
    Label closeLabel = new Label(" ⓧ");
    closeLabel.getStyleClass().add("close-button");
    closeLabel.setOnMouseClicked(e -> {
      e.consume(); // Prevent click from bubbling to the button
      removeFilter(filter);
    });
    
    HBox content = new HBox(textLabel, closeLabel);
    content.setSpacing(4);
    content.setAlignment(javafx.geometry.Pos.CENTER);
    chip.setGraphic(content);
    
    return chip;
  }

  private void setupTable() {
    // Configure table columns with numeric sorting
    bankColumn.setCellValueFactory(
        cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBank()));

    // Display bank as letter (A-H) but sort as number
    bankColumn.setCellFactory(column -> new TableCell<Sound, Integer>() {
      @Override
      protected void updateItem(Integer bank, boolean empty) {
        super.updateItem(bank, empty);
        if (empty || bank == null) {
          setText(null);
        } else {
          setText(String.valueOf((char) ('A' + bank)));
        }
      }
    });

    programColumn.setCellValueFactory(
        cellData -> {
          Integer prog = cellData.getValue().getProgram();
          return new javafx.beans.property.SimpleObjectProperty<>(prog != null ? prog + 1 : 0);
        });

    nameColumn.setCellValueFactory(
        cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

    categoryColumn.setCellValueFactory(
        cellData -> {
          Integer catIndex = cellData.getValue().getCategory();
          String catName = net.mikolas.lyra.midi.SysExParser.getCategoryName(catIndex);
          return new javafx.beans.property.SimpleStringProperty(catName);
        });

    // Enable column sorting
    bankColumn.setSortable(true);
    programColumn.setSortable(true);
    nameColumn.setSortable(true);
    categoryColumn.setSortable(true);

    // Setup filtered list, then wrap in sorted list
    filteredSounds = new FilteredList<>(allSounds, p -> true);
    sortedSounds = new SortedList<>(filteredSounds);
    
    // Bind sorted list comparator to table
    sortedSounds.comparatorProperty().bind(soundTable.comparatorProperty());
    
    soundTable.setItems(sortedSounds);
    
    // Set default sort: Bank (A-H), then Program (ascending)
    soundTable.getSortOrder().add(bankColumn);
    soundTable.getSortOrder().add(programColumn);

    // Handle double-click to edit
    soundTable.setRowFactory(tv -> {
      TableRow<Sound> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          handleEdit();
        }
      });
      return row;
    });
  }

  private void setupSearch() {
    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal == null) return;
      
      String text = newVal.trim().toLowerCase();
      
      // Support prefix typing: tag:Name, cat:Name, bank:A
      if (text.endsWith(" ") || text.endsWith(",")) {
        String cleanText = text.substring(0, text.length() - 1).trim();
        if (handlePrefixFilter(cleanText)) {
          searchField.clear();
          return;
        }
      }
      
      applyFilters();
    });

    // Handle Enter key to force prefix conversion or just keep as text
    searchField.setOnAction(e -> {
      String text = searchField.getText().trim().toLowerCase();
      if (handlePrefixFilter(text)) {
        searchField.clear();
      }
    });
  }

  private boolean handlePrefixFilter(String text) {
    var filterOpt = FilterParser.parsePrefixFilter(text);
    if (filterOpt.isPresent()) {
      addFilter(filterOpt.get());
      return true;
    }
    return false;
  }

  private void setupSelection() {
    soundTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> updateSelection(newVal));
  }

  private void setupContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem auditionItem = new MenuItem("Audition");
    auditionItem.setOnAction(e -> handleAudition());
    contextMenu.getItems().add(auditionItem);

    MenuItem editItem = new MenuItem("Edit");
    editItem.setOnAction(e -> handleEdit());
    contextMenu.getItems().add(editItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    // Add to Collection submenu
    Menu addToCollectionMenu = new Menu("Add to Collection");
    contextMenu.getItems().add(addToCollectionMenu);

    // Tag with submenu
    Menu tagWithMenu = new Menu("Tag with");
    contextMenu.getItems().add(tagWithMenu);

    contextMenu.getItems().add(new SeparatorMenuItem());

    // Remove from Collection submenu
    Menu removeFromCollectionMenu = new Menu("Remove from Collection");
    contextMenu.getItems().add(removeFromCollectionMenu);

    // Remove Tag submenu
    Menu removeTagMenu = new Menu("Remove Tag");
    contextMenu.getItems().add(removeTagMenu);

    contextMenu.setOnShowing(e -> {
      boolean hasSelection = !soundTable.getSelectionModel().isEmpty();
      auditionItem.setDisable(!hasSelection);
      editItem.setDisable(!hasSelection);
      updateAddToCollectionMenu(addToCollectionMenu);
      updateTagWithMenu(tagWithMenu);
      updateRemoveFromCollectionMenu(removeFromCollectionMenu);
      updateRemoveTagMenu(removeTagMenu);
    });

    soundTable.setContextMenu(contextMenu);
  }

  private void updateRemoveFromCollectionMenu(Menu menu) {
    menu.getItems().clear();
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || database == null) return;

    try {
      if (selected.getSoundCollections() != null) {
        for (net.mikolas.lyra.model.SoundCollection sc : selected.getSoundCollections()) {
          if (sc.getCollection() != null) {
            MenuItem item = new MenuItem(sc.getCollection().getName());
            item.setOnAction(e -> removeFromCollection(selected, sc));
            menu.getItems().add(item);
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error updating removal menu: " + e.getMessage());
    }
    menu.setDisable(menu.getItems().isEmpty());
  }

  private void updateRemoveTagMenu(Menu menu) {
    menu.getItems().clear();
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || database == null) return;

    try {
      if (selected.getSoundTags() != null) {
        for (net.mikolas.lyra.model.SoundTag st : selected.getSoundTags()) {
          if (st.getTag() != null) {
            MenuItem item = new MenuItem(st.getTag().getName());
            item.setOnAction(e -> removeTag(selected, st));
            menu.getItems().add(item);
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error updating removal tag menu: " + e.getMessage());
    }
    menu.setDisable(menu.getItems().isEmpty());
  }

  private void handleAudition() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    MidiService midi = MidiManager.getInstance().getService();
    if (midi == null || !midi.isConnected()) {
      showError("MIDI Error", "Not connected to Blofeld hardware.");
      return;
    }

    try {
      midi.auditionSound(selected);
    } catch (MidiException e) {
      showError("Audition Failed", "Failed to send sound to Blofeld: " + e.getMessage());
    }
  }

  private void handleEdit() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("SoundEditorView.fxml"));
      Parent root = loader.load();
      
      SoundEditorController controller = loader.getController();
      controller.setSound(selected);
      controller.initializeSubControllers();
      
      // Sync hardware to this sound
      try {
        MidiService midi = MidiManager.getInstance().getService();
        if (midi != null) {
          midi.sendProgramChange(selected.getBank(), selected.getProgram());
        }
      } catch (MidiException e) {
        System.err.println("Failed to send program change: " + e.getMessage());
      }
      
      Stage stage = new Stage();
      stage.setTitle("Lyra Sound Editor - " + selected.getName());
      stage.setScene(new Scene(root));
      stage.setResizable(false);
      stage.show();
    } catch (Exception e) {
      showError("Error", "Failed to open Sound Editor: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void updateAddToCollectionMenu(Menu menu) {
    menu.getItems().clear();
    if (database == null) return;

    try {
      List<net.mikolas.lyra.model.Collection> colls = database.collections.queryForAll();
      for (net.mikolas.lyra.model.Collection c : colls) {
        MenuItem item = new MenuItem(c.getName());
        item.setOnAction(e -> addSelectedToCollection(c));
        menu.getItems().add(item);
      }
      
      if (!menu.getItems().isEmpty()) {
        menu.getItems().add(new SeparatorMenuItem());
      }
      
      MenuItem newItem = new MenuItem("New Collection...");
      newItem.setOnAction(e -> handleNewCollection());
      menu.getItems().add(newItem);
    } catch (SQLException e) {
      System.err.println("Error updating collection menu: " + e.getMessage());
    }
  }

  private void updateTagWithMenu(Menu menu) {
    menu.getItems().clear();
    if (database == null) return;

    try {
      List<net.mikolas.lyra.model.Tag> tgs = database.tags.queryForAll();
      for (net.mikolas.lyra.model.Tag t : tgs) {
        MenuItem item = new MenuItem(t.getName());
        item.setOnAction(e -> tagSelectedWith(t));
        menu.getItems().add(item);
      }
      
      if (!menu.getItems().isEmpty()) {
        menu.getItems().add(new SeparatorMenuItem());
      }
      
      MenuItem newItem = new MenuItem("New Tag...");
      newItem.setOnAction(e -> handleNewTag());
      menu.getItems().add(newItem);
    } catch (SQLException e) {
      System.err.println("Error updating tag menu: " + e.getMessage());
    }
  }

  private void addSelectedToCollection(net.mikolas.lyra.model.Collection collection) {
    List<Sound> selected = new ArrayList<>(soundTable.getSelectionModel().getSelectedItems());
    if (selected.isEmpty()) return;

    soundTable.getContextMenu().hide();

    try {
      int added = collectionService.addSoundsToCollection(selected, collection);
      updateMetadataChips(soundTable.getSelectionModel().getSelectedItem());
      showInfo("Success", "Added " + added + " sounds to " + collection.getName());
    } catch (Exception e) {
      showError("Error", "Failed to add sounds to collection: " + e.getMessage());
    }
  }

  private void tagSelectedWith(net.mikolas.lyra.model.Tag tag) {
    List<Sound> selected = new ArrayList<>(soundTable.getSelectionModel().getSelectedItems());
    if (selected.isEmpty()) return;

    soundTable.getContextMenu().hide();

    try {
      int tagged = tagService.addTagsToSounds(selected, tag);
      updateMetadataChips(soundTable.getSelectionModel().getSelectedItem());
      showInfo("Success", "Tagged " + tagged + " sounds with " + tag.getName());
    } catch (Exception e) {
      showError("Error", "Failed to tag sounds: " + e.getMessage());
    }
  }

  private void loadSounds() {
    repository.refresh();
  }

  private void applyFilters() {
    String searchText = searchField.getText();
    List<Sound> filtered = filterService.filter(allSounds, searchText, activeFilters);
    filteredSounds.setPredicate(sound -> filtered.contains(sound));
    updateStatusBar();
  }

  private void updateSelection(Sound sound) {
    if (sound == null) {
      selectedNameLabel.setText("Selected: (none)");
      selectedInfoLabel.setText("");
      selectedMetadataPane.getChildren().clear();
    } else {
      selectedNameLabel.setText("Selected: " + sound.getName());
      Integer bank = sound.getBank();
      Integer prog = sound.getProgram();
      String bankStr = bank != null ? String.valueOf((char) ('A' + bank)) : "?";
      String progStr = prog != null ? String.format("%03d", prog + 1) : "???";
      selectedInfoLabel.setText(
          String.format(
              "Bank: %s  Program: %s  Category: %s",
              bankStr,
              progStr,
              net.mikolas.lyra.midi.SysExParser.getCategoryName(sound.getCategory())));
      updateMetadataChips(sound);
    }
    updateStatusBar();
  }

  private void updateMetadataChips(Sound sound) {
    selectedMetadataPane.getChildren().clear();
    if (database == null) return;

    try {
      // Collections
      if (sound.getSoundCollections() != null) {
        for (net.mikolas.lyra.model.SoundCollection sc : sound.getSoundCollections()) {
          if (sc.getCollection() != null) {
            selectedMetadataPane.getChildren().add(
                createMetadataChip("📚 " + sc.getCollection().getName(), 
                    () -> removeFromCollection(sound, sc)));
          }
        }
      }

      // Tags
      if (sound.getSoundTags() != null) {
        for (net.mikolas.lyra.model.SoundTag st : sound.getSoundTags()) {
          if (st.getTag() != null) {
            selectedMetadataPane.getChildren().add(
                createMetadataChip("🏷️ " + st.getTag().getName(), 
                    () -> removeTag(sound, st)));
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error updating metadata chips: " + e.getMessage());
    }
  }

  private Button createMetadataChip(String text, Runnable onRemove) {
    Button chip = new Button();
    chip.getStyleClass().add("filter-chip"); // Reuse filter chip style
    chip.setStyle("-fx-background-color: #555555;"); // Slightly different color
    
    Label textLabel = new Label(text);
    textLabel.getStyleClass().add("chip-text");
    textLabel.setTextFill(javafx.scene.paint.Color.WHITE);

    Label closeLabel = new Label(" ⓧ");
    closeLabel.getStyleClass().add("close-button");
    closeLabel.setTextFill(javafx.scene.paint.Color.WHITE);
    closeLabel.setOnMouseClicked(e -> {
      e.consume(); // Prevent click from bubbling
      onRemove.run();
    });
    
    HBox content = new HBox(textLabel, closeLabel);
    content.setSpacing(4);
    content.setAlignment(javafx.geometry.Pos.CENTER);
    chip.setGraphic(content);
    
    return chip;
  }

  private void removeFromCollection(Sound sound, net.mikolas.lyra.model.SoundCollection sc) {
    try {
      database.soundCollections.delete(sc);
      database.sounds.refresh(sound);
      updateMetadataChips(sound);
    } catch (SQLException e) {
      showError("Error", "Failed to remove from collection: " + e.getMessage());
    }
  }

  private void removeTag(Sound sound, net.mikolas.lyra.model.SoundTag st) {
    try {
      database.soundTags.delete(st);
      database.sounds.refresh(sound);
      updateMetadataChips(sound);
    } catch (SQLException e) {
      showError("Error", "Failed to remove tag: " + e.getMessage());
    }
  }

  private void updateStatusBar() {
    int total = allSounds.size();
    int filtered = filteredSounds.size();
    int selected = soundTable.getSelectionModel().getSelectedItems().size();
    
    StringBuilder status = new StringBuilder();
    status.append(String.format("%d sounds | %d filtered", total, filtered));
    
    if (!activeFilters.isEmpty()) {
      status.append(" (");
      for (int i = 0; i < activeFilters.size(); i++) {
        if (i > 0) status.append(" + ");
        SoundFilter f = activeFilters.get(i);
        status.append(f.type()).append(": ").append(f.value());
      }
      status.append(")");
    }
    
    status.append(String.format(" | %d selected", selected));
    statusLabel.setText(status.toString());
  }

  // Event handlers - File menu
  @FXML
  private void handleImport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Import Sounds");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("MIDI/SysEx Files (*.mid, *.syx)", "*.mid", "*.syx"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );

    File file = fileChooser.showOpenDialog(soundTable.getScene().getWindow());
    if (file != null) {
      try {
        ImportService.ImportResult result = importService.importFromFile(file);
        
        if (result.imported() > 0) {
          loadSounds(); // Refresh table
        }

        if (result.hasErrors()) {
          StringBuilder msg = new StringBuilder(result.getSummary());
          msg.append("\n\nSome errors occurred:\n");
          for (int i = 0; i < Math.min(5, result.errors().size()); i++) {
            msg.append("- ").append(result.errors().get(i)).append("\n");
          }
          if (result.errors().size() > 5) {
            msg.append("... and ").append(result.errors().size() - 5).append(" more.");
          }
          showInfo("Import Result", msg.toString());
        } else {
          showInfo("Import Complete", result.getSummary());
        }
      } catch (Exception e) {
        showError("Import Failed", "Error importing file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void handleExportSelected() {
    List<Sound> selected = soundTable.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      showError("No Selection", "Please select at least one sound to export.");
      return;
    }
    performExport(selected, "selected_sounds");
  }

  @FXML
  private void handleExportBank() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || selected.getBank() == null) {
      showError("No Bank Selected", "Please select a sound to determine which bank to export.");
      return;
    }
    int bank = selected.getBank();
    List<Sound> bankSounds = allSounds.stream()
        .filter(s -> s.getBank() != null && s.getBank() == bank)
        .toList();
    performExport(bankSounds, "bank_" + (char)('A' + bank));
  }

  @FXML
  private void handleExportAll() {
    performExport(new ArrayList<>(allSounds), "all_sounds");
  }

  private void performExport(List<Sound> sounds, String defaultName) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Export Sounds");
    fileChooser.setInitialFileName(defaultName);
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("MIDI Files (*.mid)", "*.mid"),
        new FileChooser.ExtensionFilter("SysEx Files (*.syx)", "*.syx")
    );

    File file = fileChooser.showSaveDialog(soundTable.getScene().getWindow());
    if (file != null) {
      try {
        if (file.getName().toLowerCase().endsWith(".mid")) {
          exportService.exportToMid(sounds, file);
        } else {
          exportService.exportToSyx(sounds, file);
        }
        showInfo("Export Complete", "Successfully exported " + sounds.size() + " sounds to " + file.getName());
      } catch (Exception e) {
        showError("Export Failed", "Error exporting sounds: " + e.getMessage());
      }
    }
  }

  @FXML
  private void handleExportList() {
    showNotImplemented("Export List");
  }

  @FXML
  private void handleQuit() {
    javafx.application.Platform.exit();
  }

  // Event handlers - Library menu
  @FXML
  private void handleNewCollection() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("New Collection");
    dialog.setHeaderText("Create a new collection");
    dialog.setContentText("Name:");

    dialog.showAndWait().ifPresent(name -> {
      if (name.trim().isEmpty()) return;
      try {
        net.mikolas.lyra.model.Collection c = net.mikolas.lyra.model.Collection.builder()
            .name(name.trim())
            .build();
        database.collections.create(c);
        setupTree(); // Refresh tree
      } catch (SQLException e) {
        showError("Error", "Failed to create collection: " + e.getMessage());
      }
    });
  }

  @FXML
  private void handleManageCollections() {
    if (database == null) return;
    try {
      List<net.mikolas.lyra.model.Collection> items = database.collections.queryForAll();
      showManagementDialog("Manage Collections", items, 
          net.mikolas.lyra.model.Collection::getName,
          (item, newName) -> {
            item.setName(newName);
            database.collections.update(item);
          },
          item -> {
            // Delete associations first
            var dbldr = database.soundCollections.deleteBuilder();
            dbldr.where().eq("collection_id", item.getId());
            dbldr.delete();
            database.collections.delete(item);
          },
          name -> {
            net.mikolas.lyra.model.Collection c = net.mikolas.lyra.model.Collection.builder()
                .name(name)
                .build();
            database.collections.create(c);
            return c;
          }
      );
      setupTree(); // Refresh tree
    } catch (SQLException e) {
      showError("Error", "Failed to manage collections: " + e.getMessage());
    }
  }

  @FXML
  private void handleNewTag() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("New Tag");
    dialog.setHeaderText("Create a new tag");
    dialog.setContentText("Name:");

    dialog.showAndWait().ifPresent(name -> {
      if (name.trim().isEmpty()) return;
      try {
        net.mikolas.lyra.model.Tag t = net.mikolas.lyra.model.Tag.builder()
            .name(name.trim())
            .build();
        database.tags.create(t);
        setupTree(); // Refresh tree
      } catch (SQLException e) {
        showError("Error", "Failed to create tag: " + e.getMessage());
      }
    });
  }

  @FXML
  private void handleManageTags() {
    if (database == null) return;
    try {
      List<net.mikolas.lyra.model.Tag> items = database.tags.queryForAll();
      showManagementDialog("Manage Tags", items, 
          net.mikolas.lyra.model.Tag::getName,
          (item, newName) -> {
            item.setName(newName);
            database.tags.update(item);
          },
          item -> {
            // Delete associations first
            var dbldr = database.soundTags.deleteBuilder();
            dbldr.where().eq("tag_id", item.getId());
            dbldr.delete();
            database.tags.delete(item);
          },
          name -> {
            net.mikolas.lyra.model.Tag t = net.mikolas.lyra.model.Tag.builder()
                .name(name)
                .build();
            database.tags.create(t);
            return t;
          }
      );
      setupTree(); // Refresh tree
    } catch (SQLException e) {
      showError("Error", "Failed to manage tags: " + e.getMessage());
    }
  }

  private <T> void showManagementDialog(String title, List<T> items, 
      java.util.function.Function<T, String> nameExtractor,
      BiConsumerThrowable<T, String> onRename,
      ConsumerThrowable<T> onDelete,
      FunctionThrowable<String, T> onAdd) {
    
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    
    ListView<T> listView = new ListView<>(FXCollections.observableArrayList(items));
    listView.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : nameExtractor.apply(item));
      }
    });

    Button newBtn = new Button("New...");
    Button renameBtn = new Button("Rename...");
    Button deleteBtn = new Button("Delete");
    deleteBtn.getStyleClass().add("danger");

    renameBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
    deleteBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());

    newBtn.setOnAction(e -> {
      TextInputDialog nameDialog = new TextInputDialog();
      nameDialog.setTitle("New Item");
      nameDialog.setHeaderText("Create new item");
      nameDialog.setContentText("Name:");
      nameDialog.showAndWait().ifPresent(name -> {
        if (name.trim().isEmpty()) return;
        try {
          T newItem = onAdd.apply(name.trim());
          if (newItem != null) {
            listView.getItems().add(newItem);
          }
        } catch (Exception ex) {
          showError("Error", "Failed to create: " + ex.getMessage());
        }
      });
    });

    renameBtn.setOnAction(e -> {
      T selected = listView.getSelectionModel().getSelectedItem();
      if (selected == null) return;
      
      TextInputDialog nameDialog = new TextInputDialog(nameExtractor.apply(selected));
      nameDialog.setTitle("Rename");
      nameDialog.setHeaderText("Rename item");
      nameDialog.showAndWait().ifPresent(newName -> {
        try {
          onRename.accept(selected, newName);
          listView.refresh();
        } catch (Exception ex) {
          showError("Error", "Failed to rename: " + ex.getMessage());
        }
      });
    });

    deleteBtn.setOnAction(e -> {
      T selected = listView.getSelectionModel().getSelectedItem();
      if (selected == null) return;

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirm Delete");
      confirm.setHeaderText("Delete " + nameExtractor.apply(selected) + "?");
      confirm.setContentText("This will remove all associations with sounds.");
      confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          try {
            onDelete.accept(selected);
            listView.getItems().remove(selected);
          } catch (Exception ex) {
            showError("Error", "Failed to delete: " + ex.getMessage());
          }
        }
      });
    });

    VBox layout = new VBox(10, listView, new HBox(10, newBtn, renameBtn, deleteBtn));
    layout.setPadding(new javafx.geometry.Insets(10));
    dialog.getDialogPane().setContent(layout);
    dialog.showAndWait();
  }

  @FunctionalInterface
  private interface BiConsumerThrowable<T, U> {
    void accept(T t, U u) throws Exception;
  }

  @FunctionalInterface
  private interface ConsumerThrowable<T> {
    void accept(T t) throws Exception;
  }

  @FunctionalInterface
  private interface FunctionThrowable<T, R> {
    R apply(T t) throws Exception;
  }

  @FXML
  private void handleFindDuplicates() {
    showNotImplemented("Find Duplicates");
  }

  // Event handlers - Dump menu
  @FXML
  private void handleDumpFrom() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("DumpDialogView.fxml"));
      Parent root = loader.load();
      
      DumpDialogController controller = loader.getController();
      controller.setDatabase(database);
      
      Stage dialog = new Stage();
      dialog.setTitle("Dump Sounds from Blofeld");
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(soundTable.getScene().getWindow());
      dialog.setScene(new Scene(root));
      dialog.showAndWait();
      
      // Reload sounds after dump
      loadSounds();
    } catch (Exception e) {
      showError("Error", "Failed to open dump dialog: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleDumpToSelected() {
    List<Sound> selected = soundTable.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      showError("No Selection", "Please select at least one sound to dump.");
      return;
    }
    performDumpTo(selected);
  }

  @FXML
  private void handleDumpToBank() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || selected.getBank() == null) {
      showError("No Bank Selected", "Please select a sound to determine which bank to dump.");
      return;
    }
    int bank = selected.getBank();
    List<Sound> bankSounds = allSounds.stream()
        .filter(s -> s.getBank() != null && s.getBank() == bank)
        .toList();
    performDumpTo(bankSounds);
  }

  @FXML
  private void handleDumpToAll() {
    performDumpTo(new ArrayList<>(allSounds));
  }

  private void performDumpTo(List<Sound> sounds) {
    MidiService midi = MidiManager.getInstance().getService();
    if (midi == null || !midi.isConnected()) {
      showError("MIDI Error", "Not connected to Blofeld hardware.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirm Dump");
    alert.setHeaderText("Dump " + sounds.size() + " sounds to Blofeld?");
    alert.setContentText("This will overwrite existing sounds in the Blofeld hardware.");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        // Run in background thread to not block UI
        new Thread(() -> {
          try {
            int count = 0;
            for (Sound sound : sounds) {
              midi.sendSoundDump(sound);
              count++;
              // Small delay between sends to avoid clogging hardware buffers
              Thread.sleep(100); 
            }
            final int finalCount = count;
            javafx.application.Platform.runLater(() -> 
                showInfo("Dump Complete", "Successfully dumped " + finalCount + " sounds to Blofeld."));
          } catch (Exception e) {
            javafx.application.Platform.runLater(() -> 
                showError("Dump Failed", "Error dumping sounds: " + e.getMessage()));
          }
        }).start();
      }
    });
  }

  // Event handlers - Tools menu
  @FXML
  private void handleSoundEditor() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showError("No Sound Selected", "Please select a sound to edit.");
      return;
    }
    handleEdit();
  }

  @FXML
  private void handleMultiEditor() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiEditor.fxml"));
      Parent root = loader.load();
      
      MultiEditorController controller = loader.getController();
      // We'll need a way to get/create a MultiPatch to edit.
      // For now, let's create a new default one.
      controller.setMultiPatch(new net.mikolas.lyra.model.MultiPatch());
      
      Stage stage = new Stage();
      stage.setTitle("Lyra - Multi Editor");
      stage.setScene(new Scene(root));
      stage.setResizable(false);
      stage.show();
    } catch (Exception e) {
      showError("Error", "Failed to open Multi Editor: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleWavetableEditor() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("WavetableEditor.fxml"));
      Parent root = loader.load();
      
      Stage stage = new Stage();
      stage.setTitle("Lyra - Wavetable Editor");
      stage.setScene(new Scene(root));
      stage.setMinWidth(640);
      stage.setMinHeight(580);
      stage.show();
    } catch (Exception e) {
      showError("Error", "Failed to open Wavetable Editor: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleSettings() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/mikolas/lyra/ui/SettingsView.fxml"));
      Parent root = loader.load();
      
      Stage stage = new Stage();
      stage.setTitle("Settings");
      stage.setScene(new Scene(root));
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.initOwner(soundTable.getScene().getWindow());
      stage.showAndWait();
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText("Failed to open Settings");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
    }
  }

  // Event handlers - Help menu
  @FXML
  private void handleHelp() {
    showNotImplemented("Help");
  }

  @FXML
  private void handleMidiChart() {
    showNotImplemented("MIDI Chart");
  }

  @FXML
  private void handleAbout() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("About Lyra");
    alert.setHeaderText("Lyra - Waldorf Blofeld Sound Editor");
    alert.setContentText("Version 0.3.0\n\nModern sound editor and librarian for Waldorf Blofeld.");
    alert.showAndWait();
  }

  // Event handlers - Details panel
  @FXML
  private void handleRename() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || database == null) return;

    TextInputDialog dialog = new TextInputDialog(selected.getName());
    dialog.setTitle("Rename Sound");
    dialog.setHeaderText("Enter new name for: " + selected.getName());
    dialog.setContentText("Name (max 16 chars):");

    dialog.showAndWait().ifPresent(newName -> {
      if (newName.trim().isEmpty()) return;
      String finalName = newName.trim();
      if (finalName.length() > 16) finalName = finalName.substring(0, 16);
      
      try {
        selected.setNameAndSyncParameters(finalName);
        database.sounds.update(selected);
        soundTable.refresh();
        updateSelection(selected);
      } catch (SQLException e) {
        showError("Error", "Failed to rename sound: " + e.getMessage());
      }
    });
  }

  @FXML
  private void handleDuplicate() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || database == null) return;

    try {
      Sound clone = selected.cloneSound();
      clone.setNameAndSyncParameters(clone.getName() + " copy");
      // Clear bank/program to avoid unique constraint if it was assigned
      clone.setBank(null);
      clone.setProgram(null);
      
      database.sounds.create(clone);
      allSounds.add(clone);
      soundTable.getSelectionModel().select(clone);
      updateStatusBar();
    } catch (SQLException e) {
      showError("Error", "Failed to duplicate sound: " + e.getMessage());
    }
  }

  @FXML
  private void handleDelete() {
    Sound selected = soundTable.getSelectionModel().getSelectedItem();
    if (selected == null || database == null) {
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Sound");
    alert.setHeaderText("Delete sound: " + selected.getName() + "?");
    alert.setContentText("This will permanently remove the sound from the database.");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          // 1. Delete associations first
          var scBldr = database.soundCollections.deleteBuilder();
          scBldr.where().eq("sound_id", selected.getId());
          scBldr.delete();

          var stBldr = database.soundTags.deleteBuilder();
          stBldr.where().eq("sound_id", selected.getId());
          stBldr.delete();

          // 2. Delete the sound
          database.sounds.delete(selected);
          
          // 3. Update UI
          allSounds.remove(selected);
          updateStatusBar();
        } catch (SQLException e) {
          showError("Error", "Failed to delete sound: " + e.getMessage());
        }
      }
    });
  }

  private void showNotImplemented(String feature) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not Implemented");
    alert.setHeaderText(feature);
    alert.setContentText("This feature is not yet implemented.");
    alert.showAndWait();
  }

  private void showInfo(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  private void showError(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
