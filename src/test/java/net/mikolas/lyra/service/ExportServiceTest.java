package net.mikolas.lyra.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import net.mikolas.lyra.model.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportServiceTest {
  private ExportService exportService;
  private Sound testSound;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    exportService = new ExportService();
    byte[] params = new byte[385];
    params[365] = 'T'; // Name: Test
    params[366] = 'e';
    params[367] = 's';
    params[368] = 't';
    testSound = Sound.builder()
        .name("Test")
        .bank(0)
        .program(0)
        .parameters(params)
        .build();
  }

  @Test
  void testExportToSyx() throws Exception {
    File outputFile = tempDir.resolve("test.syx").toFile();
    exportService.exportToSyx(Collections.singletonList(testSound), outputFile);

    assertTrue(outputFile.exists());
    byte[] content = Files.readAllBytes(outputFile.toPath());
    assertEquals(392, content.length);
    assertEquals((byte) 0xF0, content[0]);
    assertEquals((byte) 0xF7, content[391]);
  }

  @Test
  void testExportMultipleToSyx() throws Exception {
    Sound sound2 = Sound.builder()
        .name("Test 2")
        .bank(0)
        .program(1)
        .parameters(new byte[385])
        .build();
    
    File outputFile = tempDir.resolve("multiple.syx").toFile();
    exportService.exportToSyx(java.util.Arrays.asList(testSound, sound2), outputFile);

    assertTrue(outputFile.exists());
    byte[] content = Files.readAllBytes(outputFile.toPath());
    assertEquals(392 * 2, content.length);
  }

  @Test
  void testExportToMid() throws Exception {
    File outputFile = tempDir.resolve("test.mid").toFile();
    exportService.exportToMid(Collections.singletonList(testSound), outputFile);

    assertTrue(outputFile.exists());
    byte[] content = Files.readAllBytes(outputFile.toPath());
    
    // Minimal SMF header: MThd
    assertEquals('M', content[0]);
    assertEquals('T', content[1]);
    assertEquals('h', content[2]);
    assertEquals('d', content[3]);
  }
}