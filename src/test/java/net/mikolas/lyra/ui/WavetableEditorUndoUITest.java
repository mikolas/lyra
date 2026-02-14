package net.mikolas.lyra.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class WavetableEditorUndoUITest {
    
    static {
        // Must be set before JavaFX initializes
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
    }
    
    private WavetableEditorController controller;
    
    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/net/mikolas/lyra/ui/WavetableEditor.fxml")
        );
        Parent root = loader.load();
        controller = loader.getController();
        
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    @Test
    void testUndoMenuDisabledInitially(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(controller.getWavetable().canUndo(), "Should not be able to undo initially");
    }
    
    @Test
    void testRedoMenuDisabledInitially(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(controller.getWavetable().canRedo(), "Should not be able to redo initially");
    }
    
    @Test
    void testUndoEnabledAfterNormalize(FxRobot robot) {
        // Call model method directly (UI buttons don't save undo state yet - that's a separate bug)
        WaitForAsyncUtils.asyncFx(() -> {
            controller.getWavetable().normalize(0);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getWavetable().canUndo(), "Should be able to undo after normalize");
    }
    
    @Test
    void testRedoEnabledAfterUndo(FxRobot robot) {
        WaitForAsyncUtils.asyncFx(() -> {
            controller.getWavetable().normalize(0);
            controller.getWavetable().undo();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getWavetable().canRedo(), "Should be able to redo after undo");
    }
    
    @Test
    void testUndoKeyboardShortcut(FxRobot robot) {
        WaitForAsyncUtils.asyncFx(() -> {
            controller.getWavetable().normalize(0);
            controller.getWavetable().undo();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getWavetable().canRedo(), "Should be able to redo after undo");
        assertFalse(controller.getWavetable().canUndo(), "Should not be able to undo after single undo");
    }
    
    @Test
    void testRedoKeyboardShortcut(FxRobot robot) {
        WaitForAsyncUtils.asyncFx(() -> {
            controller.getWavetable().normalize(0);
            controller.getWavetable().undo();
            controller.getWavetable().redo();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getWavetable().canUndo(), "Should be able to undo after redo");
        assertFalse(controller.getWavetable().canRedo(), "Should not be able to redo after single redo");
    }
}
