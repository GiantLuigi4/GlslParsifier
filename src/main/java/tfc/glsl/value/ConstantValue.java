package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class ConstantValue extends GlslValue {
    @NotNull Number value;

    public ConstantValue(@NotNull Number text) {
        super(ValueType.CONSTANT);
        this.value = text;
    }

    public Number getValue() {
        return value;
    }

    public ConstantValue setValue(Number value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append(value);
    }

    @Override
    public boolean constResolvable() {
        return true;
    }
}
