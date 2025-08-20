package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.SegmentType;
import tfc.glsl.meta.VarSpecifier;

public class GlslVarSegment extends GlslSegment {
    @Nullable String qualifier;
    @NotNull VarSpecifier var;
    @Nullable GlslValue value;

    public GlslVarSegment(@NotNull VarSpecifier var) {
        super(SegmentType.VAR_DEF);
        this.var = var;
    }

    public String getQualifier() {
        return qualifier;
    }

    public GlslVarSegment setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public VarSpecifier getVar() {
        return var;
    }

    public GlslVarSegment setVar(VarSpecifier var) {
        this.var = var;
        return this;
    }

    public GlslValue getValue() {
        return value;
    }

    public GlslVarSegment setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public String asString() {
        if (value != null || qualifier != null) {
            StringBuilder builder = new StringBuilder();
            if (qualifier != null)
                builder.append(qualifier).append(" ");
            builder.append(var);
            if (value != null)
                builder.append(" = ").append(value);
            builder.append(";");
            return builder.toString();
        }
        return var + ";";
    }
}
