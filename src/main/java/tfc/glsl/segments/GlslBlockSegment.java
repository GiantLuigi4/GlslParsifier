package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.SegmentType;
import tfc.glsl.meta.enums.StorageQualifier;
import tfc.glsl.meta.LayoutQualifier;
import tfc.glsl.meta.Member;

import java.util.ArrayList;
import java.util.List;

public class GlslBlockSegment extends GlslSegment {
    @Nullable
    private LayoutQualifier layout;
    @Nullable
    private String[] qualifiers;
    @NotNull
    private StorageQualifier type;
    @Nullable
    private String name;
    @NotNull
    List<Member> members = new ArrayList<>();
    @Nullable
    private String instance;

    public GlslBlockSegment(@NotNull StorageQualifier type) {
        super(SegmentType.BLOCK_DEF);
        this.type = type;
    }

    public GlslBlockSegment setLayout(LayoutQualifier layout) {
        this.layout = layout;
        return this;
    }

    public GlslBlockSegment setQualifiers(String[] qualifiers) {
        this.qualifiers = qualifiers;
        return this;
    }

    public GlslBlockSegment setBlockType(@NotNull StorageQualifier type) {
        this.type = type;
        return this;
    }

    public GlslBlockSegment setName(String name) {
        this.name = name;
        return this;
    }

    public GlslBlockSegment setInstance(String instance) {
        this.instance = instance;
        return this;
    }

    public GlslBlockSegment addMember(Member member) {
        members.add(member);
        return this;
    }

    public LayoutQualifier getLayout() {
        return layout;
    }

    public String[] getQualifiers() {
        return qualifiers;
    }

    public StorageQualifier getBlockType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public void asString(StringBuilder builder) {
        if (layout != null) builder.append(layout).append(" ");
        if (qualifiers != null) {
            for (String qualifier : qualifiers) {
                builder.append(qualifier).append(" ");
            }
        }
        builder.append(type.getTypeName()).append(" ");
        if (name != null) builder.append(name).append(" ");
        builder.append("{\n");
        for (Member member : members) {
            builder.append("\t").append(member).append(";\n");
        }
        builder.append("}");
        if (instance != null) builder.append(" ").append(instance);
        builder.append(";");
    }
}
