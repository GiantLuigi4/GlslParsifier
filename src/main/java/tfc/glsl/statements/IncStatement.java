package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;

public class IncStatement extends GlslStatement  {
    @NotNull  GlslValue ref;
    @NotNull String operation;

    public IncStatement(@NotNull GlslValue ref, @NotNull String operation) {
        super(StatementType.INC);
        this.ref = ref;
        this.operation = operation;
    }

    public GlslValue getRef() {
        return ref;
    }

    public IncStatement setRef(GlslValue ref) {
        this.ref = ref;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public IncStatement setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append(ref).append(operation).append(";");
    }
}
