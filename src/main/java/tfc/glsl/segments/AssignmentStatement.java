package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;

public class AssignmentStatement extends GlslStatement {
    @NotNull GlslValue ref;
    @Nullable String auxiliaryOp;
    @NotNull GlslValue value;

    public AssignmentStatement(@NotNull GlslValue ref, @NotNull GlslValue value) {
        super(StatementType.ASSIGNMENT);
        this.ref = ref;
        this.value = value;
    }

    public AssignmentStatement(@NotNull GlslValue ref, @Nullable String auxiliaryOp, @NotNull GlslValue value) {
        super(StatementType.ASSIGNMENT);
        this.ref = ref;
        this.auxiliaryOp = auxiliaryOp;
        this.value = value;
    }

    public GlslValue getRef() {
        return ref;
    }

    public AssignmentStatement setRef(GlslValue ref) {
        this.ref = ref;
        return this;
    }

    public String getAuxiliaryOp() {
        return auxiliaryOp;
    }

    public AssignmentStatement setAuxiliaryOp(String auxiliaryOp) {
        this.auxiliaryOp = auxiliaryOp;
        return this;
    }

    public GlslValue getValue() {
        return value;
    }

    public AssignmentStatement setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        if (auxiliaryOp != null) {
            builder.append("\t".repeat(indentLevel)).append(ref).append(" ").append(auxiliaryOp).append("= ").append(value).append(";");
            return;
        }
        builder.append("\t".repeat(indentLevel)).append(ref).append(" = ").append(value).append(";");
    }
}
