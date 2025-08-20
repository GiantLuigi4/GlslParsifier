package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.SegmentType;
import tfc.glsl.meta.Parameter;

import java.util.ArrayList;
import java.util.List;

public class GlslCodeSegment extends GlslSegment {
    List<String> qualifiers = new ArrayList<>();
    @NotNull String type;
    String arraySpec;
    @NotNull String name;
    List<Parameter> params = new ArrayList<>();
    List<GlslStatement> statements = new ArrayList<>();

    public GlslCodeSegment(@NotNull String type, @NotNull String name) {
        super(SegmentType.CODE);
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public GlslCodeSegment setType(String type) {
        this.type = type;
        return this;
    }

    public String getArraySpec() {
        return arraySpec;
    }

    public GlslCodeSegment setArraySpec(String arraySpec) {
        this.arraySpec = arraySpec;
        return this;
    }

    public String getName() {
        return name;
    }

    public GlslCodeSegment setName(String name) {
        this.name = name;
        return this;
    }

    public GlslCodeSegment addQualifier(String qualifier) {
        qualifiers.add(qualifier);
        return this;
    }

    public GlslCodeSegment addParam(Parameter parameter) {
        params.add(parameter);
        return this;
    }

    public GlslCodeSegment addStatement(GlslStatement statement) {
        statements.add(statement);
        return this;
    }

    public List<Parameter> getParams() {
        return params;
    }

    @Override
    public void asString(StringBuilder builder) {
        for (String qualifier : qualifiers) {
            builder.append(qualifier).append(" ");
        }
        builder.append(type);
        if (arraySpec != null) builder.append(arraySpec);
        builder.append(" ");
        builder.append(name).append("(");

        int len = params.size();
        for (int i = 0; i < params.size(); i++) {
            builder.append(params.get(i));
            if (i != len - 1) {
                builder.append(", ");
            }
        }

        builder.append(") {\n");
        for (GlslStatement statement : statements) {
            statement.asString(builder, 1);
            builder.append("\n");
        }
        builder.append("}");
    }
}
