import tfc.glsl.GlslFile;
import tfc.glsl.GlslParser;

public class ParserTest1 {
    public static void main(String[] args) {
        GlslParser parser = new GlslParser();
        GlslFile file = parser.parse("""
                #version 330 core
                                
                layout(std140, binding = 0) uniform TransformBlock {
                    mat4 model;
                    mat4 view;
                    mat4 projection;
                    vec3 cameraPos;
                    float time;
                };
                                
                layout(std140, binding = 1) uniform LightBlock {
                    vec4 lightPositions[8];
                    vec3 lightColors[8];
                    int lightCount;
                };
                                
                uniform sampler2D diffuseMap[4];
                uniform samplerCube environmentMap;
                uniform vec3 materialParams[3];
                                
                in vec3 fragPos;
                in vec2 texCoord;
                in vec3 normal;
                out vec4 fragColor;
                                
                vec3 calculateLighting(vec3 normal, vec3 viewDir, vec3 diffuseColor)
                {
                    vec3 result = vec3(0.0);
                    for (int i = 0; i < lightCount; ++i) {
                        vec3 lightDir = normalize(lightPositions[i].xyz - fragPos);
                        float diff = max(dot(normal, lightDir), 0.0);
                        vec3 reflectDir = reflect(-lightDir, normal);
                        float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
                       \s
                        result += (diff * diffuseColor + spec * vec3(0.5)) * lightColors[i];
                    }
                    return result;
                }
                                
                mat3 computeTBN()
                {
                    return mat3(1.0);
                }
                                
                vec3 globalVar = vec3(0.0);
                int counter = 0;
                                
                void main()
                {
                    mat3 rotMatrix = mat3(1.0);
                    vec4 positions[10];
                    vec3 complexIndex = vec3(1.0, 2.0, 3.0);
                                
                    rotMatrix[int(complexIndex.z)][0] = materialParams[0].x;
                    positions[4].xyz = lightPositions[2].xyz;
                    fragColor.rgb[1] = materialParams[2].g;
                                
                    for (int i = 0; i < lightCount; i++) {
                        while (counter < 10) {
                            do {
                                counter++;
                                if (counter > 5) break;
                            } while (counter < 8);
                           \s
                            if (distance(fragPos, lightPositions[i].xyz) > 100.0) {
                                continue;
                            } else if (i % 2 == 0) {
                                globalVar += vec3(0.1);
                            }
                        }
                    }
                                
                    mat4 mvp = projection * view * model;
                    vec3 transformedPos = (mvp * vec4(fragPos, 1.0)).xyz;
                    mat3 normalMatrix = transpose(inverse(mat3(model)));
                                
                    vec2 distortedCoords = texCoord + vec2(sin(time * 0.1), cos(time * 0.1));
                    vec4 diffuseColor = texture(diffuseMap[2], distortedCoords * 2.0 - 1.0);
                                
                    vec3 lightValue = lightColors[int(mod(time, 8))];
                    float param = materialParams[int(floor(texCoord.x * 3))].z;
                                
                    vec3 viewDir = normalize(cameraPos - fragPos);
                    vec3 reflected = reflect(-viewDir, normal);
                    vec3 refracted = refract(-viewDir, normal, 1.0/1.33);
                                
                    vec3 result = mix(diffuseColor.rgb, texture(environmentMap, reflected).rgb, 0.5);
                    float luminance = dot(result, vec3(0.2126, 0.7152, 0.0722));
                    vec3 saturated = pow(result, vec3(2.2));
                                
                    fragColor.rgb = calculateLighting(
                        normalize(normalMatrix * normal),
                        viewDir,
                        diffuseColor.rgb
                    );
                                
                    float a = 1.0;
                    float b = 2.0;
                    float c = 3.0;
                    a = b = c = time * 0.1;
                                
                    counter++;
                    --counter;
                    a += b -= c *= 2.0;
                                
                    int flags = 0x0F;
                    flags |= 0x10;
                    flags &= ~0x08;
                    flags ^= 0x02;
                                
                    float dist = distance(fragPos, cameraPos);
                    vec3 crossProd = cross(normal, viewDir);
                    float face = dot(normal, viewDir) > 0.0 ? 1.0 : -1.0;
                                
                    for (float f = 0.0; f < 1.0; f += 0.1) {
                        result = mix(result, vec3(1.0), f);
                    }
                                
                    for (int j = 10; j > 0; j--) {
                        result *= 1.01;
                    }
                                
                    if (luminance > 0.5) {
                        for (int k = 0; k < 4; k++) {
                            while (counter < 100) {
                                if (k % 2 == 0) {
                                    break;
                                } else {
                                    counter += k;
                                }
                            }
                        }
                    }
                                
                    fragColor.a = diffuseColor.a;
                    fragColor.rgb = result;
                }
                """);

        System.out.println(
                file.asString()
        );
    }
}
