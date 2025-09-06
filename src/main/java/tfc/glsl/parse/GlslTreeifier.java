package tfc.glsl.parse;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.meta.*;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.segments.GlslBlockSegment;
import tfc.glsl.segments.GlslCodeSegment;
import tfc.glsl.segments.GlslMemberSegment;

import java.util.ArrayList;
import java.util.List;

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

    private static GlslValue nextValue() {
        throw new RuntimeException("NYI");
    }

    private static ArraySpecifier nextArraySpecifier(TokenStreamer streamer) {
        List<GlslValue> values = new ArrayList<>();
        while (streamer.current().is('[')) {
            streamer.advance();
            values.add(nextValue());
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

    private static GlslStatement nextStatement(TokenStreamer streamer) {
        // if statement type can be immediately resolved, statement is that statement type
        // elsewise, statement is var def or var assignment based upon position of =


    }

    private static GlslSegment nextFunction(VarSpecifier specifier, TokenStreamer streamer) {
        if (!streamer.current().is('(')) {
            throw new RuntimeException("Unexpected symbol");
        }

        GlslCodeSegment codeSegment = new GlslCodeSegment(
                specifier.getType(),
                specifier.getName()
        );
        while (!streamer.current().is(')')) {
            VarSpecifier varSpec = nextVarSpecifier(streamer);
            codeSegment.addParam(new Parameter(varSpec));
        }
        streamer.advance();

        if (!streamer.current().is('{')) {
            throw new RuntimeException("Unexpected symbol");
        }

        while (!streamer.current().is('}')) {
            codeSegment.addStatement(nextStatement(streamer));
        }

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
