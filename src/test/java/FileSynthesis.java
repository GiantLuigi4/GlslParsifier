import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.meta.LayoutQualifier;
import tfc.glsl.meta.Member;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.segments.*;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.statements.*;
import tfc.glsl.value.*;

import java.io.FileOutputStream;

public class FileSynthesis {
    public static void main(String[] args) {
        GlslFile file = new GlslFile("330 core");

//        {
//            // options: UNIFORM, STRUCT, BUFFER, IN, OUT, VARYING
//            GlslBlockSegment varsIn = new GlslBlockSegment(StorageQualifier.IN);
//            varsIn.setName("Input");
//            varsIn.setInstance("inputVars");
//            // can have layout qualifier
////            varsIn.setLayout(new LayoutQualifier().addSegment("binding=0"));
//
//            // var specifier can have .setArrayData(String)
//            // i.e. setArrayData("[10]")
//            Member member = new Member(new VarSpecifier("vec4", "Position"));
//            // members can have layout qualifier
//            varsIn.addMember(member);
//            member = new Member(new VarSpecifier("vec4", "Color"));
//            varsIn.addMember(member);
//            member = new Member(new VarSpecifier("vec2", "UV0"));
//            varsIn.addMember(member);
//            member = new Member(new VarSpecifier("vec2", "UV1"));
//            varsIn.addMember(member);
//            member = new Member(new VarSpecifier("ivec2", "UV2"));
//            varsIn.addMember(member);
//
//            file.addSegment(varsIn);
//        }

        {
            GlslMemberSegment memberDef = new GlslMemberSegment(StorageQualifier.UNIFORM, new VarSpecifier("vec4", "true_Position"));
            file.addSegment(memberDef);
            memberDef = new GlslMemberSegment(StorageQualifier.UNIFORM, new VarSpecifier("vec2", "true_UV0"));
            file.addSegment(memberDef);
            memberDef = new GlslMemberSegment(StorageQualifier.UNIFORM, new VarSpecifier("vec2", "true_UV1"));
            file.addSegment(memberDef);
            memberDef = new GlslMemberSegment(StorageQualifier.UNIFORM, new VarSpecifier("ivec2", "true_UV2"));
            file.addSegment(memberDef);
        }
        {
            GlslVarSegment varDef = new GlslVarSegment(new VarSpecifier("vec4", "override_Position"));
            file.addSegment(varDef);
        }
        {
            GlslCodeSegment segment = new GlslCodeSegment(
                    "void", "main"
            );
            segment.addStatement(new VarDefStatement(
                    new VarSpecifier("vec4", "pos")
            ).setValue(new OperationValue(
                    new TokenValue("true_ProjectionMatrix"),
                    "*",
                    new OperationValue(
                            new TokenValue("true_ModelViewMatrix"),
                            "*",
                            new TokenValue("override_Inject_Position")
                    )
            )));

            segment.addStatement(nestedFor(0));

            segment.addStatement(new VarDefStatement(new VarSpecifier("int", "i")).setValue(new ConstantValue(1)));
            {
                WhileStatement whileSt = new WhileStatement(new OperationValue(new TokenValue("i"), "<", new ConstantValue(5)));
                whileSt.addStatement(new IncStatement(new TokenValue("i"), "++"));
                segment.addStatement(whileSt);
            }

            segment.addStatement(new VarDefStatement(new VarSpecifier("int", "i")).setValue(new ConstantValue(1)));
            {
                DoWhileStatement whileSt = new DoWhileStatement(new OperationValue(new TokenValue("i"), "<", new ConstantValue(5)));
                whileSt.addStatement(new IncStatement(new TokenValue("i"), "++"));
                segment.addStatement(whileSt);
            }

            {
                ConditionalStatement conditionalStatement = new ConditionalStatement();
                ConditionalStatement.ConditionalCode conditionalCode = new ConditionalStatement.ConditionalCode(
                        new OperationValue(new TokenValue("q"), "<", new TokenValue("e"))
                );
                conditionalCode.addStatement(new IncStatement(new TokenValue("i"), "++"));
                conditionalStatement.addStep(conditionalCode);
                conditionalStatement.addStep(conditionalCode);

                conditionalCode = new ConditionalStatement.ConditionalCode(null);
                conditionalCode.addStatement(new IncStatement(new TokenValue("i"), "++"));
                conditionalStatement.addStep(conditionalCode);

                segment.addStatement(conditionalStatement);
            }

            segment.addStatement(new MethodCallStatement(new MethodCallValue(
                    new TokenValue("texture2d"),
                    new TokenValue("texture0"),
                    new TokenValue("uv0")
            )));

//            new ConstantValue(0.5); // float
//            new ConstantValue(12); // int
//            new UnaryOperation("~", new ConstantValue(12)); // ~12
            segment.addStatement(new AssignmentStatement(new TokenValue("gl_Position"), new TokenValue("pos")));
            file.addSegment(segment);
        }

        try {
            FileOutputStream dmpOut = new FileOutputStream("out.glsl");
            dmpOut.write(file.toString().getBytes());
            dmpOut.flush();
            dmpOut.close();
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    private static GlslStatement nestedFor(int i) {
        ForStatement statement = new ForStatement(
                new VarDefStatement(new VarSpecifier("int", "index" + i))
                        .setValue(new ConstantValue(1)),
                new OperationValue(
                        new TokenValue("index" + i),
                        "<",
                        new ConstantValue(10)
                ),
                new IncStatement(new TokenValue("index" + i), "++")
        );
        if (i < 3) {
            statement.addStatement(nestedFor(i + 1));
        }
        return statement;
    }
}
