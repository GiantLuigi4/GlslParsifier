import tfc.glsl.GlslFile;
import tfc.glsl.GlslParser;

public class ParserTest3 {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        GlslFile file = parser.parse("""
                #version 330 core

                layout(r8ui) uniform writeonly uimage3D voxel_img;
                vec3[] specialTintColor = vec3[](vec3(1.0), vec3(1.0, 0.5, 0.2), vec3(1.0, 0.1, 1.0), vec3(0.5, 0.65, 1.0), vec3(1.0, 1.0, 0.1), vec3(0.1, 1.0, 0.1), vec3(1.0, 0.3, 1.0), vec3(1.0), vec3(1.0), vec3(0.3, 0.8, 1.0), vec3(0.7, 0.3, 1.0), vec3(0.1, 0.15, 1.0), vec3(1.0, 0.75, 0.5), vec3(0.3, 1.0, 0.3), vec3(1.0, 0.1, 0.1), vec3(1.0), vec3(0.5, 0.65, 1.0), vec3(1.0), vec3(1.0), vec3(0.0));

                void main() {
                    int a = 0;
                    if (a == 0 || a == 1 && a == 2) {
                    }
                }
                """);

//        GlslFile file = parser.parse("""
//                #version 330 core
//
//                layout (std140, binding = 0) uniform float x;
//
//                void main() {
//                    ((x[2])[0] = q) = (a[2] - q[0] / vec3(2, 4, 3).zxy.xzy * 3);
//                }
//                """);
//        GlslFile file = parser.parse("""
//                #version 330 core
//
//                layout (std140, binding = 0) uniform float x;
//
//                void main() {
//                    int q = a.xyz * 2;
//                }
//                """);

        System.out.println(
                file.asString()
        );
    }
}
