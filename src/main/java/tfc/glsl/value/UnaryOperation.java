package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class UnaryOperation extends GlslValue {
    @NotNull GlslValue value;
    @NotNull String op;

    public UnaryOperation(@NotNull String op, @NotNull GlslValue value) {
        super(ValueType.UNARY_OPERATION);
        this.value = value;
        this.op = op;
    }

    @Override
    public void asString(StringBuilder builder) {
        if (
                value.getValueType() == ValueType.OPERATION ||
                        value.getValueType() == ValueType.ASSIGNMENT ||
                        value.getValueType() == ValueType.REFERENCE ||
                        value.getValueType() == ValueType.FUNCTION
        ) {
            builder.append(op).append("(").append(value).append(")");
            return;
        }
        builder.append(op).append(value);
    }
}
