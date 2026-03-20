package com.jef.justenoughfakepixel.features.scoreboard;

import java.util.LinkedHashSet;
import java.util.Set;

public class UnknownLinesHandler {

    public static class Entry {
        public final String line;
        public final long time;

        public Entry(String line) {
            this.line = line;
            this.time = System.currentTimeMillis();
        }
    }

    private static final Set<String> seen = new LinkedHashSet<>();
    public static final Set<Entry> recent = new LinkedHashSet<>();

    public static void handle(String line) {
        if (line == null || line.trim().isEmpty()) return;

        if (seen.add(line)) {
            Entry entry = new Entry(line);
            recent.add(entry);

            System.out.println("[SB UNKNOWN] " + line);
        }

        while (recent.size() > 50) {
            recent.remove(recent.iterator().next());
        }
    }
}