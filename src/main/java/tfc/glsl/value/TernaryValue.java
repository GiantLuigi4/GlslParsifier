package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class TernaryValue extends GlslValue {
    @NotNull GlslValue condition, valueA, valueB;

    public TernaryValue(@NotNull GlslValue condition, @NotNull GlslValue valueA, @NotNull GlslValue valueB) {
        super(ValueType.TERNARY);

        this.condition = condition;
        this.valueA = valueA;
        this.valueB = valueB;
    }

    @Override
    public void asString(StringBuilder builder) {
        boolean constRes = condition.constResolvable() && valueA.constResolvable() && valueB.constResolvable();

        if (!constRes)
            builder.append("(");
        condition.asString(builder);
        builder.append(" ? ");
        valueA.asString(builder);
        builder.append(" : ");
        valueB.asString(builder);
        if (!constRes)
            builder.append(")");
    }

    @Override
    public boolean constResolvable() {
        return false;
    }
}
