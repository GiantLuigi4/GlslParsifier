import tfc.glsl.parse.TokenStreamer;
import tfc.glsl.util.StringReader;

public class SpecialNumerics {
    public static void main(String[] args) {
        TokenStreamer streamer = new TokenStreamer(
                new StringReader(
                        "1E-3 0xFF 0.5f 0.5 1u"
                )
        );

        streamer.resolve();

        System.out.println(streamer);
    }
}
