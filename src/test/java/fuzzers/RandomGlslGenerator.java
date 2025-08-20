package fuzzers;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.*;
import tfc.glsl.statements.*;
import tfc.glsl.value.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGlslGenerator {
    private final Random random = new Random();
    private final GlslFile file;
    private final List<String> availableVariables = new ArrayList<>();

    public RandomGlslGenerator(String version) {
        file = new GlslFile(version);
    }

    public void generate() {
        generateGlobals();
        generateMainFunction();
    }

    private void generateGlobals() {
        // Generate random uniforms
        int uniformCount = random.nextInt(5) + 1;
        for (int i = 0; i < uniformCount; i++) {
            String type = randomType();
            String name = "u_" + randomIdentifier();
            GlslMemberSegment uniform = new GlslMemberSegment(
                    StorageQualifier.UNIFORM,
                    new VarSpecifier(type, name)
            );
            file.addSegment(uniform);
            availableVariables.add(name);
        }

        // Generate random global variables
        int globalCount = random.nextInt(3);
        for (int i = 0; i < globalCount; i++) {
            String type = randomType();
            String name = "g_" + randomIdentifier();
            GlslVarSegment global = new GlslVarSegment(new VarSpecifier(type, name));
            file.addSegment(global);
            availableVariables.add(name);
        }
    }

    private void generateMainFunction() {
        GlslCodeSegment main = new GlslCodeSegment("void", "main");

        // Generate random statements
        int statementCount = random.nextInt(10) + 5;
        for (int i = 0; i < statementCount; i++) {
            main.addStatement(generateRandomStatement());
        }

        // Ensure we set gl_Position
        if (random.nextBoolean()) {
            main.addStatement(new AssignmentStatement(
                    new TokenValue("gl_Position"),
                    generateRandomValue("vec4")
            ));
        }

        file.addSegment(main);
    }

    private GlslStatement generateRandomStatement() {
        int choice = random.nextInt(6);
        switch (choice) {
            case 0:
                return generateVarDeclaration();
            case 1:
                return generateAssignment();
            case 2:
                return generateIfStatement();
            case 3:
                return generateForLoop();
            case 4:
                return generateWhileLoop();
            case 5:
                return generateMethodCall();
            default:
                return generateVarDeclaration();
        }
    }

    private GlslStatement generateVarDeclaration() {
        String type = randomType();
        String name = "v_" + randomIdentifier();
        VarDefStatement stmt = new VarDefStatement(new VarSpecifier(type, name));

        if (random.nextBoolean()) {
            stmt.setValue(generateRandomValue(type));
        }

        availableVariables.add(name);
        return stmt;
    }

    private GlslStatement generateAssignment() {
        if (availableVariables.isEmpty()) return generateVarDeclaration();

        String target = availableVariables.get(
                random.nextInt(availableVariables.size())
        );
        return new AssignmentStatement(
                new TokenValue(target),
                generateRandomValue(randomType())
        );
    }

    private GlslStatement generateIfStatement() {
        ConditionalStatement ifStmt = new ConditionalStatement();

        // Main condition
        ifStmt.addStep(new ConditionalStatement.ConditionalCode(
                generateRandomCondition()
        ));

        // Optional else if
        if (random.nextBoolean()) {
            ifStmt.addStep(new ConditionalStatement.ConditionalCode(
                    generateRandomCondition()
            ));
        }

        // Optional else
        if (random.nextBoolean()) {
            ifStmt.addStep(new ConditionalStatement.ConditionalCode(null));
        }

        return ifStmt;
    }

    private GlslStatement generateForLoop() {
        return new ForStatement(
                generateVarDeclaration(),
                generateRandomCondition(),
                new IncStatement(
                        new TokenValue(availableVariables.get(
                                random.nextInt(availableVariables.size())
                        )),
                        random.nextBoolean() ? "++" : "--"
                )
        );
    }

    private GlslStatement generateWhileLoop() {
        return random.nextBoolean() ?
                new WhileStatement(generateRandomCondition()) :
                new DoWhileStatement(generateRandomCondition());
    }

    private GlslStatement generateMethodCall() {
        return new MethodCallStatement(new MethodCallValue(
                new TokenValue("texture"),
                generateRandomValue("sampler2D"),
                generateRandomValue("vec2")
        ));
    }

    private GlslValue generateRandomValue(String type) {
        if (random.nextBoolean() && !availableVariables.isEmpty()) {
            return new TokenValue(availableVariables.get(
                    random.nextInt(availableVariables.size())
            ));
        }

        switch (type) {
            case "float":
                return new ConstantValue(random.nextFloat());
            case "int":
                return new ConstantValue(random.nextInt(100));
            case "vec2":
                return new MethodCallValue(
                        new TokenValue("vec2"),
                        generateRandomValue("float"),
                        generateRandomValue("float")
                );
            case "vec3":
                return new MethodCallValue(
                        new TokenValue("vec3"),
                        generateRandomValue("float"),
                        generateRandomValue("float"),
                        generateRandomValue("float")
                );
            case "vec4":
                return new MethodCallValue(
                        new TokenValue("vec4"),
                        generateRandomValue("float"),
                        generateRandomValue("float"),
                        generateRandomValue("float"),
                        generateRandomValue("float")
                );
            default:
                return new ConstantValue(0.5f);
        }
    }

    private GlslValue generateRandomCondition() {
        return new OperationValue(
                generateRandomValue("float"),
                randomComparison(),
                generateRandomValue("float")
        );
    }

    private String randomComparison() {
        String[] ops = {"<", ">", "<=", ">=", "==", "!="};
        return ops[random.nextInt(ops.length)];
    }

    private String randomType() {
        String[] types = {"float", "int", "vec2", "vec3", "vec4", "mat4"};
        return types[random.nextInt(types.length)];
    }

    private String randomIdentifier() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void saveToFile(String filename) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(file.toString().getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RandomGlslGenerator generator = new RandomGlslGenerator("330 core");
        generator.generate();
        generator.saveToFile("random.glsl");
    }
}