package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;

import java.util.List;

public class ArraySpecifier {
    @NotNull List<GlslValue> values;

    public ArraySpecifier(@NotNull List<GlslValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
