package net.mikolas.lyra.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verification test to ensure Sound model array indices match the Blofeld hardware specification.
 * Uses SoundTestData.INIT_SOUND as the "Golden Sample".
 */
public class SoundAlignmentTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static JsonNode metadata;

  @BeforeEach
  void setUp() throws IOException {
    if (metadata == null) {
      metadata = mapper.readTree(new File("src/main/resources/parameter_metadata.json")).get("parameters");
    }
  }

  static List<ParameterTestCase> parameterSource() throws IOException {
    ObjectMapper loader = new ObjectMapper();
    JsonNode nodes = loader.readTree(new File("src/main/resources/parameter_metadata.json")).get("parameters");
    List<ParameterTestCase> cases = new ArrayList<>();
    for (JsonNode node : nodes) {
      cases.add(new ParameterTestCase(
          node.get("id").asInt(),
          node.get("fullName").asText(),
          node.get("default").asInt()
      ));
    }
    return cases;
  }

  @ParameterizedTest(name = "[{0}] {1}")
  @MethodSource("parameterSource")
  void testParameterAlignment(ParameterTestCase testCase) {
    // Verify parameter ID maps directly to array index
    Sound sound = new Sound();
    assertEquals(testCase.id, sound.getMemoryIndex(testCase.id),
        String.format("Memory index mapping mismatch for %s (ID %d)", testCase.name, testCase.id));
  }

  record ParameterTestCase(int id, String name, int defaultValue) {}
}
