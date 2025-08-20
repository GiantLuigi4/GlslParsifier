package fuzzers.optimized.a;

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

public class AdvancedGlslGenerator {
    // Configuration constants
    private static final int MIN_UNIFORMS = 3;
    private static final int MAX_UNIFORMS = 8;
    private static final int MIN_GLOBALS = 0;
    private static final int MAX_GLOBALS = 4;
    private static final int MIN_FUNCTIONS = 2;
    private static final int MAX_FUNCTIONS = 50;
    private static final int MIN_MAIN_STATEMENTS = 5;
    private static final int MAX_MAIN_STATEMENTS = 13;
    private static final int MIN_FUNCTION_STATEMENTS = 3;
    private static final int MAX_FUNCTION_STATEMENTS = 13;
    private static final int MIN_FUNCTION_PARAMS = 0;
    private static final int MAX_FUNCTION_PARAMS = 0;
    private static final int MAX_NESTING_DEPTH = 4;

    private final Random random = new Random();
    private final GlslFile file;
    
    // Optimized data structures
    private final Map<String, VariableInfo> availableVariables = new HashMap<>();
    private final List<String> nonUniformVariables = new ArrayList<>();
    private final Map<String, List<String>> variablesByType = new HashMap<>();
    
    private final List<FunctionInfo> functions = new ArrayList<>();
    private final Map<String, List<FunctionInfo>> functionsByReturnType = new HashMap<>();
    
    private int nestingDepth = 0;
    private final List<String> currentFunctionReturnPoints = new ArrayList<>();
    private boolean inConditionalBlock = false;
    private String currentFunctionName = "";

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
    
    // Track function information
    private static class FunctionInfo {
        String returnType;
        String name;
        List<ParameterInfo> parameters;
        
        public FunctionInfo(String returnType, String name, List<ParameterInfo> parameters) {
            this.returnType = returnType;
            this.name = name;
            this.parameters = parameters;
        }
    }
    
    // Track parameter information
    private static class ParameterInfo {
        String type;
        String name;
        
        public ParameterInfo(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public AdvancedGlslGenerator(String version) {
        file = new GlslFile(version);
        
        // Initialize type maps
        String[] types = {"float", "int", "bool", "vec2", "vec3", "vec4", "mat4", "sampler2D"};
        for (String type : types) {
            variablesByType.put(type, new ArrayList<>());
            functionsByReturnType.put(type, new ArrayList<>());
        }
        functionsByReturnType.put("void", new ArrayList<>());
    }

    public void generate() {
        generateGlobals();
        generateUniformBlocks();
        generateCustomFunctions();
        generateMainFunction();
    }

    private void generateGlobals() {
        // Generate random uniforms
        int uniformCount = random.nextInt(MAX_UNIFORMS - MIN_UNIFORMS + 1) + MIN_UNIFORMS;
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
            addVariable(type, name, true);
        }

        // Generate random global variables
        int globalCount = random.nextInt(MAX_GLOBALS - MIN_GLOBALS + 1) + MIN_GLOBALS;
        for (int i = 0; i < globalCount; i++) {
            String type = randomType();
            String name = "g_" + randomIdentifier();
            GlslVarSegment global = new GlslVarSegment(new VarSpecifier(type, name));
            file.addSegment(global);
            addVariable(type, name, false);
        }
    }

    private void addVariable(String type, String name, boolean isUniform) {
        VariableInfo info = new VariableInfo(type, name, isUniform);
        availableVariables.put(name, info);
        
        if (!isUniform) {
            nonUniformVariables.add(name);
        }
        
        // Add to type-specific list
        List<String> typeVars = variablesByType.get(type);
        if (typeVars != null) {
            typeVars.add(name);
        }
    }
    
    private void removeVariable(String name) {
        VariableInfo info = availableVariables.remove(name);
        if (info != null) {
            nonUniformVariables.remove(name);
            
            // Remove from type-specific list
            List<String> typeVars = variablesByType.get(info.type);
            if (typeVars != null) {
                typeVars.remove(name);
            }
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
                addVariable(type, "uniforms." + name, true);
            }
            
            file.addSegment(uniformBlock);
        }
    }

    private void generateCustomFunctions() {
        // Generate additional functions (not main)
        int functionCount = random.nextInt(MAX_FUNCTIONS - MIN_FUNCTIONS + 1) + MIN_FUNCTIONS;
        for (int i = 0; i < functionCount; i++) {
            generateCustomFunction();
        }
    }

    private void generateMainFunction() {
        currentFunctionName = "main";
        GlslCodeSegment main = new GlslCodeSegment("void", "main");
        currentFunctionReturnPoints.clear();
        
        // Generate random statements
        int statementCount = random.nextInt(MAX_MAIN_STATEMENTS - MIN_MAIN_STATEMENTS + 1) + MIN_MAIN_STATEMENTS;
        for (int i = 0; i < statementCount; i++) {
            if (currentFunctionReturnPoints.contains("main")) break;
            main.addStatement(generateRandomStatement(0));
        }

        // Ensure we set gl_Position if not already returned
        if (!currentFunctionReturnPoints.contains("main")) {
            main.addStatement(new AssignmentStatement(
                new TokenValue("gl_Position"), 
                generateRandomValue("vec4", 0)
            ));
        }

        file.addSegment(main);
        currentFunctionReturnPoints.remove("main");
        currentFunctionName = "";
    }

    private void generateCustomFunction() {
        String returnType = random.nextBoolean() ? "void" : randomType();
        String funcName = "func_" + randomIdentifier();
        currentFunctionName = funcName;
        
        // Create function parameters
        List<ParameterInfo> params = new ArrayList<>();
        int paramCount = random.nextInt(MAX_FUNCTION_PARAMS - MIN_FUNCTION_PARAMS + 1) + MIN_FUNCTION_PARAMS;
        for (int i = 0; i < paramCount; i++) {
            String type = randomType();
            String name = "p_" + randomIdentifier();
            params.add(new ParameterInfo(type, name));
            addVariable(type, name, false);
        }
        
        // Store function info
        FunctionInfo funcInfo = new FunctionInfo(returnType, funcName, params);
        functions.add(funcInfo);
        
        // Add to return type index
        List<FunctionInfo> returnTypeFuncs = functionsByReturnType.get(returnType);
        if (returnTypeFuncs != null) {
            returnTypeFuncs.add(funcInfo);
        }
        
        // Create function segment
        VarSpecifier[] paramSpecifiers = new VarSpecifier[params.size()];
        for (int i = 0; i < params.size(); i++) {
            ParameterInfo param = params.get(i);
            paramSpecifiers[i] = new VarSpecifier(param.type, param.name);
        }
            
        GlslCodeSegment function = new GlslCodeSegment(returnType, funcName);
        for (VarSpecifier paramSpecifier : paramSpecifiers) {
            function.addParam(new Parameter(paramSpecifier));
        }
        currentFunctionReturnPoints.clear();
        
        // Generate function body
        int statementCount = random.nextInt(MAX_FUNCTION_STATEMENTS - MIN_FUNCTION_STATEMENTS + 1) + MIN_FUNCTION_STATEMENTS;
        for (int i = 0; i < statementCount; i++) {
            if (currentFunctionReturnPoints.contains(funcName)) break;
            function.addStatement(generateRandomStatement(0));
        }
        
        // Add return statement if not void and not already returned
        if (!"void".equals(returnType) && !currentFunctionReturnPoints.contains(funcName)) {
            function.addStatement(new ReturnStatement(generateRandomValue(returnType, 0)));
            currentFunctionReturnPoints.add(funcName);
        }
        
        file.addSegment(function);
        
        // Remove parameters from available variables as they're local to this function
        for (ParameterInfo param : params) {
            removeVariable(param.name);
        }
        
        currentFunctionReturnPoints.remove(funcName);
        currentFunctionName = "";
    }

    private GlslStatement generateRandomStatement(int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            return generateSimpleStatement();
        }
        
        int choice = random.nextInt(9);
        switch (choice) {
            case 0: return generateVarDeclaration();
            case 1: return generateAssignment();
            case 2: return generateIfStatement(depth);
            case 3: return generateForLoop(depth);
            case 4: return generateWhileLoop(depth);
            case 5: return generateDoWhileLoop(depth);
            case 6: return generateMethodCall();
            case 7: return generateReturnStatement();
            case 8: return generateExpressionStatement();
            default: return generateVarDeclaration();
        }
    }

    private GlslStatement generateSimpleStatement() {
        int choice = random.nextInt(4);
        switch (choice) {
            case 0: return generateVarDeclaration();
            case 1: return generateAssignment();
            case 2: return generateMethodCall();
            case 3: return generateExpressionStatement();
            default: return generateVarDeclaration();
        }
    }

    private GlslStatement generateVarDeclaration() {
        String type = randomType();
        String name = "v_" + randomIdentifier();
        VarDefStatement stmt = new VarDefStatement(new VarSpecifier(type, name));
        
        if (random.nextBoolean()) {
            stmt.setValue(generateRandomValue(type, 0));
        }
        
        addVariable(type, name, false);
        return stmt;
    }

    private GlslStatement generateAssignment() {
        if (nonUniformVariables.isEmpty()) return generateVarDeclaration();
        
        String target = nonUniformVariables.get(random.nextInt(nonUniformVariables.size()));
        VariableInfo varInfo = availableVariables.get(target);
        return new AssignmentStatement(
            new TokenValue(target),
            generateRandomValue(varInfo.type, 0)
        );
    }

    private GlslStatement generateIfStatement(int depth) {
        boolean wasInConditional = inConditionalBlock;
        inConditionalBlock = true;
        
        ConditionalStatement ifStmt = new ConditionalStatement();
        
        // Main condition
        ConditionalStatement.ConditionalCode ifCondition = new ConditionalStatement.ConditionalCode(
            generateRandomCondition(0)
        );
        ifStmt.addStep(ifCondition);

        // Add statements to the if block
        int ifStatementCount = random.nextInt(3) + 1;
        for (int i = 0; i < ifStatementCount; i++) {
            if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
            ifCondition.addStatement(generateRandomStatement(depth + 1));
        }

        // Optional else if
        if (random.nextBoolean() && !currentFunctionReturnPoints.contains(currentFunctionName)) {
            ConditionalStatement.ConditionalCode elseIfCondition = new ConditionalStatement.ConditionalCode(
                generateRandomCondition(0)
            );
            ifStmt.addStep(elseIfCondition);
            
            // Add statements to the else if block
            int elseIfStatementCount = random.nextInt(3) + 1;
            for (int i = 0; i < elseIfStatementCount; i++) {
                if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
                elseIfCondition.addStatement(generateRandomStatement(depth + 1));
            }
        }

        // Optional else
        if (random.nextBoolean() && !currentFunctionReturnPoints.contains(currentFunctionName)) {
            ConditionalStatement.ConditionalCode elseCondition = new ConditionalStatement.ConditionalCode(null);
            ifStmt.addStep(elseCondition);
            
            // Add statements to the else block
            int elseStatementCount = random.nextInt(3) + 1;
            for (int i = 0; i < elseStatementCount; i++) {
                if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
                elseCondition.addStatement(generateRandomStatement(depth + 1));
            }
        }
        
        inConditionalBlock = wasInConditional;
        return ifStmt;
    }

    private GlslStatement generateForLoop(int depth) {
        // Create a loop variable
        String loopVarName = "i_" + randomIdentifier();
        VarDefStatement init = new VarDefStatement(new VarSpecifier("int", loopVarName))
            .setValue(new ConstantValue(0));
        
        addVariable("int", loopVarName, false);
        
        // Create condition with possible function call
        GlslValue condition;
        if (random.nextBoolean() && !functions.isEmpty()) {
            // Use a function call in the condition (but not the current function)
            List<FunctionInfo> eligibleFunctions = getEligibleFunctions();
                
            if (!eligibleFunctions.isEmpty()) {
                FunctionInfo func = eligibleFunctions.get(random.nextInt(eligibleFunctions.size()));
                if ("bool".equals(func.returnType) || "int".equals(func.returnType)) {
                    condition = new OperationValue(
                        new TokenValue(loopVarName),
                        "<",
                        new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func))
                    );
                } else {
                    condition = new OperationValue(
                        new TokenValue(loopVarName),
                        "<",
                        new ConstantValue(random.nextInt(10) + 5)
                    );
                }
            } else {
                condition = new OperationValue(
                    new TokenValue(loopVarName),
                    "<",
                    new ConstantValue(random.nextInt(10) + 5)
                );
            }
        } else {
            condition = new OperationValue(
                new TokenValue(loopVarName),
                "<",
                new ConstantValue(random.nextInt(10) + 5)
            );
        }
        
        ForStatement forLoop = new ForStatement(
            init,
            condition,
            new IncStatement(new TokenValue(loopVarName), "++")
        );
        
        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
            forLoop.addStatement(generateRandomStatement(depth + 1));
        }
        
        // Remove loop variable from available variables
        removeVariable(loopVarName);
        return forLoop;
    }

    private List<FunctionInfo> getEligibleFunctions() {
        List<FunctionInfo> eligibleFunctions = new ArrayList<>();
        for (FunctionInfo f : functions) {
            if (!f.name.equals(currentFunctionName)) {
                eligibleFunctions.add(f);
            }
        }
        return eligibleFunctions;
    }

    private GlslStatement generateWhileLoop(int depth) {
        GlslValue condition;
        if (random.nextBoolean() && !functions.isEmpty()) {
            // Use a function call in the condition (but not the current function)
            List<FunctionInfo> eligibleFunctions = getEligibleFunctions();
                
            if (!eligibleFunctions.isEmpty()) {
                FunctionInfo func = eligibleFunctions.get(random.nextInt(eligibleFunctions.size()));
                if ("bool".equals(func.returnType)) {
                    condition = new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func));
                } else {
                    condition = generateRandomCondition(0);
                }
            } else {
                condition = generateRandomCondition(0);
            }
        } else {
            condition = generateRandomCondition(0);
        }
        
        WhileStatement whileLoop = new WhileStatement(condition);
        
        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
            whileLoop.addStatement(generateRandomStatement(depth + 1));
        }
        
        return whileLoop;
    }

    private GlslStatement generateDoWhileLoop(int depth) {
        GlslValue condition;
        if (random.nextBoolean() && !functions.isEmpty()) {
            // Use a function call in the condition (but not the current function)
            List<FunctionInfo> eligibleFunctions = getEligibleFunctions();
                
            if (!eligibleFunctions.isEmpty()) {
                FunctionInfo func = eligibleFunctions.get(random.nextInt(eligibleFunctions.size()));
                if ("bool".equals(func.returnType)) {
                    condition = new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func));
                } else {
                    condition = generateRandomCondition(0);
                }
            } else {
                condition = generateRandomCondition(0);
            }
        } else {
            condition = generateRandomCondition(0);
        }
        
        DoWhileStatement doWhileLoop = new DoWhileStatement(condition);
        
        // Add statements to the loop body
        int statementCount = random.nextInt(4) + 1;
        for (int i = 0; i < statementCount; i++) {
            if (currentFunctionReturnPoints.contains(currentFunctionName)) break;
            doWhileLoop.addStatement(generateRandomStatement(depth + 1));
        }
        
        return doWhileLoop;
    }

    private GlslStatement generateMethodCall() {
        if (!functions.isEmpty() && random.nextBoolean()) {
            // Call a custom function (but not the current function)
            List<FunctionInfo> eligibleFunctions = getEligibleFunctions();
                
            if (!eligibleFunctions.isEmpty()) {
                FunctionInfo func = eligibleFunctions.get(random.nextInt(eligibleFunctions.size()));
                
                // For void functions, just call them
                if ("void".equals(func.returnType)) {
                    return new MethodCallStatement(new MethodCallValue(
                        new TokenValue(func.name),
                        generateRandomArguments(func)
                    ));
                } else {
                    // For non-void functions, we can use the result in an assignment or expression
                    if (random.nextBoolean() && !nonUniformVariables.isEmpty()) {
                        // Find compatible variables
                        List<String> compatibleVars = getCompatibleVariables(func.returnType);
                        
                        if (!compatibleVars.isEmpty()) {
                            String target = compatibleVars.get(random.nextInt(compatibleVars.size()));
                            return new AssignmentStatement(
                                new TokenValue(target),
                                new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func))
                            );
                        }
                    }
                    
                    // Create a new variable to store the result
                    String varName = "result_" + randomIdentifier();
                    VarDefStatement stmt = new VarDefStatement(new VarSpecifier(func.returnType, varName))
                        .setValue(new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func)));
                    
                    addVariable(func.returnType, varName, false);
                    return stmt;
                }
            }
        }
        
        // Call a built-in function
        String[] builtIns = {"texture", "sin", "cos", "pow", "max", "min", "length", "normalize", "dot", "cross"};
        String funcName = builtIns[random.nextInt(builtIns.length)];
        String returnType = getBuiltInReturnType(funcName);
        
        // For void functions, just call them
        if ("void".equals(returnType)) {
            return new MethodCallStatement(new MethodCallValue(
                new TokenValue(funcName),
                generateRandomArgumentsForBuiltIn(funcName)
            ));
        } else {
            // For non-void functions, we can use the result in an assignment or expression
            if (random.nextBoolean() && !nonUniformVariables.isEmpty()) {
                // Find compatible variables
                List<String> compatibleVars = getCompatibleVariables(returnType);
                
                if (!compatibleVars.isEmpty()) {
                    String target = compatibleVars.get(random.nextInt(compatibleVars.size()));
                    return new AssignmentStatement(
                        new TokenValue(target),
                        new MethodCallValue(new TokenValue(funcName), generateRandomArgumentsForBuiltIn(funcName))
                    );
                }
            }
            
            // Create a new variable to store the result
            String varName = "result_" + randomIdentifier();
            VarDefStatement stmt = new VarDefStatement(new VarSpecifier(returnType, varName))
                .setValue(new MethodCallValue(new TokenValue(funcName), generateRandomArgumentsForBuiltIn(funcName)));
            
            addVariable(returnType, varName, false);
            return stmt;
        }
    }

    private List<String> getCompatibleVariables(String type) {
        List<String> compatibleVars = new ArrayList<>();
        List<String> typeVars = variablesByType.get(type);
        
        if (typeVars != null) {
            for (String varName : typeVars) {
                VariableInfo varInfo = availableVariables.get(varName);
                if (varInfo != null && !varInfo.isUniform) {
                    compatibleVars.add(varName);
                }
            }
        }
        
        return compatibleVars;
    }

    private GlslStatement generateReturnStatement() {
        // Find the current function
        if (currentFunctionName.isEmpty()) {
            return generateAssignment();
        }
        
        FunctionInfo func = null;
        for (FunctionInfo f : functions) {
            if (f.name.equals(currentFunctionName)) {
                func = f;
                break;
            }
        }
        
        if (func != null && !"void".equals(func.returnType)) {
            // Non-void function - return a value
            currentFunctionReturnPoints.add(currentFunctionName);
            return new ReturnStatement(generateRandomValue(func.returnType, 0));
        } else {
            // Void function or main - return nothing
            currentFunctionReturnPoints.add(currentFunctionName);
            return new ReturnStatement();
        }
    }

    private GlslStatement generateExpressionStatement() {
        // Generate a standalone expression (like i++ or function call without assignment)
        if (random.nextBoolean() && !nonUniformVariables.isEmpty()) {
            // Find numeric variables
            List<String> numericVars = new ArrayList<>();
            for (String varName : nonUniformVariables) {
                VariableInfo varInfo = availableVariables.get(varName);
                if (isNumericType(varInfo.type)) {
                    numericVars.add(varName);
                }
            }
            
            if (!numericVars.isEmpty()) {
                String var = numericVars.get(random.nextInt(numericVars.size()));
                return new IncStatement(new TokenValue(var), random.nextBoolean() ? "++" : "--");
            }
        }
        
        // Function call without using the result (but not the current function)
        if (!functions.isEmpty()) {
            List<FunctionInfo> eligibleFunctions = new ArrayList<>();
            for (FunctionInfo f : functions) {
                if (!f.name.equals(currentFunctionName) && "void".equals(f.returnType)) {
                    eligibleFunctions.add(f);
                }
            }
                
            if (!eligibleFunctions.isEmpty()) {
                FunctionInfo func = eligibleFunctions.get(random.nextInt(eligibleFunctions.size()));
                return new MethodCallStatement(new MethodCallValue(
                    new TokenValue(func.name),
                    generateRandomArguments(func)
                ));
            }
        }
        
        // Fallback to a simple assignment
        return generateAssignment();
    }

    private GlslValue generateRandomValue(String type, int depth) {
        // Try to use a variable if available
        if (random.nextInt(100) < 40) {
            List<String> typeVars = variablesByType.get(type);
            if (typeVars != null && !typeVars.isEmpty()) {
                String varName = typeVars.get(random.nextInt(typeVars.size()));
                return new TokenValue(varName);
            }
        }
        
        // Try to use a function call if available (but not the current function)
        if (random.nextInt(100) < 30) {
            List<FunctionInfo> typeFuncs = functionsByReturnType.get(type);
            if (typeFuncs != null && !typeFuncs.isEmpty()) {
                // Filter out current function
                List<FunctionInfo> eligibleFuncs = new ArrayList<>();
                for (FunctionInfo f : typeFuncs) {
                    if (!f.name.equals(currentFunctionName)) {
                        eligibleFuncs.add(f);
                    }
                }
                
                if (!eligibleFuncs.isEmpty()) {
                    FunctionInfo func = eligibleFuncs.get(random.nextInt(eligibleFuncs.size()));
                    return new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func));
                }
            }
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
                List<String> samplerVars = variablesByType.get("sampler2D");
                if (samplerVars != null && !samplerVars.isEmpty()) {
                    return new TokenValue(samplerVars.get(random.nextInt(samplerVars.size())));
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
        
        // Sometimes use a function call that returns bool (but not the current function)
        if (random.nextBoolean()) {
            List<FunctionInfo> boolFuncs = functionsByReturnType.get("bool");
            if (boolFuncs != null && !boolFuncs.isEmpty()) {
                // Filter out current function
                List<FunctionInfo> eligibleFuncs = new ArrayList<>();
                for (FunctionInfo f : boolFuncs) {
                    if (!f.name.equals(currentFunctionName)) {
                        eligibleFuncs.add(f);
                    }
                }
                
                if (!eligibleFuncs.isEmpty()) {
                    FunctionInfo func = eligibleFuncs.get(random.nextInt(eligibleFuncs.size()));
                    return new MethodCallValue(new TokenValue(func.name), generateRandomArguments(func));
                }
            }
        }
        
        return new OperationValue(
            generateRandomValue("float", depth),
            op,
            generateRandomValue("float", depth)
        );
    }

    private GlslValue[] generateRandomArguments(FunctionInfo func) {
        GlslValue[] args = new GlslValue[func.parameters.size()];
        for (int i = 0; i < func.parameters.size(); i++) {
            ParameterInfo param = func.parameters.get(i);
            
            // Sometimes use a function call as an argument (but not the current function)
            if (random.nextBoolean()) {
                List<FunctionInfo> paramFuncs = functionsByReturnType.get(param.type);
                if (paramFuncs != null && !paramFuncs.isEmpty()) {
                    // Filter out current function
                    List<FunctionInfo> eligibleFuncs = new ArrayList<>();
                    for (FunctionInfo f : paramFuncs) {
                        if (!f.name.equals(currentFunctionName)) {
                            eligibleFuncs.add(f);
                        }
                    }
                    
                    if (!eligibleFuncs.isEmpty()) {
                        FunctionInfo argFunc = eligibleFuncs.get(random.nextInt(eligibleFuncs.size()));
                        args[i] = new MethodCallValue(new TokenValue(argFunc.name), generateRandomArguments(argFunc));
                        continue;
                    }
                }
            }
            
            args[i] = generateRandomValue(param.type, 0);
        }
        return args;
    }

    private GlslValue[] generateRandomArgumentsForBuiltIn(String funcName) {
        int argCount = getBuiltInArgCount(funcName);
        String[] argTypes = getBuiltInArgTypes(funcName);
        
        GlslValue[] args = new GlslValue[argCount];
        for (int i = 0; i < argCount; i++) {
            // Sometimes use a function call as an argument (but not the current function)
            if (random.nextBoolean()) {
                List<FunctionInfo> paramFuncs = functionsByReturnType.get(argTypes[i]);
                if (paramFuncs != null && !paramFuncs.isEmpty()) {
                    // Filter out current function
                    List<FunctionInfo> eligibleFuncs = new ArrayList<>();
                    for (FunctionInfo f : paramFuncs) {
                        if (!f.name.equals(currentFunctionName)) {
                            eligibleFuncs.add(f);
                        }
                    }
                    
                    if (!eligibleFuncs.isEmpty()) {
                        FunctionInfo argFunc = eligibleFuncs.get(random.nextInt(eligibleFuncs.size()));
                        args[i] = new MethodCallValue(new TokenValue(argFunc.name), generateRandomArguments(argFunc));
                        continue;
                    }
                }
            }
            
            args[i] = generateRandomValue(argTypes[i], 0);
        }
        return args;
    }

    private String randomType() {
        String[] types = {"float", "int", "bool", "vec2", "vec3", "vec4", "mat4", "sampler2D"};
        return types[random.nextInt(types.length)];
    }

    private boolean isNumericType(String type) {
        return type.equals("float") || type.equals("int") || type.equals("vec2") || 
               type.equals("vec3") || type.equals("vec4");
    }

    private String getBuiltInReturnType(String funcName) {
        switch (funcName) {
            case "texture": return "vec4";
            case "sin": case "cos": case "pow": return "float";
            case "max": case "min": 
                return random.nextBoolean() ? "float" : "vec" + (random.nextInt(3) + 2);
            case "length": return "float";
            case "normalize": 
                return "vec" + (random.nextInt(3) + 2);
            case "dot": return "float";
            case "cross": return "vec3";
            default: return "float";
        }
    }

    private int getBuiltInArgCount(String funcName) {
        switch (funcName) {
            case "texture": return 2;
            case "sin": case "cos": return 1;
            case "pow": case "max": case "min": return 2;
            case "length": return 1;
            case "normalize": return 1;
            case "dot": return 2;
            case "cross": return 2;
            default: return 1;
        }
    }

    private String[] getBuiltInArgTypes(String funcName) {
        switch (funcName) {
            case "texture": return new String[]{"sampler2D", "vec2"};
            case "sin": case "cos": return new String[]{"float"};
            case "pow": case "max": case "min": 
                String type = random.nextBoolean() ? "float" : "vec" + (random.nextInt(3) + 2);
                return new String[]{type, type};
            case "length": return new String[]{"vec" + (random.nextInt(3) + 2)};
            case "normalize": 
                return new String[]{"vec" + (random.nextInt(3) + 2)};
            case "dot": 
                String vecType = "vec" + (random.nextInt(3) + 2);
                return new String[]{vecType, vecType};
            case "cross": return new String[]{"vec3", "vec3"};
            default: return new String[]{"float"};
        }
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
        AdvancedGlslGenerator generator = new AdvancedGlslGenerator("330 core");
        generator.generate();
        generator.saveToFile("advanced_random.glsl");
    }
}