package net.mikolas.lyra.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.mikolas.lyra.midi.MidiManager;
import net.mikolas.lyra.midi.MidiService;
import net.mikolas.lyra.model.AppSettings;
import net.mikolas.lyra.model.AutosaveMode;
import net.mikolas.lyra.model.MidiFilterMode;

import javax.sound.midi.MidiDevice;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Controller for Settings dialog.
 */
public class SettingsController {

  @FXML private TreeView<String> navigationTree;
  @FXML private Label pageTitle;
  
  // Pages
  @FXML private VBox midiConnectionsPage;
  @FXML private VBox midiConfigPage;
  @FXML private VBox databasePage;
  @FXML private VBox editorPage;
  @FXML private VBox miscPage;
  
  // MIDI Connections
  @FXML private ListView<String> inputDevicesList;
  @FXML private ListView<String> outputDevicesList;
  @FXML private CheckBox autoConnectCheck;
  @FXML private Button refreshDevicesBtn;
  
  // MIDI Configuration
  @FXML private Spinner<Integer> deviceIdSpinner;
  @FXML private Label deviceIdHexLabel;
  @FXML private Button broadcastBtn;
  @FXML private Button detectBtn;
  @FXML private ChannelSelector sendChannelSelector;
  @FXML private ChannelSelector receiveChannelSelector;
  
  // Database
  @FXML private TextField dbPathField;
  @FXML private Button dbBrowseBtn;
  @FXML private Button dbRestoreBtn;
  @FXML private CheckBox backupEnabledCheck;
  @FXML private Spinner<Integer> backupIntervalSpinner;
  
  // Miscellaneous
  @FXML private CheckBox singleClickProgramChangeCheck;
  @FXML private CheckBox updateCheckCheck;
  
  // Editor
  @FXML private ComboBox<String> autosaveCombo;
  @FXML private ComboBox<String> midiReceiveCombo;
  @FXML private ComboBox<String> midiSendCombo;
  
  // Buttons
  @FXML private Button cancelBtn;
  @FXML private Button okBtn;
  
  private AppSettings settings;
  private Preferences prefs;
  private MidiDevice.Info selectedInputDevice;
  private MidiDevice.Info selectedOutputDevice;
  private List<MidiDevice.Info> availableDevices;

  @FXML
  public void initialize() {
    settings = new AppSettings();
    prefs = Preferences.userNodeForPackage(SettingsController.class);
    
    setupNavigationTree();
    setupMidiConnections();
    setupMidiConfiguration();
    setupDatabaseSettings();
    setupEditorSettings();
    setupMiscSettings();
    setupButtons();
    
    // Show MIDI Connections by default
    showPage("MIDI Connections");
  }

  private void setupNavigationTree() {
    TreeItem<String> root = new TreeItem<>("Settings");
    root.setExpanded(true);
    
    root.getChildren().addAll(
        new TreeItem<>("MIDI Connections"),
        new TreeItem<>("MIDI Configuration"),
        new TreeItem<>("Database"),
        new TreeItem<>("Editor"),
        new TreeItem<>("Miscellaneous")
    );
    
    navigationTree.setRoot(root);
    navigationTree.setShowRoot(false);
    
    navigationTree.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) {
            showPage(newVal.getValue());
          }
        });
  }

  private void setupMidiConnections() {
    // Configure list views for device selection
    inputDevicesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    outputDevicesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    
    // Load auto-connect preference
    autoConnectCheck.setSelected(prefs.getBoolean("autoConnectBlofeld", true));
    
    // Refresh devices
    refreshDevicesBtn.setOnAction(e -> loadMidiDevices());
    
    // Handle device selection
    inputDevicesList.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) {
            int index = inputDevicesList.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < availableDevices.size()) {
              selectedInputDevice = availableDevices.get(index);
            }
          }
        });
    
    outputDevicesList.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) {
            int index = outputDevicesList.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < availableDevices.size()) {
              selectedOutputDevice = availableDevices.get(index);
            }
          }
        });
    
    // Load devices
    loadMidiDevices();
  }

  private void setupMidiConfiguration() {
    // Device ID spinner (0-127)
    SpinnerValueFactory<Integer> valueFactory = 
        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 127, 0);
    deviceIdSpinner.setValueFactory(valueFactory);
    
    // Update hex label when value changes
    deviceIdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
      deviceIdHexLabel.setText(String.format("(%02Xh)", newVal));
    });
    
    // Broadcast button sets to 127
    broadcastBtn.setOnAction(e -> deviceIdSpinner.getValueFactory().setValue(127));
    
    // Load channel settings
    sendChannelSelector.setSelectedChannels(settings.getSendChannels());
    receiveChannelSelector.setSelectedChannels(settings.getReceiveChannels());
    
    // Detect button (mockup - would query Blofeld)
    detectBtn.setOnAction(e -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Detect from Blofeld");
      alert.setHeaderText("Detection not yet implemented");
      alert.setContentText("This feature will query the connected Blofeld for its device ID and channel settings.");
      alert.showAndWait();
    });
    
    // Load saved device ID
    int savedDeviceId = settings.getDeviceId();
    deviceIdSpinner.getValueFactory().setValue(savedDeviceId);
  }

  private void setupButtons() {
    okBtn.setOnAction(e -> applySettings());
    cancelBtn.setOnAction(e -> closeDialog());
  }

  private void setupDatabaseSettings() {
    // Load current settings
    dbPathField.setText(settings.getDatabasePath());
    backupEnabledCheck.setSelected(settings.isDatabaseBackupEnabled());
    backupIntervalSpinner.getValueFactory().setValue(settings.getBackupIntervalMinutes());
    
    // Browse button
    dbBrowseBtn.setOnAction(e -> {
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Select Database Directory");
      
      File currentPath = new File(settings.getDatabasePath()).getParentFile();
      if (currentPath != null && currentPath.exists()) {
        chooser.setInitialDirectory(currentPath);
      }
      
      File selected = chooser.showDialog(dbBrowseBtn.getScene().getWindow());
      if (selected != null) {
        String newPath = selected.getAbsolutePath() + "/lyra.db";
        dbPathField.setText(newPath);
      }
    });
    
    // Restore default button
    dbRestoreBtn.setOnAction(e -> {
      String home = System.getProperty("user.home");
      String defaultPath = home + "/.lyra/lyra.db";
      dbPathField.setText(defaultPath);
    });
    
    // Enable/disable interval spinner based on backup checkbox
    backupIntervalSpinner.setDisable(!backupEnabledCheck.isSelected());
    backupEnabledCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
      backupIntervalSpinner.setDisable(!newVal);
    });
  }

  private void setupEditorSettings() {
    // Autosave combo
    autosaveCombo.getItems().addAll(
        "Always off", "Always on", "Remember last state");
    
    // Load current setting
    switch (settings.getAutosaveMode()) {
      case ALWAYS_OFF -> autosaveCombo.setValue("Always off");
      case ALWAYS_ON -> autosaveCombo.setValue("Always on");
      case REMEMBER_LAST -> autosaveCombo.setValue("Remember last state");
    }
    
    // MIDI Receive combo
    midiReceiveCombo.getItems().addAll(
        "None", "Ctrl/SysEx events", "Program changes", "All events");
    
    // Load current setting
    switch (settings.getMidiReceiveMode()) {
      case NONE -> midiReceiveCombo.setValue("None");
      case CTRL_SYSEX -> midiReceiveCombo.setValue("Ctrl/SysEx events");
      case PROGRAM_CHANGES -> midiReceiveCombo.setValue("Program changes");
      case ALL_EVENTS -> midiReceiveCombo.setValue("All events");
    }
    
    // MIDI Send combo
    midiSendCombo.getItems().addAll(
        "None", "Ctrl/SysEx events", "Program changes", "All events");
    
    // Load current setting
    switch (settings.getMidiSendMode()) {
      case NONE -> midiSendCombo.setValue("None");
      case CTRL_SYSEX -> midiSendCombo.setValue("Ctrl/SysEx events");
      case PROGRAM_CHANGES -> midiSendCombo.setValue("Program changes");
      case ALL_EVENTS -> midiSendCombo.setValue("All events");
    }
  }

  private void setupMiscSettings() {
    // Load current settings
    singleClickProgramChangeCheck.setSelected(settings.isSingleClickProgramChange());
    updateCheckCheck.setSelected(settings.isUpdateCheckEnabled());
  }

  private void loadMidiDevices() {
    MidiService midi = MidiManager.getInstance().getService();
    if (midi == null) {
      inputDevicesList.getItems().add("No MIDI service available");
      outputDevicesList.getItems().add("No MIDI service available");
      return;
    }
    
    try {
      availableDevices = midi.listDevices();
      
      inputDevicesList.getItems().clear();
      outputDevicesList.getItems().clear();
      
      if (availableDevices.isEmpty()) {
        inputDevicesList.getItems().add("No MIDI devices found");
        outputDevicesList.getItems().add("No MIDI devices found");
        return;
      }
      
      for (MidiDevice.Info device : availableDevices) {
        String name = device.getName();
        if (name.toLowerCase().contains("blofeld")) {
          name += " ★"; // Mark Blofeld devices
        }
        inputDevicesList.getItems().add(name);
        outputDevicesList.getItems().add(name);
      }
      
      // Select currently connected device
      if (midi.isConnected()) {
        // Would need to track which device is connected
        // For now, just select first Blofeld
        for (int i = 0; i < availableDevices.size(); i++) {
          if (availableDevices.get(i).getName().toLowerCase().contains("blofeld")) {
            inputDevicesList.getSelectionModel().select(i);
            outputDevicesList.getSelectionModel().select(i);
            break;
          }
        }
      }
    } catch (Exception e) {
      inputDevicesList.getItems().add("Error loading devices: " + e.getMessage());
      outputDevicesList.getItems().add("Error loading devices: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void showPage(String pageName) {
    // Hide all pages
    midiConnectionsPage.setVisible(false);
    midiConfigPage.setVisible(false);
    databasePage.setVisible(false);
    editorPage.setVisible(false);
    miscPage.setVisible(false);
    
    // Show selected page
    pageTitle.setText(pageName);
    
    switch (pageName) {
      case "MIDI Connections" -> midiConnectionsPage.setVisible(true);
      case "MIDI Configuration" -> midiConfigPage.setVisible(true);
      case "Database" -> databasePage.setVisible(true);
      case "Editor" -> editorPage.setVisible(true);
      case "Miscellaneous" -> miscPage.setVisible(true);
    }
  }

  private void applySettings() {
    // Save database settings
    settings.setDatabasePath(dbPathField.getText());
    settings.setDatabaseBackupEnabled(backupEnabledCheck.isSelected());
    settings.setBackupIntervalMinutes(backupIntervalSpinner.getValue());
    
    // Save MIDI preferences
    settings.setAutoConnectEnabled(autoConnectCheck.isSelected());
    settings.setDeviceId(deviceIdSpinner.getValue());
    settings.setSendChannels(sendChannelSelector.getSelectedChannels());
    settings.setReceiveChannels(receiveChannelSelector.getSelectedChannels());
    
    // Save editor settings
    settings.setAutosaveMode(parseAutosaveMode(autosaveCombo.getValue()));
    settings.setMidiReceiveMode(parseMidiFilterMode(midiReceiveCombo.getValue()));
    settings.setMidiSendMode(parseMidiFilterMode(midiSendCombo.getValue()));
    
    // Save miscellaneous settings
    settings.setSingleClickProgramChange(singleClickProgramChangeCheck.isSelected());
    settings.setUpdateCheckEnabled(updateCheckCheck.isSelected());
    
    // Reconnect to selected device if changed
    if (selectedInputDevice != null || selectedOutputDevice != null) {
      try {
        MidiDevice.Info deviceToConnect = selectedOutputDevice != null 
            ? selectedOutputDevice 
            : selectedInputDevice;
        
        if (deviceToConnect != null) {
          MidiManager.getInstance().reconnect(deviceToConnect);
          
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("MIDI Connection");
          alert.setHeaderText("Connected successfully");
          alert.setContentText("Connected to: " + deviceToConnect.getName());
          alert.showAndWait();
        }
      } catch (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("MIDI Connection Error");
        alert.setHeaderText("Failed to connect");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
      }
    }
    
    closeDialog();
  }

  private AutosaveMode parseAutosaveMode(String value) {
    return switch (value) {
      case "Always off" -> AutosaveMode.ALWAYS_OFF;
      case "Always on" -> AutosaveMode.ALWAYS_ON;
      default -> AutosaveMode.REMEMBER_LAST;
    };
  }

  private MidiFilterMode parseMidiFilterMode(String value) {
    return switch (value) {
      case "None" -> MidiFilterMode.NONE;
      case "Ctrl/SysEx events" -> MidiFilterMode.CTRL_SYSEX;
      case "Program changes" -> MidiFilterMode.PROGRAM_CHANGES;
      default -> MidiFilterMode.ALL_EVENTS;
    };
  }

  private void closeDialog() {
    Stage stage = (Stage) cancelBtn.getScene().getWindow();
    stage.close();
  }
}
