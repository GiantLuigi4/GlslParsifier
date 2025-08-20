package fuzzers.custom;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.meta.Parameter;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.AssignmentStatement;
import tfc.glsl.segments.GlslCodeSegment;
import tfc.glsl.segments.GlslMemberSegment;
import tfc.glsl.segments.GlslVarSegment;
import tfc.glsl.statements.*;
import tfc.glsl.value.*;

import java.io.FileOutputStream;
import java.util.*;
import java.util.function.Consumer;

public class Fuzzer {
    private static final int MIN_GLOBALS = 3;
    private static final int MAX_GLOBALS = 5;
    private static final int MIN_FUNCTIONS = 3;
    private static final int MAX_FUNCTIONS = 5;
    private static final int MIN_PARAMS = 0;
    private static final int MAX_PARAMS = 4;
    private static final int MIN_STATEMENTS = 3;
    private static final int MAX_STATEMENTS = 13;
    private static final int MAX_VALUE_DEPTH = 4;
    private static final int MAX_DEPTH = 5;
    private static final int MAX_IF_CHAIN = 2;
    GlslFile file;
    Random rng = new Random();

    String[] numericTypes = new String[]{
            "float", "int"
    };
    String[] types = new String[]{
            "float", "int", "bool", "vec2", "vec3", "vec4", "mat4", "mat3", "mat2"
    };
    String[] typesWithVoid = new String[]{
            "float", "int", "bool", "vec2", "vec3", "vec4", "mat4", "mat3", "mat2", "void"
    };
    String[] operators = new String[]{"+", "-", "/", "*"};
    String[] operatorsOrNull = new String[]{"+", "-", "/", "*", null};
    String[] operatorsComp = new String[]{"<", "<=", ">", ">=", "==", "!="};
    String[] operatorsJoin = new String[]{"&&", "||"};
    Map<String, List<String>> globalsByType = new HashMap<>();
    Scope scope = new Scope();

    Map<String, List<GlslCodeSegment>> functions = new HashMap<>();

    public Fuzzer() {
        List<String> versions = new ArrayList<>();
        String[] mainline = new String[]{
                "100",
                "110",
                "120",
                "130",
                "140",
                "150",
                "330",
                "400",
                "410",
                "420",
                "430",
                "440",
                "450",
                "460",
        };
        for (String s : mainline) {
            versions.add(s);
            versions.add(s + " core");
        }
        versions.add("100 es");
        versions.add("300 es");
        versions.add("310 es");
        versions.add("320 es");

        int i = rng.nextInt(versions.size());
        file = new GlslFile(versions.get(i));

        scope.myVars = globalsByType;
    }

    public GlslSegment generateGlobal() {
        int value = rng.nextInt(3);
        switch (value) {
            case 0 -> {
                GlslMemberSegment member = new GlslMemberSegment(
                        StorageQualifier.UNIFORM,
                        new VarSpecifier(randomType(), "g_" + randomIdentifier())
                );
                addGlobal(
                        member.getMember().getVar().getType(),
                        member.getMember().getVar().getName()
                );
                return member;
            }
            case 1 -> {
                GlslVarSegment var = new GlslVarSegment(new VarSpecifier(randomType(), "g_" + randomIdentifier()));
                var.setValue(nextValue(var.getVar().getType()));
                addGlobal(
                        var.getVar().getType(),
                        var.getVar().getName()
                );
                return var;
            }
            case 2 -> {
                // TODO: should generate a ubo
                GlslMemberSegment member = new GlslMemberSegment(
                        StorageQualifier.UNIFORM,
                        new VarSpecifier(randomType(), "g_" + randomIdentifier())
                );
                addGlobal(
                        member.getMember().getVar().getType(),
                        member.getMember().getVar().getName()
                );
                return member;
            }
        }
        throw new RuntimeException("wat");
    }

    private GlslSegment generateFunction() {
        GlslCodeSegment func = new GlslCodeSegment(
                randomFuncType(),
                "func_" + randomIdentifier()
        );
        int statements = randRange(MIN_STATEMENTS, MAX_STATEMENTS);
        boolean[] endBlock = new boolean[1];
        scope = new Scope(scope);

        int params = randRange(MIN_PARAMS, MAX_PARAMS);
        for (int i = 0; i < params; i++) {
            VarSpecifier spec = new VarSpecifier(randomType(), "p_" + randomIdentifier());
            func.addParam(new Parameter(spec));
            scope.addVar(spec.getType(), spec.getName());
        }

        for (int i = 0; i < statements; i++) {
            func.addStatement(nextStatement(func.getType(), endBlock, i / (float) statements));
            if (endBlock[0]) break;
        }
        if (!endBlock[0]) {
            if (!func.getType().equals("void") || rng.nextBoolean())
                func.addStatement(new ReturnStatement(nextValue(func.getType())));
        }
        scope = scope.parent;
        getList(functions, func.getType()).add(func);
        return func;
    }

    private <T> List<T> getList(Map<String, List<T>> map, String key) {
        List<T> li = map.get(key);
        //noinspection Java8MapApi
        if (li == null) {
            li = new ArrayList<>();
            map.put(key, li);
        }
        return li;
    }

    int generationDepth = 0;

    private void nextBody(String retType, Consumer<GlslStatement> addStatement) {
        generationDepth++;
        scope = new Scope(scope);
        boolean[] endBlock = new boolean[1];
        int statements = randRange(MIN_STATEMENTS, MAX_STATEMENTS);
        for (int i = 0; i < statements; i++) {
            addStatement.accept(nextStatement(retType, endBlock, i / (float) statements));
            if (endBlock[0]) break;
        }
        if (!endBlock[0]) {
            if (rng.nextBoolean())
                addStatement.accept(new ReturnStatement(nextValue(retType)));
        }
        scope = scope.parent;
        generationDepth--;
    }

    private GlslStatement nextStatement(String retType, boolean[] terminateBlock, float chanceOfReturn) {
        if (rng.nextFloat() < chanceOfReturn) {
            terminateBlock[0] = true;
            return new ReturnStatement(nextValue(retType));
        }

        List<Integer> options = new ArrayList<>();
        options.add(0);
        String typeTo = randomType();
        String varTo = scope.getRandomValueForKey(rng, typeTo, true);
        if (varTo != null) {
            options.add(1);
            if (typeTo.equals("int")) {
                options.add(2);
            }
        }
        if (generationDepth < MAX_DEPTH) {
            options.add(3);
            options.add(4);
            options.add(5);
            options.add(6);
        }

        switch (options.get(rng.nextInt(options.size()))) {
            case 0 -> {
                String type = randomType();
                VarDefStatement vd = new VarDefStatement(
                        new VarSpecifier(type, "v_" + randomIdentifier())
                ).setValue(nextValue(type));
                scope.addVar(vd.getVar().getType(), vd.getVar().getName());
                return vd;
            }
            case 1 -> {
                String op;
                if (typeTo.equals("bool")) op = null;
                else op = nextOperatorOrNull();
                AssignmentStatement value = new AssignmentStatement(
                        new TokenValue(varTo),
                        op,
                        nextValue(typeTo)
                );
                return value;
            }
            case 2 -> {
                IncStatement inc = new IncStatement(
                        new TokenValue(varTo),
                        rng.nextBoolean() ? "++" : "--"
                );
                return inc;
            }
            case 3 -> {
                DoWhileStatement doWhile = new DoWhileStatement(
                        nextValue("bool")
                );
                nextBody(retType, doWhile::addStatement);
                return doWhile;
            }
            case 4 -> {
                WhileStatement doWhile = new WhileStatement(
                        nextValue("bool")
                );
                nextBody(retType, doWhile::addStatement);
                return doWhile;
            }
            case 5 -> {
                String type = randomNumericType();
                String name = "i_" + randomIdentifier();
                ForStatement statement = new ForStatement(
                        new VarDefStatement(new VarSpecifier(type, name)).setValue(nextValue(type)),
                        nextValue("bool"),
                        new IncStatement(new TokenValue(name), "++")
                );
                nextBody(retType, statement::addStatement);
                return statement;
            }
            case 6 -> {
                int number = randRange(1, MAX_IF_CHAIN);
                ConditionalStatement statement = new ConditionalStatement();
                for (int i = 0; i < number; i++) {
                    ConditionalStatement.ConditionalCode conditionalCode = new ConditionalStatement.ConditionalCode(
                            nextValue("bool")
                    );
                    nextBody(retType, conditionalCode::addStatement);
                    statement.addStep(conditionalCode);
                }
                return statement;
            }
            // TODO: for loop, if/else chains
            default -> throw new RuntimeException("wat");
        }
    }

    private String[] compatibleOperands(String type) {
        switch (type) {
            case "vec4" -> {
                return new String[]{"vec4", "mat4", "float", "int"};
            }
            case "vec3" -> {
                return new String[]{"vec3", "mat3", "float", "int"};
            }
            case "vec2" -> {
                return new String[]{"vec2", "mat2", "float", "int"};
            }
            case "float" -> {
                return new String[]{"float", "int"};
            }
            case "int" -> {
                return new String[]{"int"};
            }
            case "mat4" -> {
                return new String[]{"mat4"};
            }
            case "mat3" -> {
                return new String[]{"mat3"};
            }
            case "mat2" -> {
                return new String[]{"mat2"};
            }
            case "bool" -> {
                return null;
            }
            default -> throw new RuntimeException("NYI: " + type);
        }
    }

    private String[] filterOperands(String type, String[] operands, String operator) {
        switch (operator) {
            case "/", "+", "-", "*" -> {
                switch (type) {
                    case "vec4", "vec3", "vec2" -> {
                        List<String> li = new ArrayList<>();
                        for (String operand : operands) {
                            if (!operand.startsWith("mat"))
                                li.add(operand);
                        }
                        operands = li.toArray(new String[0]);
                    }
                    case "mat4", "mat3", "mat2" -> {
                        List<String> li = new ArrayList<>();
                        for (String operand : operands) {
                            if (!operand.startsWith("vec"))
                                li.add(operand);
                        }
                        operands = li.toArray(new String[0]);
                    }
                }
            }
        }
        return operands;
    }

    int valueDepth = 0;

    private GlslValue nextValue(String type) {
        if (type.equals("void")) return null;

        if (valueDepth > MAX_VALUE_DEPTH)
            return randomConstant(type);

        valueDepth++;
        List<Integer> options = new ArrayList<>();
        String[] operands = compatibleOperands(type);
        options.add(0);
        String operator;
        if (operands != null) {
            operator = nextOperator();
            operands = filterOperands(type, operands, operator);
            if (operands != null)
                options.add(1);
        }
        if (type.startsWith("vec")) {
            options.add(2);
        }
        if (functions.containsKey(type)) {
            options.add(3);
        }
        if (type.equals("bool")) {
            options.add(4);
            options.add(5);
        }

        if (scope.hasValueForKey(type)) options.add(-1);

        switch (options.get(rng.nextInt(options.size()))) {
            case 0 -> {
                GlslValue value = randomConstant(type);
                valueDepth--;
                return value;
            }
            case 1 -> {
                int i = rng.nextInt(operands.length);
                GlslValue value = new OperationValue(
                        nextValue(type),
                        nextOperator(),
                        nextValue(operands[i])
                );
                valueDepth--;
                return value;
            }
            case 2 -> {
                GlslValue value = new OperationValue(
                        nextValue(type),
                        "*",
                        nextValue("mat" + type.charAt(type.length() - 1))
                );
                valueDepth--;
                return value;
            }
            case 3 -> {
                List<GlslCodeSegment> funcs = functions.get(type);
                GlslCodeSegment tgt = nextElement(funcs);
                List<Parameter> params = tgt.getParams();
                List<GlslValue> args = new ArrayList<>();
                for (Parameter param : params)
                    args.add(nextValue(param.getVar().getType()));
                GlslValue value = new MethodCallValue(new TokenValue(tgt.getName()), args.toArray(new GlslValue[0]));
                valueDepth--;
                return value;
            }
            case 4 -> {
                String typeSelected = nextElement(new String[]{
                        "int", "float"
                });
                GlslValue value = new OperationValue(
                        nextValue(typeSelected),
                        nextCompOperator(),
                        nextValue(typeSelected)
                );
                valueDepth--;
                return value;
            }
            case 5 -> {
                GlslValue value = new OperationValue(
                        nextValue("bool"),
                        nextJoinOperator(),
                        nextValue("bool")
                );
                valueDepth--;
                return value;
            }
            default -> {
                GlslValue value = new TokenValue(scope.getRandomValueForKey(rng, type));
                valueDepth--;
                return value;
            }
        }
    }

    private String nextElement(String[] li) {
        return li[rng.nextInt(li.length)];
    }

    private <T> T nextElement(List<T> li) {
        return li.get(rng.nextInt(li.size()));
    }

    private String nextOperator() {
        return operators[rng.nextInt(operators.length)];
    }

    private String nextOperatorOrNull() {
        return operatorsOrNull[rng.nextInt(operatorsOrNull.length)];
    }

    private String nextCompOperator() {
        return operatorsComp[rng.nextInt(operatorsComp.length)];
    }

    private String nextJoinOperator() {
        return operatorsJoin[rng.nextInt(operatorsJoin.length)];
    }

    private GlslValue randomConstant(String type) {
        // Otherwise generate a literal or constructor
        switch (type) {
            case "float":
                return new ConstantValue(rng.nextFloat() * 10);
            case "int":
                return new ConstantValue(rng.nextInt(100));
            case "bool":
                return new BooleanValue(rng.nextBoolean());
            case "vec2":
                return new MethodCallValue(
                        new TokenValue("vec2"),
                        nextValue("float"),
                        nextValue("float")
                );
            case "vec3":
                return switch (rng.nextInt(3)) {
                    case 0 -> new MethodCallValue(
                            new TokenValue("vec3"),
                            nextValue("float"),
                            nextValue("float"),
                            nextValue("float")
                    );
                    case 1 -> new MethodCallValue(
                            new TokenValue("vec3"),
                            nextValue("vec2"),
                            nextValue("float")
                    );
                    case 2 -> new MethodCallValue(
                            new TokenValue("vec3"),
                            nextValue("float"),
                            nextValue("vec2")
                    );
                    default -> throw new RuntimeException("wat");
                };
            case "vec4":
                return switch (rng.nextInt(7)) {
                    case 0 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("float"),
                            nextValue("float"),
                            nextValue("float"),
                            nextValue("float")
                    );
                    case 1 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("vec2"),
                            nextValue("float"),
                            nextValue("float")
                    );
                    case 2 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("vec3"),
                            nextValue("float")
                    );
                    case 3 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("vec2"),
                            nextValue("vec2")
                    );
                    case 4 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("float"),
                            nextValue("vec3")
                    );
                    case 5 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("float"),
                            nextValue("float"),
                            nextValue("vec2")
                    );
                    case 6 -> new MethodCallValue(
                            new TokenValue("vec4"),
                            nextValue("float"),
                            nextValue("vec2"),
                            nextValue("float")
                    );
                    default -> throw new RuntimeException("wat");
                };
            case "mat4":
                switch (rng.nextInt(2)) {
                    case 0 -> {
                        return new MethodCallValue(
                                new TokenValue("mat4"),
                                nextValue("float")
                        );
                    }
                    case 1 -> {
                        return new MethodCallValue(
                                new TokenValue("mat4"),
                                nextValue("vec4"),
                                nextValue("vec4"),
                                nextValue("vec4"),
                                nextValue("vec4")
                        );
                    }
                    default -> throw new RuntimeException("wat");
                }
            case "mat3":
                switch (rng.nextInt(2)) {
                    case 0 -> {
                        return new MethodCallValue(
                                new TokenValue("mat3"),
                                nextValue("float")
                        );
                    }
                    case 1 -> {
                        return new MethodCallValue(
                                new TokenValue("mat3"),
                                nextValue("vec3"),
                                nextValue("vec3"),
                                nextValue("vec3")
                        );
                    }
                    default -> throw new RuntimeException("wat");
                }
            case "mat2":
                switch (rng.nextInt(2)) {
                    case 0 -> {
                        return new MethodCallValue(
                                new TokenValue("mat2"),
                                nextValue("float")
                        );
                    }
                    case 1 -> {
                        return new MethodCallValue(
                                new TokenValue("mat2"),
                                nextValue("vec2"),
                                nextValue("vec2")
                        );
                    }
                    default -> throw new RuntimeException("wat");
                }
            default:
                return new ConstantValue(0.5f);
        }
    }

    private int randRange(int min, int max) {
        if (max <= min) return max;
        return rng.nextInt(max - min) + min;
    }

    private void generate() {
        int globals = randRange(MIN_GLOBALS, MAX_GLOBALS);
        for (int i = 0; i < globals; i++) {
            file.addSegment(generateGlobal());
        }

        int funcs = randRange(MIN_FUNCTIONS, MAX_FUNCTIONS);
        for (int i = 0; i < funcs; i++) {
            file.addSegment(generateFunction());
        }
    }

    private String randomType() {
        return types[rng.nextInt(types.length)];
    }

    private String randomNumericType() {
        return numericTypes[rng.nextInt(numericTypes.length)];
    }

    private String randomFuncType() {
        return typesWithVoid[rng.nextInt(typesWithVoid.length)];
    }

    private boolean isNumericType(String type) {
        return type.equals("float") || type.equals("int") || type.equals("vec2") ||
                type.equals("vec3") || type.equals("vec4");
    }

    private String getBuiltInReturnType(String funcName) {
        switch (funcName) {
            case "texture":
                return "vec4";
            case "sin":
            case "cos":
            case "pow":
                return "float";
            case "max":
            case "min":
                // These can return various types based on inputs
                return rng.nextBoolean() ? "float" : "vec" + (rng.nextInt(3) + 2);
            case "length":
                return "float";
            case "normalize":
                return "vec" + (rng.nextInt(3) + 2);
            case "dot":
                return "float";
            case "cross":
                return "vec3";
            default:
                return "float";
        }
    }

    private int getBuiltInArgCount(String funcName) {
        switch (funcName) {
            case "texture":
                return 2;
            case "sin":
            case "cos":
                return 1;
            case "pow":
            case "max":
            case "min":
                return 2;
            case "length":
                return 1;
            case "normalize":
                return 1;
            case "dot":
                return 2;
            case "cross":
                return 2;
            default:
                return 1;
        }
    }

    private String randomIdentifier() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb + "_" + System.nanoTime();
    }

    private void addGlobal(String type, String name) {
        List<String> vars = globalsByType.get(type);
        //noinspection Java8MapApi
        if (vars == null) {
            vars = new ArrayList<>();
            globalsByType.put(type, vars);
        }
        vars.add(name);
    }

    public void saveToFile(String filename) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(file.asString().getBytes());
            out.flush();
            out.close();
            System.out.println("Generated GLSL saved to: " + filename);
            Thread.sleep(10000); // sleep to try to make sure the file is fully written
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Fuzzer fuzzer = new Fuzzer();
        fuzzer.generate();
        fuzzer.saveToFile("fuzzed.glsl");
    }
}
