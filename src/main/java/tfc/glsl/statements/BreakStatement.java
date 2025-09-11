package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;

public class BreakStatement extends GlslStatement {
    public static final BreakStatement INSTANCE = new BreakStatement();

    public BreakStatement() {
        super(StatementType.BREAK);
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("break;");
    }
}
