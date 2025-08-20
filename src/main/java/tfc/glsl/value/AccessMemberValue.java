package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class AccessMemberValue extends GlslValue {
    @NotNull GlslValue object;
    @NotNull GlslValue member;

    public AccessMemberValue(@NotNull GlslValue object, @NotNull GlslValue member) {
        super(ValueType.ACCESS_MEMBER);
        this.object = object;
        this.member = member;
    }

    public GlslValue getObject() {
        return object;
    }

    public AccessMemberValue setObject(GlslValue object) {
        this.object = object;
        return this;
    }

    public GlslValue getMember() {
        return member;
    }

    public AccessMemberValue setMember(GlslValue member) {
        this.member = member;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append(object).append(".").append(member);
    }
}
