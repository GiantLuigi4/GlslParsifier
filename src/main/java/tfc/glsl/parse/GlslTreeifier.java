package tfc.glsl.parse;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;
import tfc.glsl.meta.*;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.GlslBlockSegment;
import tfc.glsl.segments.GlslCodeSegment;
import tfc.glsl.segments.GlslMemberSegment;
import tfc.glsl.statements.*;
import tfc.glsl.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlslTreeifier {
    protected static void checkSpecial(TokenStreamer streamer, char expected) {
        if (!streamer.current().is(expected)) {
            throw new RuntimeException("Unexpected symbol");
        }
    }

    private static boolean isSpecial(TokenStreamer streamer, char expected) {
        return streamer.current().is(expected);
    }

    private static LayoutQualifier layout(TokenStreamer streamer) {
        checkSpecial(streamer, '(');
        streamer.advance();
        LayoutQualifier qualifier = new LayoutQualifier();

        loopA:
        while (true) {
            StringBuilder txt = new StringBuilder();
            txt.append(streamer.current().string());
            streamer.advance();

            while (true) {
                if (isSpecial(streamer, ')')) {
                    streamer.advance();
                    qualifier.addSegment(txt.toString());
                    break loopA;
                } else if (isSpecial(streamer, ',')) {
                    streamer.advance();
                    break;
                }
                txt.append(" ").append(streamer.current().string());
                streamer.advance();
            }
            qualifier.addSegment(txt.toString());
        }

        return qualifier;
    }

    private static GlslValue nextMonoTokenValue(TokenStreamer streamer) {
        switch (streamer.current().string()) {
            case "true" -> {
                streamer.advance();
                return new BooleanValue(true);
            }
            case "false" -> {
                streamer.advance();
                return new BooleanValue(false);
            }
            default -> {
                String str = streamer.current().string();

                streamer.advance();
                try {
                    if (str.contains(".")) {
                        try {
                            return new ConstantValue(
                                    Float.parseFloat(str)
                            );
                        } catch (Throwable err) {
                            return new ConstantValue(
                                    Double.parseDouble(str)
                            );
                        }
                    } else {
                        try {
                            return new ConstantValue(
                                    Integer.parseInt(str)
                            );
                        } catch (Throwable err) {
                            return new ConstantValue(
                                    Long.parseLong(str)
                            );
                        }
                    }
                } catch (Throwable err) {
                }

                return new TokenValue(str);
            }
        }
    }

    private static GlslValue chainAccess(GlslValue value, TokenStreamer streamer) {
        breakLoop:
        while (true) {
            GlslToken token = streamer.current();
            if (token.is(TokenType.EOS))
                return value;

            switch (token.string()) {
                case "[" -> {
                    streamer.advance();
                    AccessArrayValue value1 = new AccessArrayValue(
                            value,
                            nextValue(streamer)
                    );
                    streamer.advance();
                    value = value1;
                }
                case "(" -> {
                    streamer.advance();

                    List<GlslValue> params = new ArrayList<>();
                    while (!streamer.current().is(')')) {
                        GlslValue param = nextValue(streamer);
                        params.add(param);
                        GlslToken current = streamer.current();
                        if (!(current.is(',') || current.is(')'))) {
                            throw new RuntimeException("Unexpected symbol");
                        }
                        if (current.is(','))
                            streamer.advance();
                    }
                    streamer.advance();

                    value = new MethodCallValue(
                            value,
                            params.toArray(new GlslValue[0])
                    );
                }
                case "." -> {
                    streamer.advance();
                    AccessMemberValue value1 = new AccessMemberValue(
                            value,
                            nextMonoTokenValue(streamer)
                    );
                    value = value1;
                }

                default -> {
                    break breakLoop;
                }
            }
        }

        return value;
    }

    private static GlslValue nextInnerValue(TokenStreamer streamer) {
        switch (streamer.current().string()) {
            case "(" -> {
                streamer.advance();
                GlslValue value = nextValue(streamer);
                streamer.advance();
                return new ParenthValue(value);
            }
            case "-" -> {
                streamer.advance();
                GlslValue value = new UnaryOperation(
                        "-",
                        nextValueNoExpr(streamer)
                );
                return value;
            }
        }

        GlslValue value = nextMonoTokenValue(streamer);

        return value;
    }

    public static GlslValue nextValueNoExpr(TokenStreamer streamer) {
//        throw new RuntimeException("NYI");

//        ((new int[2][2])[0] = new int[5])[0] = 2;

        GlslValue value = nextInnerValue(streamer);
        value = chainAccess(value, streamer);

        if (streamer.current().is(TokenType.EOS))
            return value;

        switch (streamer.current().string()) {
            case ";" -> {
                return value;
            }
        }

        if (matchAssignmentOverload(streamer.current().string())) {
            GlslToken token = streamer.current();
            streamer.advance();

            AssignmentValue value1 = new AssignmentValue(
                    value,
                    nextValue(streamer)
            );

            if (!token.string().equals("=")) {
                value1.setAuxiliaryOp(
                        token.string().substring(0, 1)
                );
            }

            return value1;
        }

        return value;
    }

    private static GlslValue nextValue(TokenStreamer streamer) {
//        return nextValueNoExpr(streamer);
        return ExpressionParser.doParse(
                streamer, () -> nextValueNoExpr(streamer)
        );
    }

    private static ArraySpecifier nextArraySpecifier(TokenStreamer streamer) {
        List<GlslValue> values = new ArrayList<>();
        while (streamer.current().is('[')) {
            streamer.advance();
            values.add(nextValue(streamer));
            streamer.advance();
        }
        if (values.isEmpty()) return null;
        return new ArraySpecifier(values);
    }

    private static VarSpecifier nextVarSpecifier(TokenStreamer streamer) {
        GlslToken typeToken = streamer.current();
        if (!(typeToken.is(TokenGroup.TYPE) || typeToken.is(TokenType.LITERAL))) {
            throw new RuntimeException("Unexpected symbol");
        }
        streamer.advance();
        ArraySpecifier specifier = nextArraySpecifier(streamer);

        GlslToken name = streamer.current();
        streamer.advance();
        ArraySpecifier specifier1 = nextArraySpecifier(streamer);

        VarSpecifier varSpec = new VarSpecifier(typeToken.string(), name.string());
        varSpec.setArray(specifier1 == null ? specifier : specifier1);
        return varSpec;
    }

    private static GlslSegment nextMemberDef(TokenStreamer streamer, StorageQualifier qualifier) {
        VarSpecifier specifier = nextVarSpecifier(streamer);
        return new GlslMemberSegment(
                qualifier, specifier
        );
    }

    private static Member nextMember(TokenStreamer streamer) {
        Member member = new Member(
                nextVarSpecifier(streamer)
        );
        // TODO: attribute qualifiers?
        // TODO: default values?
        return member;
    }

    private static GlslSegment nextVarBlock(TokenStreamer streamer, StorageQualifier qualifier) {
        GlslToken first = streamer.current();
        GlslToken name;
        if (first.is('{')) {
            name = null;
        } else {
            name = first;
            streamer.advance();
        }
        streamer.advance();

        GlslBlockSegment segment = new GlslBlockSegment(qualifier);
        if (name != null) {
            segment.setName(name.string());
        }
        GlslToken current = streamer.current();
        while (!current.is('}')) {
            nextMember(streamer);
            popSemis(streamer);
            current = streamer.current();
        }

        streamer.advance();
        current = streamer.current();
        if (current.is(';')) {
            return segment;
        }

        segment.setInstance(current.string());
        streamer.advance();

        return segment;
    }

    private static GlslSegment nextStorage(TokenStreamer streamer) {
        GlslToken storage = streamer.current();
        streamer.advance();
        TokenType type = storage.type();
        StorageQualifier qualifier = switch (type) {
            case UNIFORM -> StorageQualifier.UNIFORM;
            case IN -> StorageQualifier.IN;
            case OUT -> StorageQualifier.OUT;
            case BUFFER -> StorageQualifier.BUFFER;
            case STRUCT -> StorageQualifier.STRUCT;
            case VARYING -> StorageQualifier.VARYING;
            case ATTRIBUTE -> StorageQualifier.ATTRIBUTE;
            default -> throw new RuntimeException("Unexpected symbol");
        };

        GlslToken current = streamer.current();
        boolean isBlock = false;
        if (!current.is('{')) {
            GlslToken peek = streamer.peek(1);
            if (peek.is('{')) {
                isBlock = true;
            }
        } else {
            isBlock = true;
        }

        if (isBlock) {
            return nextVarBlock(
                    streamer, qualifier
            );
        } else {
            return nextMemberDef(
                    streamer, qualifier
            );
        }
    }

    private static void popSemis(TokenStreamer streamer) {
        while (streamer.current().is(';')) {
            streamer.advance();
        }
    }

    // regex is a potato, so this is better
    private static boolean matchAssignmentOverload(String str) {
        if (str.length() == 1) return str.equals("=");

        char echar = str.charAt(0);
        switch (echar) {
            case '+', '-', '/', '*' -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static GlslStatement nextFor(TokenStreamer streamer) {
        streamer.advance(); // pop "for"
        if (!streamer.current().is('('))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();

        GlslStatement vd;
        if (streamer.current().is(';'))
            vd = new ArbitraryStatement("");
        else vd = nextStatement(streamer);
        streamer.advance();

        GlslValue comparison;
        if (streamer.current().is(';'))
            comparison = new BooleanValue(true);
        else comparison = nextValue(streamer);
        streamer.advance();

        GlslStatement incr;
        if (streamer.current().is(';'))
            incr = new ArbitraryStatement("");
        else incr = nextStatement(streamer);
        streamer.advance();

        ForStatement forSt = new ForStatement(vd, comparison, incr);

        if (!streamer.current().is('{'))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();

        nextBody(streamer, forSt::addStatement);

        return forSt;
    }

    private static GlslStatement nextWhile(TokenStreamer streamer) {
        streamer.advance(); // pop "while"

        if (!streamer.current().is('('))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();

        GlslValue condition;
        if (streamer.current().is(')'))
            condition = new BooleanValue(true);
        else condition = nextValue(streamer);
        streamer.advance();

        WhileStatement statement = new WhileStatement(condition);
        if (!streamer.current().is('{'))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();
        nextBody(streamer, statement::addStatement);

        return statement;
    }

    private static final GlslValue PLACEHOLDER_TRUE = new BooleanValue(true);

    private static GlslStatement nextDo(TokenStreamer streamer) {
        streamer.advance(); // pop "do"
        if (!streamer.current().is('{'))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();

        DoWhileStatement statement = new DoWhileStatement(PLACEHOLDER_TRUE);
        nextBody(streamer, statement::addStatement);

        if (!streamer.current().is("while"))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();
        if (!streamer.current().is("("))
            throw new RuntimeException("Unexpected symbol");
        streamer.advance();

        if (!streamer.current().is(')'))
            statement.setCondition(nextValue(streamer));
        streamer.advance();

        return statement;
    }

    private static GlslStatement nextStatement(TokenStreamer streamer) {
        // if statement type can be immediately resolved, statement is that statement type
        // elsewise, statement is var def or var assignment based upon position of =

        GlslToken token = streamer.current();

        switch (token.type()) {
            case IF -> {
                throw new RuntimeException("NYI");
            }
            case FOR -> {
                return nextFor(streamer);
            }
            case DO -> {
                return nextDo(streamer);
            }
            case WHILE -> {
                return nextWhile(streamer);
            }
            case CONTINUE -> {
                return ContinueStatement.INSTANCE;
            }
            case BREAK -> {
                return BreakStatement.INSTANCE;
            }
            case RETURN -> {
                streamer.advance();
                token = streamer.current();
                if (token.is(';')) {
                    return new ReturnStatement();
                } else {
                    return new ReturnStatement(nextValue(streamer));
                }
            }
            default -> {
                int index = streamer.index();
                GlslValue value = nextValueNoExpr(streamer);
                if (
                        value.getValueType() == ValueType.ASSIGNMENT
                ) {
                    return new AssignmentStatement(
                            ((AssignmentValue) value).getRef(),
                            ((AssignmentValue) value).getValue()
                    ).setAuxiliaryOp(((AssignmentValue) value).getAuxiliaryOp());
                } else if (value.getValueType() == ValueType.FUNCTION) {
                    return new MethodCallStatement(
                            (MethodCallValue) value
                    );
                }
                streamer.setIndex(index);

                {
                    VarSpecifier varSpec = nextVarSpecifier(streamer);

                    GlslToken token1 = streamer.current();

                    if (token1.is('=') || token1.is(TokenType.OPERATOR)) {
                        String str = token1.string();
                        if (!str.equals("=")) {
                            throw new RuntimeException("Unexpected symbol");
                        }

                        VarDefStatement statement = new VarDefStatement(varSpec);
                        streamer.advance();
                        statement.setValue(nextValue(streamer));
                        return statement;
                    } else if (token1.is(';')) {
                        return new VarDefStatement(varSpec);
                    } else {
                        throw new RuntimeException("Unexpected symbol");
                    }
                }
            }
        }
    }

    private static void nextBody(TokenStreamer streamer, Consumer<GlslStatement> addStatement) {
        while (!streamer.current().is('}')) {
            popSemis(streamer);
            if (streamer.current().is('}'))
                break;

            addStatement.accept(nextStatement(streamer));
        }
        streamer.advance();
    }

    private static GlslSegment nextFunction(VarSpecifier specifier, TokenStreamer streamer) {
        if (!streamer.current().is('(')) {
            throw new RuntimeException("Unexpected symbol");
        }

        GlslCodeSegment codeSegment = new GlslCodeSegment(
                specifier.getType(),
                specifier.getName()
        );
        streamer.advance();
        while (!streamer.current().is(')')) {
            VarSpecifier varSpec = nextVarSpecifier(streamer);
            codeSegment.addParam(new Parameter(varSpec));
        }
        streamer.advance();

        if (!streamer.current().is('{')) {
            throw new RuntimeException("Unexpected symbol");
        }
        streamer.advance();

        nextBody(streamer, codeSegment::addStatement);

        return codeSegment;
    }

    private static GlslSegment nextSegment(TokenStreamer streamer) {
        popSemis(streamer);
        LayoutQualifier layoutQualif = null;
        List<GlslToken> attributes = new ArrayList<>();
        while (true) {
            GlslToken current = streamer.current();

//            GlslSegment segment = trySegment(streamer);

            if (current.is(TokenGroup.ATTRIBUTE)) {
                attributes.add(current);
            } else if (current.is(TokenGroup.STORAGE_TYPE)) {
                GlslSegment segment = nextStorage(streamer);
                switch (segment.getSegmentType()) {
                    case BLOCK_DEF:
                        ((GlslBlockSegment) segment).setLayout(layoutQualif);
                        break;
                    case MEMBER_DEF:
                        ((GlslMemberSegment) segment).setLayout(layoutQualif);
                        // TODO: attribute qualifiers
                        break;

                    default:
                        throw new RuntimeException("wat");
                }
                return segment;
            } else if (current.is(TokenType.LAYOUT)) {
                streamer.advance();
                layoutQualif = layout(streamer);
            } else {
                VarSpecifier specifier = nextVarSpecifier(streamer);
                return nextFunction(specifier, streamer);
//                throw new RuntimeException("Unexpected symbol");
            }
        }
    }

    private static void parseTo(TokenStreamer streamer, GlslFile file) {
        while (!streamer.isDone()) {
            file.addSegment(nextSegment(streamer));
            streamer.clearBuffer();
        }
    }

    public static GlslFile toTree(TokenStreamer streamer) {
        GlslToken directive = streamer.current();
        if (!directive.is(TokenType.VERSION_DIRECTIVE)) {
            throw new RuntimeException("Unspecified version.");
        }
        streamer.advance();
        GlslToken versionNumber = streamer.current();
        streamer.advance();

        GlslFile file = new GlslFile(versionNumber.string());

        parseTo(streamer, file);

        return file;
    }
}
