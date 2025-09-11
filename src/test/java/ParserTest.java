import tfc.glsl.GlslFile;
import tfc.glsl.GlslParser;
import tfc.glsl.parse.ExpressionParser;
import tfc.glsl.parse.TokenStreamer;

public class ParserTest {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
//        GlslFile file = parser.parse("""
//                #version 330 core
//
//                layout (std140, binding = 0) uniform float x;
//
//                void main() {
//                    gl_ClipDistance[0] = (pos.yzx.xzy[0].xxx);
//                    gl_Position = q *= vec4(x, x, x, x);
//                    vec4(3);
//
//                    vec4 x = vec4(3);;;;
//
//                    ((x[2])[0] = q)[0] = 2;
//                    int i = 1 - 2 + 4 * 3 + 2;
//                }
//                """);
//        GlslFile file = parser.parse("""
//                #version 330 core
//
//                layout (std140, binding = 0) uniform float x;
//
//                void main() {
//                   ((((obj.arr[++i % 5].xyz.yx[foo(bar(42 ? q-- : ++z), a[b ? c[d] : e])) = (x = y + (z *= 3.14))) ? ((mat3(1)[0].yzx.x + vec4(1).w) * --counter) : (alpha ? beta : gamma)) /= (delta[delta[0]++]++ * (func()(a++, --b) ? 1 : 0))) = theta;
//                }
//                """);
        GlslFile file = parser.parse("""
                #version 330 core

                layout (std140, binding = 0) uniform float x;

                void main() {
                    x[mat3(1)[int(a.z)].xy[int(b.x + 1)]].rgb.xy[func(vec3(1,2,3).zxy.x)] = y = z = dot(normalize(p + q * 0.5), r);
                    x[a].rgb = y = 3;
                    x[a[1 + (b * 2)] % n].rgb[func(vec3(1, 3, 2).zxy.z + m[2])] = y = 3;
                    
                    q = x == b;
                    
                    for (int i = 0; i < 10; i+=1) {
                        x = v;
                        v = x;
                        
                        int a = i;
                        while (a > 0) {
                            a -= 1;
                        }
                        
                        int q = 0;
                        do {
                            q += 1;
                        } while (q < 10);
                        
                        q = q++;
                        i++;
                        ++i;
                        q = ++q;
                        q = -(++q);
                        
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
