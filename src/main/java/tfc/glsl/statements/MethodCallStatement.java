package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;
import tfc.glsl.value.MethodCallValue;

public class MethodCallStatement extends GlslStatement {
    MethodCallValue value;

    public MethodCallStatement(MethodCallValue value) {
        super(StatementType.METHOD_CALL);
        this.value = value;
    }

    public MethodCallValue getValue() {
        return value;
    }

    public MethodCallStatement setValue(MethodCallValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel));
        value.asString(builder);
        builder.append(";");
    }

    @Override
    public GlslStatement duplicate() {
        return new MethodCallStatement((MethodCallValue) value.duplicate());
    }
}
