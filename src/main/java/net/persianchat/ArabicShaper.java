package net.persianchat;

import java.text.Bidi;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts logical-order Persian/Arabic text into "visual order" text made of
 * Arabic Presentation Forms, so that clients with no RTL/shaping support
 * (Java with a non-RTL language, Bedrock) show connected letters in the right order.
 */
public final class ArabicShaper {

    private ArabicShaper() {}

    // {isolated, final, initial, medial}; initial = 0 means the letter does not join to the next one
    private static final Map<Integer, int[]> FORMS = new HashMap<>();

    private static void put(int ch, int iso, int fin, int ini, int med) {
        FORMS.put(ch, new int[]{iso, fin, ini, med});
    }

    static {
        put(0x0621, 0xFE80, 0, 0, 0);
        put(0x0622, 0xFE81, 0xFE82, 0, 0);
        put(0x0623, 0xFE83, 0xFE84, 0, 0);
        put(0x0624, 0xFE85, 0xFE86, 0, 0);
        put(0x0625, 0xFE87, 0xFE88, 0, 0);
        put(0x0626, 0xFE89, 0xFE8A, 0xFE8B, 0xFE8C);
        put(0x0627, 0xFE8D, 0xFE8E, 0, 0);
        put(0x0628, 0xFE8F, 0xFE90, 0xFE91, 0xFE92);
        put(0x0629, 0xFE93, 0xFE94, 0, 0);
        put(0x062A, 0xFE95, 0xFE96, 0xFE97, 0xFE98);
        put(0x062B, 0xFE99, 0xFE9A, 0xFE9B, 0xFE9C);
        put(0x062C, 0xFE9D, 0xFE9E, 0xFE9F, 0xFEA0);
        put(0x062D, 0xFEA1, 0xFEA2, 0xFEA3, 0xFEA4);
        put(0x062E, 0xFEA5, 0xFEA6, 0xFEA7, 0xFEA8);
        put(0x062F, 0xFEA9, 0xFEAA, 0, 0);
        put(0x0630, 0xFEAB, 0xFEAC, 0, 0);
        put(0x0631, 0xFEAD, 0xFEAE, 0, 0);
        put(0x0632, 0xFEAF, 0xFEB0, 0, 0);
        put(0x0633, 0xFEB1, 0xFEB2, 0xFEB3, 0xFEB4);
        put(0x0634, 0xFEB5, 0xFEB6, 0xFEB7, 0xFEB8);
        put(0x0635, 0xFEB9, 0xFEBA, 0xFEBB, 0xFEBC);
        put(0x0636, 0xFEBD, 0xFEBE, 0xFEBF, 0xFEC0);
        put(0x0637, 0xFEC1, 0xFEC2, 0xFEC3, 0xFEC4);
        put(0x0638, 0xFEC5, 0xFEC6, 0xFEC7, 0xFEC8);
        put(0x0639, 0xFEC9, 0xFECA, 0xFECB, 0xFECC);
        put(0x063A, 0xFECD, 0xFECE, 0xFECF, 0xFED0);
        put(0x0640, 0x0640, 0x0640, 0x0640, 0x0640); // tatweel
        put(0x0641, 0xFED1, 0xFED2, 0xFED3, 0xFED4);
        put(0x0642, 0xFED5, 0xFED6, 0xFED7, 0xFED8);
        put(0x0643, 0xFED9, 0xFEDA, 0xFEDB, 0xFEDC);
        put(0x0644, 0xFEDD, 0xFEDE, 0xFEDF, 0xFEE0);
        put(0x0645, 0xFEE1, 0xFEE2, 0xFEE3, 0xFEE4);
        put(0x0646, 0xFEE5, 0xFEE6, 0xFEE7, 0xFEE8);
        put(0x0647, 0xFEE9, 0xFEEA, 0xFEEB, 0xFEEC);
        put(0x0648, 0xFEED, 0xFEEE, 0, 0);
        put(0x0649, 0xFEEF, 0xFEF0, 0, 0);
        put(0x064A, 0xFEF1, 0xFEF2, 0xFEF3, 0xFEF4);
        // Persian letters
        put(0x067E, 0xFB56, 0xFB57, 0xFB58, 0xFB59); // pe
        put(0x0686, 0xFB7A, 0xFB7B, 0xFB7C, 0xFB7D); // che
        put(0x0698, 0xFB8A, 0xFB8B, 0, 0);           // zhe
        put(0x06A9, 0xFB8E, 0xFB8F, 0xFB90, 0xFB91); // keheh
        put(0x06AF, 0xFB92, 0xFB93, 0xFB94, 0xFB95); // gaf
        put(0x06CC, 0xFBFC, 0xFBFD, 0xFBFE, 0xFBFF); // farsi yeh
    }

    private static final int NONE = 0, RIGHT = 1, DUAL = 2, TRANSPARENT = 3;

    private static int type(int c) {
        if ((c >= 0x064B && c <= 0x065F) || c == 0x0670) return TRANSPARENT;
        int[] f = FORMS.get(c);
        if (f == null || f[1] == 0) return NONE;
        return f[2] != 0 ? DUAL : RIGHT;
    }

    public static boolean containsArabic(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0x0750 && c <= 0x077F)
                    || (c >= 0xFB50 && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF)) {
                return true;
            }
        }
        return false;
    }


    /** True when the text contains already-shaped presentation-form letters. */
    public static boolean containsPresentationForms(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 0xFB50 && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF)) return true;
        }
        return false;
    }

    /** True for text with normal Persian/Arabic letters that has not been shaped yet. */
    public static boolean needsProcessing(String s) {
        if (containsPresentationForms(s)) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0x0750 && c <= 0x077F)) return true;
        }
        return false;
    }

    /** Full pipeline: shape letters, drop ZWNJ, reorder to visual order. */
    public static String process(String logical) {
        return reorder(shape(logical).replace("\u200C", ""));
    }

    static String shape(String in) {
        int[] cps = in.codePoints().toArray();
        int n = cps.length;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int c = cps[i];
            int t = type(c);
            if (t == NONE || t == TRANSPARENT) {
                int[] f = FORMS.get(c);
                out.appendCodePoint(f != null ? f[0] : c);
                continue;
            }
            boolean prev = false;
            for (int j = i - 1; j >= 0; j--) {
                int tj = type(cps[j]);
                if (tj == TRANSPARENT) continue;
                prev = (tj == DUAL);
                break;
            }
            boolean next = false;
            for (int k = i + 1; k < n; k++) {
                int tk = type(cps[k]);
                if (tk == TRANSPARENT) continue;
                next = (tk == RIGHT || tk == DUAL);
                break;
            }
            // lam + alef ligatures
            if (c == 0x0644 && i + 1 < n) {
                int lig = ligature(cps[i + 1], prev);
                if (lig != 0) {
                    out.appendCodePoint(lig);
                    i++;
                    continue;
                }
            }
            int[] f = FORMS.get(c);
            int form;
            if (t == DUAL) {
                form = prev && next ? f[3] : prev ? f[1] : next ? f[2] : f[0];
            } else { // RIGHT
                form = prev ? f[1] : f[0];
            }
            out.appendCodePoint(form);
        }
        return out.toString();
    }

    private static int ligature(int alef, boolean prevConnects) {
        int iso, fin;
        switch (alef) {
            case 0x0622: iso = 0xFEF5; fin = 0xFEF6; break;
            case 0x0623: iso = 0xFEF7; fin = 0xFEF8; break;
            case 0x0625: iso = 0xFEF9; fin = 0xFEFA; break;
            case 0x0627: iso = 0xFEFB; fin = 0xFEFC; break;
            default: return 0;
        }
        return prevConnects ? fin : iso;
    }

    static String reorder(String s) {
        Bidi bidi = new Bidi(s, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        if (bidi.isLeftToRight()) return s;
        int n = bidi.getRunCount();
        byte[] levels = new byte[n];
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) {
            levels[i] = (byte) bidi.getRunLevel(i);
            idx[i] = i;
        }
        Bidi.reorderVisually(levels, 0, idx, 0, n);
        StringBuilder sb = new StringBuilder();
        for (Integer r : idx) {
            String run = s.substring(bidi.getRunStart(r), bidi.getRunLimit(r));
            if ((bidi.getRunLevel(r) & 1) == 1) run = reverseMirrored(run);
            sb.append(run);
        }
        return sb.toString();
    }

    private static String reverseMirrored(String run) {
        int[] cps = run.codePoints().toArray();
        StringBuilder sb = new StringBuilder();
        for (int i = cps.length - 1; i >= 0; i--) {
            sb.appendCodePoint(mirror(cps[i]));
        }
        return sb.toString();
    }

    private static int mirror(int c) {
        switch (c) {
            case '(': return ')';
            case ')': return '(';
            case '[': return ']';
            case ']': return '[';
            case '{': return '}';
            case '}': return '{';
            case '<': return '>';
            case '>': return '<';
            case 0x00AB: return 0x00BB;
            case 0x00BB: return 0x00AB;
            default: return c;
        }
    }
}
