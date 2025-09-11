package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class MethodCallValue extends GlslValue {
    @NotNull GlslValue name;
    @Nullable GlslValue[] params;

    public MethodCallValue(@NotNull GlslValue object, @Nullable GlslValue... params) {
        super(ValueType.FUNCTION);
        this.name = object;
        this.params = params;
    }

    public GlslValue getName() {
        return name;
    }

    public MethodCallValue setName(GlslValue name) {
        this.name = name;
        return this;
    }

    public GlslValue[] getParams() {
        return params;
    }

    public MethodCallValue setParams(GlslValue[] params) {
        this.params = params;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        name.asString(builder);
        builder.append("(");

        if (params != null) {
            int len = params.length;
            for (int i = 0; i < len; i++) {
                params[i].asString(builder);
                if (i != len - 1) {
                    builder.append(", ");
                }
            }
        }
        builder.append(")");
    }

    @Override
    public boolean constResolvable() {
        return true;
    }
}
