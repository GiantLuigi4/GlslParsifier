import tfc.glsl.GlslParser;

public class ParserTest {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        parser.parse("""
                #version 330 core
                
                layout (std140, binding = 0) uniform float x;
                
                void main() {
                    gl_Position = vec4(x);
                }
                """);
    }
}
