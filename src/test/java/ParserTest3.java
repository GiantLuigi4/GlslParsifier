import tfc.glsl.GlslFile;
import tfc.glsl.GlslParser;

public class ParserTest3 {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        GlslFile file = parser.parse("""
                #version 330 core

                /* comment */
                /*
                    multiline
                    comment
                */
                #extension GL_ARB_explicit_uniform_location : require

                layout(r8ui) uniform writeonly uimage3D voxel_img;
                float c = cos(a), s = sin(a);

                void main() {
                    float c = cos(a), s = sin(a);
                    {
                        int a = 0;
                        if (a == 0 || a == 1 && a == 2) {
                        }
                        float f = 0.3f;
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
