import tfc.glsl.GlslParser;

public class ParserTest {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        parser.parse("""
                #version 330 core
                
                layout (std140, binding = 0) uniform float x;
                
                void main() {
                    gl_ClipDistance[0] = (pos.yzx.xzy[0].xxx);
                    gl_Position = q *= vec4(x, x, x, x);
                    vec4(3);
                }
                """);
    }
}
