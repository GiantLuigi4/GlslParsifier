import tfc.glsl.GlslFile;
import tfc.glsl.GlslParser;

public class ParserTest {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        GlslFile file = parser.parse("""
                #version 330 core
                
                layout (std140, binding = 0) uniform float x;
                
                void main() {
                    gl_ClipDistance[0] = (pos.yzx.xzy[0].xxx);
                    gl_Position = q *= vec4(x, x, x, x);
                    vec4(3);
                    
                    vec4 x = vec4(3);;;;
                    
                }
                """);

        System.out.println(
                file.asString()
        );
    }
}
