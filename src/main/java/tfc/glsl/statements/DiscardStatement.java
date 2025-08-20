package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;

public class DiscardStatement extends GlslStatement {
    public static final DiscardStatement INSTANCE = new DiscardStatement();

    public DiscardStatement() {
        super(StatementType.DISCARD);
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("discard;");
    }
}
