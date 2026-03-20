package com.jef.justenoughfakepixel.features.scoreboard;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboardAPI {

    private static List<String> lastLines = new ArrayList<>();

    public static void update(List<String> lines) {
        lastLines = new ArrayList<>(lines);
    }

    public static List<String> getLines() {
        return new ArrayList<>(lastLines);
    }

    public static List<String> getUnclaimed() {
        List<String> out = new ArrayList<>();
        for (UnknownLinesHandler.Entry e : UnknownLinesHandler.recent) {
            out.add(e.line);
        }
        return out;
    }

    public static String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"lines\":[");
        for (int i = 0; i < lastLines.size(); i++) {
            sb.append("\"").append(escape(lastLines.get(i))).append("\"");
            if (i < lastLines.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"unclaimed\":[");
        List<String> un = getUnclaimed();
        for (int i = 0; i < un.size(); i++) {
            sb.append("\"").append(escape(un.get(i))).append("\"");
            if (i < un.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}