import tfc.glsl.base.GlslValue;
import tfc.glsl.parse.ExpressionParser;
import tfc.glsl.parse.GlslTreeifier;
import tfc.glsl.parse.TokenStreamer;
import tfc.glsl.util.StringReader;

public class ExprTest {
    public static void main(String[] args) {
        StringReader reader = new StringReader(
                "4+3*2"
        );
        TokenStreamer streamer = new TokenStreamer(reader);

        GlslValue value = ExpressionParser.doParse(
                streamer, () -> GlslTreeifier.nextValueNoExpr(streamer)
        );
        System.out.println(value.asString());
    }
}
