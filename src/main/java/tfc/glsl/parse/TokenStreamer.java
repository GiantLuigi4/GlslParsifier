package tfc.glsl.parse;

import tfc.glsl.util.StringReader;

import java.util.ArrayList;
import java.util.List;

public class TokenStreamer {
    List<GlslToken> tokens = new ArrayList<>();
    StringReader reader;
    private int current = 0;

    public TokenStreamer(StringReader reader) {
        this.reader = reader;
    }

    public GlslToken current() {
        if (current >= tokens.size()) {
            nextToken();
        }
        return tokens.get(current);
    }

    public GlslToken peek(int offset) {
        int cursor = current + offset;
        while (cursor >= tokens.size()) {
            nextToken();
        }
        return tokens.get(cursor);
    }

    public void advance() {
        current++;
        if (current >= tokens.size()) {
            nextToken();
        }
    }

    private GlslToken nextToken() {
        List<GlslToken> token = GlslParsing.nextToken(reader);
        tokens.addAll(token);
        return token.get(0);
    }

    public void resolve() {
        while (!nextToken().is(TokenType.EOS)) {
        }
    }

    public void clearBuffer() {
        if (current == 0) return;

        if (tokens.size() <= current) {
            tokens.clear();
        } else {
            tokens = new ArrayList<>(tokens.subList(current, tokens.size()));
        }
        current = 0;
    }

    public boolean isDone() {
        return current().is(TokenType.EOS);
    }

    public int index() {
        return current;
    }

    public void setIndex(int idx) {
        current = idx;
    }
}
