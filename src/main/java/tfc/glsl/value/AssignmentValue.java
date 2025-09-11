package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class AssignmentValue extends GlslValue {
    @NotNull GlslValue ref;
    @Nullable String auxiliaryOp;
    @NotNull GlslValue value;

    public AssignmentValue(@NotNull GlslValue ref, @NotNull GlslValue value) {
        super(ValueType.ASSIGNMENT);
        this.ref = ref;
        this.value = value;
    }

    public AssignmentValue(@NotNull GlslValue ref, @Nullable String auxiliaryOp, @NotNull GlslValue value) {
        super(ValueType.ASSIGNMENT);
        this.ref = ref;
        this.auxiliaryOp = auxiliaryOp;
        this.value = value;
    }

    public GlslValue getRef() {
        return ref;
    }

    public AssignmentValue setRef(GlslValue ref) {
        this.ref = ref;
        return this;
    }

    public String getAuxiliaryOp() {
        return auxiliaryOp;
    }

    public AssignmentValue setAuxiliaryOp(String auxiliaryOp) {
        this.auxiliaryOp = auxiliaryOp;
        return this;
    }

    public GlslValue getValue() {
        return value;
    }

    public AssignmentValue setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        if (auxiliaryOp != null) {
            ref.asString(builder);
            builder.append(" ").append(auxiliaryOp).append("= ");
            value.asString(builder);
            return;
        }
//        builder.append(ref).append(" = ").append(value);
        ref.asString(builder);
        builder.append(" = ");
        value.asString(builder);
    }

    @Override
    public boolean constResolvable() {
        return false;
    }
}
