package tfc.glsl.base;

public abstract class GlslStatement {
    private final StatementType type;

    public GlslStatement(StatementType type) {
        this.type = type;
    }

    public StatementType getStatementType() {
        return type;
    }

    public abstract void asString(StringBuilder builder, int indentLevel);

    public String asString() {
        StringBuilder builder = new StringBuilder();
        asString(builder, 0);
        return builder.toString();
    }
}
