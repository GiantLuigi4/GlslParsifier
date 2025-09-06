package tfc.glsl.parse;

import tfc.glsl.util.StringReader;

import java.util.*;

class GlslParsing {
    static final PrefixChecker<TokenType> checker = new PrefixChecker<>();

    private static final Map<Character, GlslToken> specialTokens = new HashMap<>();

    static {
        for (TokenType value : TokenType.values()) {
            if (value.text != null)
                checker.add(value.text, value);
        }
        checker.prepare();
        char[] chrs = ",.?:&|^+-=*/!~;[](){}<>".toCharArray();
        for (char chr : chrs) {
            specialTokens.put(chr, new GlslToken(
                    TokenType.SYMBOL,
                    String.valueOf(chr)
            ));
        }
    }

    private static void popComments(StringReader reader) {
        while (true) {
            if (reader.startsWith("//")) {
                reader.skip(2);
                char c = reader.peek();
                while (c != '\n') {
                    reader.advance();
                    c = reader.peek();
                }
                reader.skipWS();
                continue;
            }
            if (reader.startsWith("/*")) {
                reader.skip(2);
                char c = reader.peek();
                while (true) {
                    if (c == '*') {
                        if (reader.startsWith("*/")) {
                            break;
                        }
                    }
                    reader.advance();
                    c = reader.peek();
                }
                reader.skipWS();
                continue;
            }
            break;
        }
    }

    private static GlslToken getSpecial(char chr) {
        return specialTokens.get(chr);
    }

    private static List<GlslToken> parseToken(StringReader reader) {
        reader.skipWS();

        if (reader.isEmpty()) {
            return Collections.singletonList(TokenType.EOS.singletonToken);
        }

        TokenType type = checker.find(reader);
        if (type != null) {
            reader.skip(type.text.length());
            reader.skipWS();
            if (type == TokenType.VERSION_DIRECTIVE || type == TokenType.EXTENSION_DIRECTIVE) {
                char c = reader.charAt(0);
                StringBuilder builder = new StringBuilder();
                while (c != '\n') {
                    reader.advance();
                    builder.append(c);
                    c = reader.charAt(0);
                }
                return Arrays.asList(
                        type.singletonToken,
                        new GlslToken(
                                TokenType.LITERAL,
                                builder.toString()
                        )
                );
            }
            return Collections.singletonList(type.singletonToken);
        }

        char c = reader.charAt(0);
        GlslToken special = getSpecial(c);
        if (special != null) {
            reader.advance();
            return Collections.singletonList(special);
        }

        int start = reader.index();
        while (!Character.isWhitespace(c)) {
            reader.advance();
            c = reader.peek();
            special = getSpecial(c);
            if (special != null) {
                break;
            }
        }

        return Collections.singletonList(new GlslToken(
                TokenType.LITERAL,
                reader.absSubstring(start, reader.index())
        ));
    }

    static List<GlslToken> nextToken(StringReader reader) {
        popComments(reader);
        return parseToken(reader);
    }
}
