package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class ParenthValue extends GlslValue {
    @NotNull GlslValue value;

    public ParenthValue(@NotNull GlslValue value) {
        super(ValueType.PARENTH);
        this.value = value;
    }

    public GlslValue getValue() {
        return value;
    }

    public ParenthValue setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append("(").append(value).append(")");
    }
}
