package fuzzers;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.meta.LayoutQualifier;
import tfc.glsl.meta.Member;
import tfc.glsl.meta.Parameter;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.*;
import tfc.glsl.statements.*;
import tfc.glsl.value.*;

import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EnhancedRandomGlslGenerator {
    private final Random random = new Random();
    private final GlslFile file;
    private final Map<String, VariableInfo> availableVariables = new HashMap<>();
    private final List<String> functionNames = new ArrayList<>();
    private int nestingDepth = 0;
    private final int maxNestingDepth = 4;

    // Track variable information
    private static class VariableInfo {
        String type;
        String name;
        boolean isUniform;

        public VariableInfo(String type, String name, boolean isUniform) {
            this.type = type;
            this.name = name;
            this.isUniform = isUniform;
        }
    }

    public EnhancedRandomGlslGenerator(String version) {
        file = new GlslFile(version);
        functionNames.add("main"); // Always include main function
    }

    public void generate() {
        generateGlobals();
        generateUniformBlocks();
        generateFunctions();
    }

    private void generateGlobals() {
        // Generate random uniforms
        int uniformCount = random.nextInt(5) + 3;
        for (int i = 0; i < uniformCount; i++) {
            String type = randomType();
            String name = "u_" + randomIdentifier();
            GlslMemberSegment uniform = new GlslMemberSegment(
                    StorageQualifier.UNIFORM,
                    new VarSpecifier(type, name)
            );

            // Add layout qualifier sometimes
            if (random.nextBoolean()) {
                LayoutQualifier layout = new LayoutQualifier();
                layout.addSegment("binding=" + random.nextInt(8));
                uniform.setLayout(layout);
            }

            file.addSegment(uniform);
            availableVariables.put(name, new VariableInfo(type, name, true));
        }

        // Generate random global variables
        int globalCount = random.nextInt(4);
        for (int i = 0; i < globalCount; i++) {
            String type = randomType();
            String name = "g_" + randomIdentifier();
            GlslVarSegment global = new GlslVarSegment(new VarSpecifier(type, name));
            file.addSegment(global);
            availableVariables.put(name, new VariableInfo(type, name, false));
        }
    }

    private void generateUniformBlocks() {
        if (random.nextBoolean()) {
            // Create a uniform block
            GlslBlockSegment uniformBlock = new GlslBlockSegment(StorageQualifier.UNIFORM);
            uniformBlock.setName("Uniforms_" + randomIdentifier());
            uniformBlock.setInstance("uniforms");

            // Add layout qualifier
            LayoutQualifier layout = new LayoutQualifier();
            layout.addSegment("binding=" + random.nextInt(8));
            layout.addSegment("std140");
            uniformBlock.setLayout(layout);

            // Add members to the block
            int memberCount = random.nextInt(5) + 2;
            for (int i = 0; i < memberCount; i++) {
                String type = randomType();
                String name = "ub_" + randomIdentifier();
                Member member = new Member(new VarSpecifier(type, name));
                uniformBlock.addMember(member);
                availableVariables.put("uniforms." + name, new VariableInfo(type, "uniforms." + name, true));
            }

            file.addSegment(uniformBlock);
        }
    }

    private void generateFunctions() {
        // Generate main function
        generateMainFunction();

        // Generate additional functions
        int functionCount = random.nextInt(3) + 1;
        for (int i = 0; i < functionCount; i++) {
            generateCustomFunction();
        }
    }

    private void generateMainFunction() {
        GlslCodeSegment main = new GlslCodeSegment("void", "main");

        // Generate random statements
        int statementCount = random.nextInt(8) + 5;
        for (int i = 0; i < statementCount; i++) {
            main.addStatement(generateRandomStatement(0));
        }

        // Ensure we set gl_Position
        main.addStatement(new AssignmentStatement(
                new TokenValue("gl_Position"),
                generateRandomValue("vec4", 0)
        ));

        file.addSegment(main);
    }

    private void generateCustomFunction() {
        String returnType = random.nextBoolean() ? "void" : randomType();
        String funcName = "func_" + randomIdentifier();
        functionNames.add(funcName);

        // Create function parameters
        List<VarSpecifier> params = new ArrayList<>();
        int paramCount = random.nextInt(4);
        for (int i = 0; i < paramCount; i++) {
            String type = randomType();
            String name = "p_" + randomIdentifier();
            params.add(new VarSpecifier(type, name));
            availableVariables.put(name, new VariableInfo(type, name, false));
        }

        GlslCodeSegment function = new GlslCodeSegment(returnType, funcName);
        for (VarSpecifier varSpecifier : params.toArray(new VarSpecifier[0])) {
            function.addParam(new Parameter(varSpecifier));
        }

        // Generate function body
        int statementCount = random.nextInt(10) + 3;
        for (int i = 0; i < statementCount; i++) {
            function.addStatement(generateRandomStatement(0));
        }

        // Add return statement if not void
        if (!"void".equals(returnType)) {
            function.addStatement(new ReturnStatement(generateRandomValue(returnType, 0)));
        }

        file.addSegment(function);

        // Remove parameters from available variables as they're local to this function
        for (VarSpecifier param : params) {
            availableVariables.remove(param.getName());
        }
    }

    private GlslStatement generateRandomStatement(int depth) {
        if (depth > maxNestingDepth) {
            // Avoid too deep nesting, return a simple statement
            return generateSimpleStatement();
        }

        int choice = random.nextInt(8);
        switch (choice) {
            case 0:
                return generateVarDeclaration();
            case 1:
                return generateAssignment();
            case 2:
                return generateIfStatement(depth);
            case 3:
                return generateForLoop(depth);
            case 4:
                return generateWhileLoop(depth);
            case 5:
                return generateDoWhileLoop(depth);
            case 6:
                return generateMethodCall();
            case 7:
                return generateReturnStatement();
            default:
                return generateVarDeclaration();
        }
    }

    private GlslStatement generateSimpleStatement() {
        int choice = random.nextInt(3);
        switch (choice) {
            case 0:
                return generateVarDeclaration();
            case 1:
                return generateAssignment();
            case 2:
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
            stmt.setValue(generateRandomValue(type, 0));
        }

        availableVariables.put(name, new VariableInfo(type, name, false));
        return stmt;
    }

    private GlslStatement generateAssignment() {
        if (availableVariables.isEmpty()) return generateVarDeclaration();

        List<String> nonUniformVars = availableVariables.entrySet().stream()
                .filter(entry -> !entry.getValue().isUniform)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (nonUniformVars.isEmpty()) return generateVarDeclaration();

        String target = nonUniformVars.get(random.nextInt(nonUniformVars.size()));
        VariableInfo varInfo = availableVariables.get(target);
        return new AssignmentStatement(
                new TokenValue(target),
                generateRandomValue(varInfo.type, 0)
        );
    }

    private GlslStatement generateIfStatement(int depth) {
        ConditionalStatement ifStmt = new ConditionalStatement();

        // Main condition
        ifStmt.addStep(new ConditionalStatement.ConditionalCode(
                generateRandomCondition(0)
        ));

        // Add statements to the if block
        int ifStatementCount = random.nextInt(3) + 1;
        for (int i = 0; i < ifStatementCount; i++) {
            ConditionalStatement.ConditionalCode code = ifStmt.getLastConditional();
            code.addStatement(generateRandomStatement(depth + 1));
        }

        // Optional else if
        if (random.nextBoolean()) {
            ifStmt.addStep(new ConditionalStatement.ConditionalCode(
                    generateRandomCondition(0)
            ));

            // Add statements to the else if block
            int elseIfStatementCount = random.nextInt(3) + 1;
            for (int i = 0; i < elseIfStatementCount; i++) {
                ConditionalStatement.ConditionalCode code = ifStmt.getLastConditional();
                code.addStatement(generateRandomStatement(depth + 1));
            }
        }

        // Optional else
        if (random.nextBoolean()) {
            ifStmt.addStep(new ConditionalStatement.ConditionalCode(null));

            // Add statements to the else block
            int elseStatementCount = random.nextInt(3) + 1;
            for (int i = 0; i < elseStatementCount; i++) {
                ConditionalStatement.ConditionalCode code = ifStmt.getLastConditional();
                code.addStatement(generateRandomStatement(depth + 1));
            }
        }

        return ifStmt;
    }

    private GlslStatement generateForLoop(int depth) {
        // Create a loop variable
        String loopVarName = "i_" + randomIdentifier();
        VarDefStatement init = new VarDefStatement(new VarSpecifier("int", loopVarName))
                .setValue(new ConstantValue(0));

        availableVariables.put(loopVarName, new VariableInfo("int", loopVarName, false));

        ForStatement forLoop = new ForStatement(
                init,
                new OperationValue(
                        new TokenValue(loopVarName),
                        "<",
                        new ConstantValue(random.nextInt(10) + 5)
                ),
                new IncStatement(new TokenValue(loopVarName), "++")
        );

        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            forLoop.addStatement(generateRandomStatement(depth + 1));
        }

        return forLoop;
    }

    private GlslStatement generateWhileLoop(int depth) {
        WhileStatement whileLoop = new WhileStatement(generateRandomCondition(0));

        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            whileLoop.addStatement(generateRandomStatement(depth + 1));
        }

        return whileLoop;
    }

    private GlslStatement generateDoWhileLoop(int depth) {
        DoWhileStatement doWhileLoop = new DoWhileStatement(generateRandomCondition(0));

        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            doWhileLoop.addStatement(generateRandomStatement(depth + 1));
        }

        return doWhileLoop;
    }

    private GlslStatement generateMethodCall() {
        if (functionNames.size() > 1 && random.nextBoolean()) {
            // Call a custom function (not main)
            String funcName = functionNames.get(random.nextInt(functionNames.size() - 1) + 1);
            List<GlslValue> params = new ArrayList<>();

            // For simplicity, pass 0-3 random values
            int paramCount = random.nextInt(4);
            for (int i = 0; i < paramCount; i++) {
                params.add(generateRandomValue(randomType(), 0));
            }

            return new MethodCallStatement(new MethodCallValue(
                    new TokenValue(funcName),
                    params.toArray(new GlslValue[0])
            ));
        } else {
            // Call a built-in function
            String[] builtIns = {"texture", "sin", "cos", "pow", "max", "min", "length", "normalize"};
            String funcName = builtIns[random.nextInt(builtIns.length)];

            List<GlslValue> params = new ArrayList<>();
            int paramCount = funcName.equals("texture") ? 2 : random.nextInt(3) + 1;
            for (int i = 0; i < paramCount; i++) {
                // For texture, first param should be a sampler, second a vec2
                if (funcName.equals("texture") && i == 0) {
                    params.add(generateRandomValue("sampler2D", 0));
                } else if (funcName.equals("texture") && i == 1) {
                    params.add(generateRandomValue("vec2", 0));
                } else {
                    params.add(generateRandomValue(randomType(), 0));
                }
            }

            return new MethodCallStatement(new MethodCallValue(
                    new TokenValue(funcName),
                    params.toArray(new GlslValue[0])
            ));
        }
    }

    private GlslStatement generateReturnStatement() {
        return new ReturnStatement(generateRandomValue(randomType(), 0));
    }

    private GlslValue generateRandomValue(String type, int depth) {
        // 30% chance to use a variable if available
        if (random.nextInt(100) < 30 && !availableVariables.isEmpty()) {
            // Filter variables by type if possible
            List<VariableInfo> matchingVars = availableVariables.values().stream()
                    .filter(v -> v.type.equals(type))
                    .collect(Collectors.toList());

            if (!matchingVars.isEmpty()) {
                VariableInfo var = matchingVars.get(random.nextInt(matchingVars.size()));
                return new TokenValue(var.name);
            }

            // If no exact type match, use any variable
            List<VariableInfo> allVars = new ArrayList<>(availableVariables.values());
            VariableInfo var = allVars.get(random.nextInt(allVars.size()));
            return new TokenValue(var.name);
        }

        // Otherwise generate a literal or constructor
        switch (type) {
            case "float":
                return new ConstantValue(random.nextFloat() * 10);
            case "int":
                return new ConstantValue(random.nextInt(100));
            case "bool":
                return new BooleanValue(random.nextBoolean());
            case "vec2":
                return new MethodCallValue(
                        new TokenValue("vec2"),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth)
                );
            case "vec3":
                return new MethodCallValue(
                        new TokenValue("vec3"),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth)
                );
            case "vec4":
                return new MethodCallValue(
                        new TokenValue("vec4"),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth),
                        generateRandomValue("float", depth)
                );
            case "mat4":
                return new MethodCallValue(
                        new TokenValue("mat4"),
                        generateRandomValue("float", depth)
                );
            case "sampler2D":
                // For sampler2D, we just return a reference to a texture uniform
                List<VariableInfo> samplerVars = availableVariables.values().stream()
                        .filter(v -> v.type.equals("sampler2D") && v.isUniform)
                        .collect(Collectors.toList());

                if (!samplerVars.isEmpty()) {
                    return new TokenValue(samplerVars.get(random.nextInt(samplerVars.size())).name);
                } else {
                    return new TokenValue("u_texture0");
                }
            default:
                return new ConstantValue(0.5f);
        }
    }

    private GlslValue generateRandomCondition(int depth) {
        String[] comparisonOps = {"<", ">", "<=", ">=", "==", "!="};
        String op = comparisonOps[random.nextInt(comparisonOps.length)];

        return new OperationValue(
                generateRandomValue("float", depth),
                op,
                generateRandomValue("float", depth)
        );
    }

    private String randomType() {
        String[] types = {"float", "int", "bool", "vec2", "vec3", "vec4", "mat4", "sampler2D"};
        return types[random.nextInt(types.length)];
    }

    private String randomIdentifier() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void saveToFile(String filename) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(file.toString().getBytes());
            out.close();
            System.out.println("Generated GLSL saved to: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EnhancedRandomGlslGenerator generator = new EnhancedRandomGlslGenerator("330 core");
        generator.generate();
        generator.saveToFile("enhanced_random.glsl");
    }
}