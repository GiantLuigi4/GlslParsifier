package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;
import tfc.glsl.meta.VarSpecifier;

public class ReturnStatement extends GlslStatement {
    @Nullable GlslValue value;

    public ReturnStatement(@Nullable GlslValue value) {
        super(StatementType.RETURN);
        this.value = value;
    }

    public ReturnStatement() {
        super(StatementType.RETURN);
    }

    public GlslValue getValue() {
        return value;
    }

    public ReturnStatement setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        if (value != null) {
            builder.append("\t".repeat(indentLevel)).append("return ");
            value.asString(builder);
            builder.append(";");
            return;
        }
        builder.append("\t".repeat(indentLevel)).append("return;");
    }
}
