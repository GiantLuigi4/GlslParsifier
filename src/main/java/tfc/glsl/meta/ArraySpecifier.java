package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;

import java.util.ArrayList;
import java.util.List;

public class ArraySpecifier {
    @NotNull List<GlslValue> values;

    public ArraySpecifier(@NotNull List<GlslValue> values) {
        this.values = values;
    }

    public void asString(StringBuilder builder) {
        for (GlslValue value : values) {
            builder.append('[');
            if (value != null) {
                value.asString(builder);
            }
            builder.append(']');
        }
    }

    @Override
    public String toString() {
        throw new RuntimeException();
    }

    public ArraySpecifier template() {
        List<GlslValue> cpy = new ArrayList<>(values);
        return new ArraySpecifier(cpy);
    }
}
