package com.skyblockqol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses Hypixel skill level-up / XP messages to pin exact levels. */
public final class LevelParser {
    private LevelParser() {}

    // "SKILL LEVEL UP Farming 24➜25", "Farming 24->25", "Farming 24 » 25"
    private static final Pattern LEVEL_UP = Pattern.compile(
        "(?i)(?:skill level up\\s+)?([A-Za-z]+)\\s+([0-9]+)\\s*(?:\\u279c|->|\\u00bb|\\u2192|\\u27a4)\\s*([0-9]+)");

    public record LevelUp(String skill, int from, int to) {}

    /** Returns the level-up if the message is one, else null. */
    public static LevelUp parse(String raw) {
        String s = Util.stripCodes(raw).trim();
        Matcher m = LEVEL_UP.matcher(s);
        if (!m.find()) return null;
        try {
            return new LevelUp(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}