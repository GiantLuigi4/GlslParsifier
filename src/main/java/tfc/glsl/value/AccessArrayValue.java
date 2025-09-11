package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class AccessArrayValue extends GlslValue {
    @NotNull GlslValue object;
    @NotNull GlslValue access;

    public AccessArrayValue(@NotNull GlslValue object, @NotNull GlslValue access) {
        super(ValueType.ACCESS_ARRAY);
        this.object = object;
        this.access = access;
    }

    public GlslValue getObject() {
        return object;
    }

    public AccessArrayValue setObject(GlslValue object) {
        this.object = object;
        return this;
    }

    public GlslValue getAccess() {
        return access;
    }

    public AccessArrayValue setAccess(GlslValue access) {
        this.access = access;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
//        builder.append(object).append("[").append(access).append("]");
        object.asString(builder);
        builder.append('[');
        access.asString(builder);
        builder.append(']');
    }

    @Override
    public boolean constResolvable() {
        return true;
    }
}
