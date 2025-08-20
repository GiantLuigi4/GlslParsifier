package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;

public class ContinueStatement extends GlslStatement {
    public static final ContinueStatement INSTANCE = new ContinueStatement();

    public ContinueStatement() {
        super(StatementType.CONTINUE);
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("continue;");
    }
}
