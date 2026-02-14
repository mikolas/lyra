package net.mikolas.lyra.midi;

import java.util.HashMap;
import java.util.Map;

/**
 * Bidirectional mapping between MIDI CC (Control Change) and Blofeld SysEx parameters.
 *
 * <p>The Blofeld supports 118 CC-controllable parameters for real-time control. This mapper
 * provides bidirectional conversion between CC numbers (0-127) and parameter IDs (0-384).
 *
 * <p>Usage:
 *
 * <pre>{@code
 * CCMapper mapper = new CCMapper();
 * int paramId = mapper.ccToParameter(74);  // CC 74 -> Filter1 Cutoff (param 88)
 * int cc = mapper.parameterToCC(88);       // Param 88 -> CC 74
 * }</pre>
 */
public class CCMapper {
  private final Map<Integer, Integer> ccToParam = new HashMap<>();
  private final Map<Integer, Integer> paramToCC = new HashMap<>();

  public CCMapper() {
    initMappings();
  }

  /**
   * Maps CC number to parameter ID.
   *
   * @param cc CC number (0-127)
   * @return parameter ID (0-384), or -1 if not mapped
   */
  public int ccToParameter(int cc) {
    return ccToParam.getOrDefault(cc, -1);
  }

  /**
   * Maps parameter ID to CC number.
   *
   * @param paramId parameter ID (0-384)
   * @return CC number (0-127), or -1 if not mapped
   */
  public int parameterToCC(int paramId) {
    return paramToCC.getOrDefault(paramId, -1);
  }

  /**
   * Checks if CC number is mapped to a parameter.
   *
   * @param cc CC number
   * @return true if mapped
   */
  public boolean isCCMapped(int cc) {
    return ccToParam.containsKey(cc);
  }

  /**
   * Checks if parameter ID is mapped to a CC.
   *
   * @param paramId parameter ID
   * @return true if mapped
   */
  public boolean isParameterMapped(int paramId) {
    return paramToCC.containsKey(paramId);
  }

  private void initMappings() {
    // 118 CC-to-parameter mappings from Blofeld specification
    addMapping(5, 57); // glide
    addMapping(12, 316);
    addMapping(13, 323);
    addMapping(14, 311); // arp
    addMapping(15, 160);
    addMapping(16, 161);
    addMapping(17, 163);
    addMapping(18, 166); // lfo 1
    addMapping(19, 172);
    addMapping(20, 173);
    addMapping(21, 175);
    addMapping(22, 178); // lfo 2
    addMapping(23, 184);
    addMapping(24, 185);
    addMapping(25, 187);
    addMapping(26, 190); // lfo 3
    addMapping(27, 1);
    addMapping(28, 2);
    addMapping(29, 3);
    addMapping(30, 7);
    addMapping(31, 8);
    addMapping(33, 9);
    addMapping(34, 11); // osc 1
    addMapping(35, 17);
    addMapping(36, 18);
    addMapping(37, 19);
    addMapping(38, 23);
    addMapping(39, 24);
    addMapping(40, 25);
    addMapping(41, 27); // osc 2
    addMapping(42, 33);
    addMapping(43, 34);
    addMapping(44, 35);
    addMapping(45, 39);
    addMapping(46, 40);
    addMapping(47, 41);
    addMapping(48, 43); // osc 3
    addMapping(49, 49); // sync
    addMapping(50, 51); // pitchmod
    addMapping(51, 56); // glide mode
    addMapping(52, 61);
    addMapping(53, 62); // osc 1 lev/bal
    addMapping(54, 71);
    addMapping(55, 72); // ringmod lev/bal
    addMapping(56, 63);
    addMapping(57, 64); // osc 2 lev/bal
    addMapping(58, 65);
    addMapping(59, 66); // osc 3 lev/bal
    addMapping(60, 67);
    addMapping(61, 68);
    addMapping(62, 69); // noise lev/bal/col
    addMapping(65, 53); // glide active
    addMapping(67, 117); // filter routing
    addMapping(68, 77);
    addMapping(69, 78);
    addMapping(70, 80);
    addMapping(71, 81);
    addMapping(72, 86);
    addMapping(73, 87); // filter 1
    addMapping(74, 88);
    addMapping(75, 90);
    addMapping(76, 92);
    addMapping(77, 93);
    addMapping(78, 95);
    addMapping(79, 97);
    addMapping(80, 98);
    addMapping(81, 100);
    addMapping(82, 101);
    addMapping(83, 106);
    addMapping(84, 107); // filter 2
    addMapping(85, 108);
    addMapping(86, 110);
    addMapping(87, 112);
    addMapping(88, 113);
    addMapping(89, 115);
    addMapping(90, 121);
    addMapping(91, 122);
    addMapping(92, 124);
    addMapping(93, 129);
    addMapping(94, 145);
    addMapping(95, 199); // fil env
    addMapping(96, 201);
    addMapping(97, 202);
    addMapping(98, 203);
    addMapping(99, 204);
    addMapping(100, 205);
    addMapping(101, 211);
    addMapping(102, 213);
    addMapping(103, 214);
    addMapping(104, 215);
    addMapping(105, 216);
    addMapping(106, 217); // amp env
    addMapping(107, 223);
    addMapping(108, 225);
    addMapping(109, 226);
    addMapping(110, 227);
    addMapping(111, 228);
    addMapping(112, 229); // env3 env
    addMapping(113, 235);
    addMapping(114, 237);
    addMapping(115, 238);
    addMapping(116, 239);
    addMapping(117, 240);
    addMapping(118, 241); // env4 env
  }

  private void addMapping(int cc, int paramId) {
    ccToParam.put(cc, paramId);
    paramToCC.put(paramId, cc);
  }
}
