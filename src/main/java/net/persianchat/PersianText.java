package net.persianchat;

import java.util.ArrayList;
import java.util.List;

/** Text helpers for signs and books (pure Java, no server API). */
public final class PersianText {

    private PersianText() {}

    /** One sign line / title: shape it unless it was already shaped. */
    public static String processLine(String line) {
        return ArabicShaper.needsProcessing(line) ? ArabicShaper.process(line) : line;
    }

    /**
     * One book page: every line that still holds unshaped Persian is word-wrapped
     * at maxChars, and each wrapped line is shaped on its own so reading order
     * stays correct. Lines that are already shaped or are plain English stay as they are.
     */
    public static String processPage(String page, int maxChars) {
        String[] lines = page.split("\n", -1);
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            if (!ArabicShaper.needsProcessing(line)) {
                out.add(line);
                continue;
            }
            for (String piece : wrap(line, maxChars)) {
                out.add(ArabicShaper.process(piece));
            }
        }
        return String.join("\n", out);
    }

    static List<String> wrap(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            if (cur.length() > 0 && cur.length() + 1 + word.length() > maxChars) {
                result.add(cur.toString());
                cur.setLength(0);
            }
            if (cur.length() > 0) cur.append(' ');
            cur.append(word);
        }
        result.add(cur.toString());
        return result;
    }
}
