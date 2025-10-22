package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class BooleanValue extends GlslValue {
    @NotNull Boolean value;

    public BooleanValue(@NotNull Boolean text) {
        super(ValueType.BOOLEAN);
        this.value = text;
    }

    public Boolean getValue() {
        return value;
    }

    public BooleanValue setValue(Boolean value) {
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

    @Override
    public GlslValue duplicate() {
        return new BooleanValue(value);
    }
}
