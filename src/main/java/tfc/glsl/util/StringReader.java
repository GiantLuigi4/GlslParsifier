package tfc.glsl.util;

public class StringReader {
    String text;
    int cursor;
    int len;

    public StringReader(String text) {
        this.text = text;
        this.len = text.length();
    }

    public char charAt(int i) {
        return text.charAt(cursor + i);
    }

    public char peek() {
        return text.charAt(cursor);
    }

    public char peek(int i) {
        if (cursor + i > len) return 0;
        return text.charAt(cursor + i);
    }

    public void advance() {
        cursor++;
    }

    public void skip(int count) {
        cursor += count;
    }

    public boolean isEmpty() {
        return cursor >= len;
    }

    public void skipWS() {
        if (isEmpty()) return;
        char c = charAt(0);
        while (Character.isWhitespace(c)) {
            cursor++;
            if (isEmpty()) return;
            c = charAt(0);
        }
    }

    @Override
    public String toString() {
        return text.substring(cursor);
    }

    private int remaining() {
        return len - cursor;
    }

    public boolean startsWith(String other) {
        // can't start with a string which is longer than the remaining text
        if (other.length() > remaining())
            return false;

        char[] otherChrs = other.toCharArray();
        for (int i = 0; i < otherChrs.length; i++) {
            // not a match
            if (charAt(i) != otherChrs[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWithWs() {
        return Character.isWhitespace(charAt(0));
    }

    public int index() {
        return cursor;
    }

    public String absSubstring(int start, int end) {
        return text.substring(start, end);
    }

    public boolean whitespaceAt(int i) {
        if (cursor + i > len) return true;
        return Character.isWhitespace(text.charAt(cursor + i));
    }
}
