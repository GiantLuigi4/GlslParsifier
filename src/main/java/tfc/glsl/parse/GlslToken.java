package tfc.glsl.parse;

public class GlslToken {
    private final TokenType type;
    private final String data;

    public GlslToken(TokenType type) {
        this.type = type;
        this.data = type.text;
    }

    public GlslToken(TokenType type, String data) {
        this.type = type;
        this.data = data;
    }

    public boolean is(TokenType tokenType) {
        return type == tokenType;
    }

    public boolean is(String text) {
        return data != null && data.equals(text);
    }

    public boolean is(TokenGroup group) {
        return group.isIn(type);
    }

    public boolean is(char character) {
        return data != null && data.length() == 1 && data.charAt(0) == character;
    }

    @Override
    public String toString() {
        return data;
    }

    public String string() {
        return data;
    }

    public TokenType type() {
        return type;
    }
}
