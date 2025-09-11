package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;
import tfc.glsl.base.ValueType;

public class IncValue extends GlslValue {
    @NotNull GlslValue ref;
    @NotNull String operation;
    boolean preIncrement = false;

    public IncValue(@NotNull GlslValue ref, @NotNull String operation) {
        super(ValueType.INC);
        this.ref = ref;
        this.operation = operation;
    }

    public boolean isPreIncrement() {
        return preIncrement;
    }

    public IncValue setPreIncrement(boolean preIncrement) {
        this.preIncrement = preIncrement;
        return this;
    }

    public GlslValue getRef() {
        return ref;
    }

    public IncValue setRef(GlslValue ref) {
        this.ref = ref;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public IncValue setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        if (preIncrement) {
            builder.append(operation);
            ref.asString(builder);
        } else {
            ref.asString(builder);
            builder.append(operation);
        }
    }
}
