package tfc.glsl.parse;

public enum TokenType {
    VERSION_DIRECTIVE("#version"),
    EXTENSION_DIRECTIVE("#extension"),

    IN("in"), OUT("out"), INOUT("inout"),
    VARYING("varying"), UNIFORM("uniform"),
    STRUCT("struct"), BUFFER("buffer"),
    ATTRIBUTE("attribute"),

    VOID("void"), INT("int"),
    FLOAT("float"), BOOL("bool"),

    RESTRICT("restrict"),
    CONST("const"),
    READONLY("readonly"),
    WRITEONLY("writeonly"),

    FLAT("flat"), NO_PERSPECTIVE("noperspective"),
    CENTROID("centroid"), SMOOTH("smooth"),

    LAYOUT("layout"),

    IF("if"), ELSE("else"),
    FOR("for"), WHILE("while"),
    DO("do"), SWITCH("switch"),
    CASE("case"), DEFAULT("default"),

    BREAK("break"), CONTINUE("continue"),
    RETURN("return"),

    EOS(null),
    LITERAL(null),
    SYMBOL(null),
    OPERATOR(null);

    String text;
    GlslToken singletonToken;

    TokenType(String text) {
        this.text = text;
        singletonToken = new GlslToken(this);
    }
}
