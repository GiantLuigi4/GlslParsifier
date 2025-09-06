package tfc.glsl;

import tfc.glsl.parse.GlslTreeifier;
import tfc.glsl.util.StringReader;
import tfc.glsl.parse.TokenStreamer;

public class GlslParser {
    public GlslParser() {
    }

    public GlslFile parse(String text) {
        return parse(new StringReader(text));
    }

    public GlslFile parse(StringReader text) {
        text.skipWS();
        TokenStreamer streamer = new TokenStreamer(text);
        return GlslTreeifier.toTree(streamer);
    }
}
