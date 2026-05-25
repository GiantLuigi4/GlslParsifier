package tfc.glsl.parse;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;
import tfc.glsl.ex.ParseException;
import tfc.glsl.meta.*;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.*;
import tfc.glsl.statements.*;
import tfc.glsl.util.DuplicationUtil;
import tfc.glsl.util.LiteralNumber;
import tfc.glsl.value.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GlslTreeifier {
    protected static void checkSpecial(TokenStreamer streamer, char expected) {
        if (!streamer.current().is(expected)) {
            throw new ParseException("Unexpected symbol");
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
                    LiteralNumber number = new LiteralNumber(str);
                    return new ConstantValue(number);
                } catch (Throwable ignored) {
                }
                // TODO: hexadecimal number type

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
                    if (streamer.current().is(']')) {
                        streamer.advance();
                        if (!streamer.current().is('(')) {
                            throw new ParseException("Unexpected symbol");
                        }
                        streamer.advance();
                        List<GlslValue> values = new ArrayList<>();
                        if (!streamer.current().is(')')) {
                            while (true) {
                                values.add(nextValue(streamer, false));
                                if (streamer.current().is(')')) {
                                    break;
                                } else {
                                    streamer.advance();
                                }
                            }
                        }
                        streamer.advance();
                        return new CreateArrayValue(value, values);
                    } else {
                        AccessArrayValue value1 = new AccessArrayValue(
                                value,
                                nextValue(streamer)
                        );
                        streamer.advance();
                        value = value1;
                    }
                }
                case "(" -> {
                    streamer.advance();

                    List<GlslValue> params = new ArrayList<>();
                    while (!streamer.current().is(')')) {
                        GlslValue param = nextValue(streamer, false);
                        params.add(param);
                        GlslToken current = streamer.current();
                        if (!(current.is(',') || current.is(')'))) {
                            throw new ParseException("Unexpected symbol");
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

    private static GlslValue nextTernary(GlslValue value, TokenStreamer streamer) {
        streamer.advance(); // pop ?
        GlslValue valueA = nextValue(streamer, false);
        if (!streamer.current().is(':'))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        GlslValue valueB = nextValue(streamer, false);

        return new TernaryValue(value, valueA, valueB);
    }

    private static GlslValue nextInnerValue(TokenStreamer streamer) {
        switch (streamer.current().string()) {
            case "(" -> {
                streamer.advance();
                GlslValue value = nextValue(streamer);
                streamer.advance();
                return new ParenthValue(value);
            }
            case "+" -> {
                streamer.advance();
                GlslValue value = new UnaryOperation(
                        "+",
                        nextValueNoExpr(streamer)
                );
                return value;
            }
            case "-" -> {
                streamer.advance();
                GlslValue value = new UnaryOperation(
                        "-",
                        nextValueNoExpr(streamer)
                );
                return value;
            }
            case "!" -> {
                streamer.advance();
                GlslValue value = new UnaryOperation(
                        "!",
                        nextValueNoExpr(streamer)
                );
                return value;
            }
            case "~" -> {
                streamer.advance();
                GlslValue value = new UnaryOperation(
                        "~",
                        nextValueNoExpr(streamer)
                );
                return value;
            }
            case "++" -> {
                streamer.advance();
                return new IncValue(
                        nextValueNoExpr(streamer),
                        "++"
                ).setPreIncrement(true);
            }
            case "--" -> {
                streamer.advance();
                return new IncValue(
                        nextValueNoExpr(streamer),
                        "--"
                ).setPreIncrement(true);
            }
        }

        GlslValue value = nextMonoTokenValue(streamer);

        switch (streamer.current().string()) {
            case "++" -> {
                streamer.advance();
                return new IncValue(
                        value,
                        "++"
                ).setPreIncrement(false);
            }
            case "--" -> {
                streamer.advance();
                return new IncValue(
                        value,
                        "--"
                ).setPreIncrement(false);
            }
        }

        return value;
    }

    public static GlslValue nextValueNoExpr(TokenStreamer streamer) {
//        throw new ParseException()("NYI");

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

    private static GlslValue nextValue(TokenStreamer streamer, boolean allowComma) {
//        return nextValueNoExpr(streamer);
        GlslValue value = ExpressionParser.doParse(
                streamer, () -> nextValueNoExpr(streamer)
        );

        if (streamer.current().is('?')) {
            value = nextTernary(value, streamer);
        }

        if (allowComma && streamer.current().is(',')) {
            List<GlslValue> parts = new ArrayList<>();
            parts.add(value);
            while (streamer.current().is(',')) {
                streamer.advance();
                parts.add(nextValue(streamer, true)); // allow chaining
            }
            return new CommaValue(parts); // implement CommaValue as a sequence node
        }

        return value;
    }

    private static GlslValue nextValue(TokenStreamer streamer) {
        return nextValue(streamer, true);
    }

    private static ArraySpecifier nextArraySpecifier(TokenStreamer streamer) {
        List<GlslValue> values = new ArrayList<>();
        while (streamer.current().is('[')) {
            streamer.advance();
            if (streamer.current().is(']')) {
                values.add(null);
            } else {
                values.add(nextValue(streamer));
            }
            streamer.advance();
        }
        if (values.isEmpty()) return null;
        return new ArraySpecifier(values);
    }

    private static VarSpecifier nextVarSpecifier(TokenStreamer streamer) {
        GlslToken typeToken = streamer.current();
        if (!(typeToken.is(TokenGroup.TYPE) || typeToken.is(TokenType.LITERAL))) {
            throw new ParseException("Unexpected symbol");
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

    private static GlslSegment nextMemberDef(TokenStreamer streamer, StorageQualifier qualifier, List<String> attributes) {
        while (streamer.current().is(TokenGroup.ATTRIBUTE)) {
            attributes.add(streamer.current().string());
            streamer.advance();
        }
        VarSpecifier specifier = nextVarSpecifier(streamer);
        return new GlslMemberSegment(
                qualifier, specifier
        );
    }

    private static VarSpecifier nextVarSpecifierFull(TokenStreamer streamer) {
        List<String> attributes = new ArrayList<>();
        while (streamer.current().is(TokenGroup.ATTRIBUTE)) {
            attributes.add(streamer.current().string());
            streamer.advance();
        }
        return nextVarSpecifier(streamer).setModifiers(attributes);
    }

    private static List<Member> nextMember(TokenStreamer streamer) {
		List<Member> members = new ArrayList<>();
	    
	    LayoutQualifier layout = null;
	    if (streamer.current().is(TokenType.LAYOUT)) {
			streamer.advance();
		    layout = layout(streamer);
	    }
	    VarSpecifier specifier = nextVarSpecifierFull(streamer);
	    members.add(new Member(specifier).setLayout(layout));
		
		while (streamer.current().is(',')) {
			streamer.advance();
			
			String name = streamer.current().string();
			VarSpecifier nSpec = DuplicationUtil.dupVarSpec(specifier);
			nSpec.setName(name);
			streamer.advance();
			members.add(new Member(nSpec));
		}
		
        // TODO: attribute qualifiers?
        // TODO: default values?
        return members;
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
	        for (Member member : nextMember(streamer)) {
		        segment.addMember(member);
	        }
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

    private static StorageQualifier asQualifier(TokenType type) {
        return switch (type) {
            case UNIFORM -> StorageQualifier.UNIFORM;
            case IN -> StorageQualifier.IN;
            case OUT -> StorageQualifier.OUT;
            case INOUT -> StorageQualifier.INOUT;
            case BUFFER -> StorageQualifier.BUFFER;
            case STRUCT -> StorageQualifier.STRUCT;
            case VARYING -> StorageQualifier.VARYING;
            case ATTRIBUTE -> StorageQualifier.ATTRIBUTE;
            default -> throw new ParseException("Unexpected symbol");
        };
    }

    private static GlslSegment nextStorage(TokenStreamer streamer, List<String> attributes) {
        GlslToken storage = streamer.current();
        streamer.advance();
        TokenType type = storage.type();
        StorageQualifier qualifier = asQualifier(type);

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
                    streamer, qualifier, attributes
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

        char ochar = str.charAt(1);
        if (ochar != '=')
            return false;

        char echar = str.charAt(0);
        switch (echar) {
            case '+', '-', '/', '*' -> {
                return true;
            }
            case '|', '&', '^' -> {
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
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        List<GlslStatement> vd;
        if (streamer.current().is(';'))
            vd = Collections.singletonList(new ArbitraryStatement(""));
        else vd = nextStatement(streamer);
        streamer.advance();
        if (vd.size() != 1) {
            throw new ParseException("Expected a single var def statement.");
        }

        GlslValue comparison;
        if (streamer.current().is(';'))
            comparison = new BooleanValue(true);
        else comparison = nextValue(streamer);
        streamer.advance();

        List<GlslStatement> incr;
        if (streamer.current().is(';'))
            incr = Collections.singletonList(new ArbitraryStatement(""));
        else incr = nextStatement(streamer);
        streamer.advance();
        if (incr.size() != 1) {
            throw new ParseException("Expected a single increment statement.");
        }

        ForStatement forSt = new ForStatement(vd.get(0), comparison, incr.get(0));

        if (!streamer.current().is('{')) {
            forSt.getBody().addAll(nextStatement(streamer));
            streamer.advance();
        } else {
            streamer.advance();
            nextBody(streamer, forSt.getBody()::addAll);
        }

        return forSt;
    }

    private static GlslStatement nextWhile(TokenStreamer streamer) {
        streamer.advance(); // pop "while"

        if (!streamer.current().is('('))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        GlslValue condition;
        if (streamer.current().is(')'))
            condition = new BooleanValue(true);
        else condition = nextValue(streamer);
        streamer.advance();

        WhileStatement statement = new WhileStatement(condition);
        if (!streamer.current().is('{'))
            throw new ParseException("Unexpected symbol");
        streamer.advance();
        nextBody(streamer, statement.getBody()::addAll);

        return statement;
    }

    private static final GlslValue PLACEHOLDER_TRUE = new BooleanValue(true);

    private static GlslStatement nextDo(TokenStreamer streamer) {
        streamer.advance(); // pop "do"
        DoWhileStatement statement = new DoWhileStatement(PLACEHOLDER_TRUE);

        if (!streamer.current().is('{')) {
            statement.getBody().addAll(nextStatement(streamer));
            streamer.advance();
        } else {
            streamer.advance();
            nextBody(streamer, statement.getBody()::addAll);
        }

        if (!streamer.current().is("while"))
            throw new ParseException("Unexpected symbol");
        streamer.advance();
        if (!streamer.current().is("("))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        if (!streamer.current().is(')'))
            statement.setCondition(nextValue(streamer));
        streamer.advance();

        return statement;
    }

    private static GlslStatement nextSwitch(TokenStreamer streamer) {
        streamer.advance(); // pop "switch"

        if (!streamer.current().is('('))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        GlslValue value = nextValue(streamer);
        streamer.advance();

        if (!streamer.current().is('{'))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        SwitchStatement statement = new SwitchStatement(value);

        SwitchStatement.SwitchCase switchCase = null;
        while (true) {
            popSemis(streamer);
            if (streamer.current().is('}')) {
                break;
            } else if (streamer.current().is(TokenType.CASE)) {
	            streamer.advance();
	            switchCase = new SwitchStatement.SwitchCase(nextValue(streamer));
	            if (!streamer.current().is(':'))
		            throw new ParseException("Unexpected symbol");
	            streamer.advance();
	            statement.addCase(switchCase);
            } else if (streamer.current().is(TokenType.DEFAULT)) {
	            streamer.advance();
	            if (!streamer.current().is(':'))
		            throw new ParseException("Unexpected symbol");
				streamer.advance();
	            
				switchCase = new SwitchStatement.SwitchCase(null);
	            statement.addCase(switchCase);
            } else {
                switchCase.getStatements().addAll(
                        nextStatement(streamer)
                );
            }
        }
        streamer.advance();

        return statement;
    }

    private static List<GlslStatement> nextStatement(TokenStreamer streamer) {
        // if statement type can be immediately resolved, statement is that statement type
        // elsewise, statement is var def or var assignment based upon position of =

        GlslToken token = streamer.current();

        switch (token.type()) {
            case SYMBOL -> {
                if (token.is('{')) {
                    streamer.advance();
                    BodyStatement statement = new BodyStatement(new ArrayList<>());
                    nextBody(streamer, statement.getBody()::addAll);
                    return Collections.singletonList(statement);
                }
                throw new ParseException("Unexpected symbol");
            }
            case IF -> {
                return Collections.singletonList(nextIf(streamer));
            }
            case SWITCH -> {
                return Collections.singletonList(nextSwitch(streamer));
            }
            case FOR -> {
                return Collections.singletonList(nextFor(streamer));
            }
            case DO -> {
                return Collections.singletonList(nextDo(streamer));
            }
            case WHILE -> {
                return Collections.singletonList(nextWhile(streamer));
            }
            case CONTINUE -> {
                streamer.advance();
                return Collections.singletonList(ContinueStatement.INSTANCE);
            }
            case BREAK -> {
                streamer.advance();
                return Collections.singletonList(BreakStatement.INSTANCE);
            }
            case RETURN -> {
                streamer.advance();
                token = streamer.current();
                if (token.is(';')) {
                    return Collections.singletonList(new ReturnStatement());
                } else {
                    return Collections.singletonList(new ReturnStatement(nextValue(streamer)));
                }
            }
            default -> {
                int index = streamer.index();
                GlslValue value = nextValueNoExpr(streamer);
                if (
                        value.getValueType() == ValueType.ASSIGNMENT
                ) {
                    return Collections.singletonList(new AssignmentStatement(
                            ((AssignmentValue) value).getRef(),
                            ((AssignmentValue) value).getValue()
                    ).setAuxiliaryOp(((AssignmentValue) value).getAuxiliaryOp()));
                } else if (value.getValueType() == ValueType.FUNCTION) {
                    return Collections.singletonList(new MethodCallStatement(
                            (MethodCallValue) value
                    ));
                } else if (value.getValueType() == ValueType.INC) {
                    return Collections.singletonList(new IncStatement(
                            ((IncValue) value).getRef(),
                            ((IncValue) value).getOperation()
                    ).setPreIncrement(((IncValue) value).isPreIncrement()));
                }
                streamer.setIndex(index);

                List<GlslStatement> statements = new ArrayList<>();
                {
                    VarSpecifier varSpec = nextVarSpecifierFull(streamer);

                    GlslToken token1 = streamer.current();

                    if (token1.is('=') || token1.is(TokenType.OPERATOR) || token1.is(',')) {
                        String str = token1.string();
						
	                    VarDefStatement statement = new VarDefStatement(varSpec);
						
	                    if (str.equals("=")) {
		                    streamer.advance();
		                    statement.setValue(nextValue(streamer, false));
	                    }
                        statements.add(statement);

                        while (streamer.current().is(',')) {
                            streamer.advance();
                            String name = streamer.current().string();
                            VarSpecifier nSpec = DuplicationUtil.dupVarSpec(varSpec);
                            nSpec.setName(name);
                            streamer.advance();

                            token1 = streamer.current();

							GlslValue vv;
                            if (token1.is('=') || token1.is(TokenType.OPERATOR)) {
                                str = token1.string();
                                if (!str.equals("=")) {
                                    throw new ParseException("Unexpected symbol");
                                }

                                streamer.advance();
                                vv = nextValue(streamer, false);
                            } else {
                                vv = null;
                            }
                            statement = new VarDefStatement(nSpec).setValue(vv);
                            statements.add(statement);
                        }

                        if (!streamer.current().is(';')) {
                            throw new ParseException("Unexpected symbol");
                        }

                        return statements;
                    } else if (token1.is(';')) {
                        return Collections.singletonList(new VarDefStatement(varSpec));
                    } else if (streamer.current().is(',')) {
                        statements.add(new VarDefStatement(varSpec));
                        while (streamer.current().is(',')) {
                            streamer.advance();
                            varSpec = DuplicationUtil.dupVarSpec(varSpec);
                            varSpec.setName(streamer.current().string());
                            streamer.advance();
                            statements.add(new VarDefStatement(varSpec));
                        }
                        return statements;
                    } else {
                        throw new ParseException("Unexpected symbol");
                    }
                }
            }
        }
    }

    private static GlslStatement nextIf(TokenStreamer streamer) {
        streamer.advance(); // pop "if"
        if (!streamer.current().is('('))
            throw new ParseException("Unexpected symbol");
        streamer.advance();

        GlslValue condition;
        if (streamer.current().is(')'))
            condition = new BooleanValue(true);
        else condition = nextValue(streamer);
        streamer.advance();

        ConditionalStatement statement = new ConditionalStatement();
        ConditionalStatement.ConditionalCode conditionalCode = new ConditionalStatement.ConditionalCode(
                condition
        );

        if (!streamer.current().is("{")) {
            conditionalCode.getStatements().addAll(nextStatement(streamer));
            streamer.advance();
        } else {
            streamer.advance();
            nextBody(streamer, conditionalCode.getStatements()::addAll);
        }

        statement.addStep(conditionalCode);

        while (streamer.current().is("else")) {
            streamer.advance();

            if (streamer.current().is("if")) {
                streamer.advance();
                if (!streamer.current().is('('))
                    throw new ParseException("Unexpected symbol");
                streamer.advance();

                if (streamer.current().is(')'))
                    condition = new BooleanValue(true);
                else condition = nextValue(streamer);
                streamer.advance();

                conditionalCode = new ConditionalStatement.ConditionalCode(
                        condition
                );
            } else {
                conditionalCode = new ConditionalStatement.ConditionalCode(
                        null
                );
            }
            if (streamer.current().is('{')) {
                streamer.advance();
                nextBody(streamer, conditionalCode.getStatements()::addAll);
            } else {
                conditionalCode.getStatements().addAll(nextStatement(streamer));
                streamer.advance();
            }

            statement.addStep(conditionalCode);
        }

        return statement;
    }

    private static void nextBody(TokenStreamer streamer, Consumer<List<GlslStatement>> addStatement) {
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
            throw new ParseException("Unexpected symbol");
        }

        GlslCodeSegment codeSegment = new GlslCodeSegment(
                specifier.getType(),
                specifier.getName()
        );
        streamer.advance();
        while (!streamer.current().is(')')) {
            List<String> attributes = new ArrayList<>();
            while (streamer.current().is(TokenGroup.ATTRIBUTE)) {
                attributes.add(streamer.current().string());
                streamer.advance();
            }

            StorageQualifier qualifier = null;
            if (streamer.current().is(TokenGroup.STORAGE_TYPE)) {
                qualifier = asQualifier(streamer.current().type());
                streamer.advance();
            }

            VarSpecifier varSpec = nextVarSpecifier(streamer).setModifiers(attributes);
            codeSegment.addParam(new Parameter(varSpec).setQualifier(qualifier));
            if (streamer.current().is(','))
                streamer.advance();
        }
        streamer.advance();

        if (!streamer.current().is('{')) {
            throw new ParseException("Unexpected symbol");
        }
        streamer.advance();

        nextBody(streamer, codeSegment.getStatements()::addAll);

        return codeSegment;
    }

    private static List<GlslSegment> nextSegment(TokenStreamer streamer) {
        LayoutQualifier layoutQualif = null;
        List<String> attributes = new ArrayList<>();
        while (true) {
            GlslToken current = streamer.current();

//            GlslSegment segment = trySegment(streamer);

            if (current.is(TokenType.EXTENSION_DIRECTIVE)) {
                streamer.advance();
                String dir = streamer.current().string();
                streamer.advance();
                return Collections.singletonList(new ExtensionSegment(dir));
            } else if (current.is(TokenGroup.ATTRIBUTE)) {
                attributes.add(current.string());
                streamer.advance();
            } else if (current.is(TokenGroup.STORAGE_TYPE)) {
                List<GlslSegment> result = new ArrayList<>();

                GlslSegment segment = nextStorage(streamer, attributes);
                switch (segment.getSegmentType()) {
                    case BLOCK_DEF:
                        ((GlslBlockSegment) segment).setLayout(layoutQualif);
                        result.add(segment);
                        break;
                    case MEMBER_DEF:
                        ((GlslMemberSegment) segment).setModifiers(attributes);
                        ((GlslMemberSegment) segment).setLayout(layoutQualif);

                        while (true) {
                            GlslMemberSegment member = ((GlslMemberSegment) segment).template();

                            if (streamer.current().is('=')) {
                                streamer.advance();
                                ((GlslMemberSegment) segment).setValue(nextValue(streamer));
                            }

                            result.add(member);

                            if (!streamer.current().is(','))
                                break;
                            else {
//                                System.out.println("h");
                                streamer.advance();
                                ((GlslMemberSegment) segment).getMember().getVar().setName(
                                        streamer.current().string()
                                );
                                streamer.advance();
                            }
                        }

                        break;

                    default:
                        throw new ParseException("wat");
                }
                return result;
            } else if (current.is(TokenType.LAYOUT)) {
                streamer.advance();
                layoutQualif = layout(streamer);
            } else {
                VarSpecifier specifier = nextVarSpecifier(streamer);
                specifier.setModifiers(attributes);

                if (streamer.current().is('(')) {
                    return Collections.singletonList(nextFunction(specifier, streamer));
                } else if (streamer.current().is(';')) {
                    // TODO: support multi-def here
                    return Collections.singletonList(new GlslVarSegment(
                            specifier
                    ));
                } else if (streamer.current().is('=') || streamer.current().is(',')) {
	                List<GlslSegment> segments = new ArrayList<>();
	                GlslValue value = null;
					if (streamer.current().is('=')) {
						streamer.advance();
						value = nextValue(streamer);
					}
					
	                segments.add(new GlslVarSegment(
			                specifier
	                ).setValue(value));

                    while (streamer.current().is(',')) {
                        streamer.advance();

                        VarSpecifier nSpec = new VarSpecifier(
                                specifier.getType(),
                                streamer.current().string()
                        ).setArray(specifier.getArray());
                        streamer.advance();

                        if (streamer.current().is('=')) {
                            streamer.advance();
                            value = nextValue(streamer);
                        } else {
                            value = null;
                        }

                        segments.add(new GlslVarSegment(nSpec).setValue(value));
                    }

                    if (!streamer.current().is(';')) {
                        throw new ParseException("Unexpected symbol");
                    }

                    return segments;
                } else {
                    throw new ParseException("Unexpected symbol");
                }
//                throw new ParseException()("Unexpected symbol");
            }
        }
    }

    private static void parseTo(TokenStreamer streamer, GlslFile file) {
        popSemis(streamer);
        while (!streamer.isDone()) {
            for (GlslSegment segment : nextSegment(streamer)) {
                file.addSegment(segment);
            }
            popSemis(streamer);
            streamer.clearBuffer();
        }
    }

    public static GlslFile toTree(TokenStreamer streamer) {
        GlslToken directive = streamer.current();
        if (!directive.is(TokenType.VERSION_DIRECTIVE)) {
            throw new ParseException("Unspecified version.");
        }
        streamer.advance();
        GlslToken versionNumber = streamer.current();
        streamer.advance();

        GlslFile file = new GlslFile(versionNumber.string());

        parseTo(streamer, file);

        return file;
    }
}
