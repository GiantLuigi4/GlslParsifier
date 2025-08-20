package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;
import tfc.glsl.meta.VarSpecifier;

public class VarDefStatement extends GlslStatement {
    @NotNull VarSpecifier var;
    @Nullable GlslValue value;

    public VarDefStatement(@NotNull VarSpecifier var) {
        super(StatementType.VAR_DEF);
        this.var = var;
    }

    public VarSpecifier getVar() {
        return var;
    }

    public VarDefStatement setVar(VarSpecifier var) {
        this.var = var;
        return this;
    }

    public GlslValue getValue() {
        return value;
    }

    public VarDefStatement setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel));
        builder.append(var);
        if (value != null) {
            builder.append(" = ");
            value.asString(builder);
        }
        builder.append(";");
    }
}
