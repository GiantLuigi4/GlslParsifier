package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.SegmentType;
import tfc.glsl.meta.LayoutQualifier;
import tfc.glsl.meta.Member;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.meta.enums.StorageQualifier;

import java.util.ArrayList;
import java.util.List;

public class GlslMemberSegment extends GlslSegment {
    @Nullable List<String> modifiers;
    @NotNull StorageQualifier qualifier;
    @NotNull Member member;
    @Nullable GlslValue value;

    public GlslMemberSegment(@NotNull StorageQualifier qualifier, @NotNull Member member) {
        super(SegmentType.MEMBER_DEF);
        this.qualifier = qualifier;
        this.member = member;
    }

    public GlslMemberSegment(@NotNull StorageQualifier qualifier, @NotNull VarSpecifier varDef) {
        super(SegmentType.MEMBER_DEF);
        this.qualifier = qualifier;
        this.member = new Member(varDef);
    }

    public StorageQualifier getQualifier() {
        return qualifier;
    }

    public Member getMember() {
        return member;
    }

    public GlslMemberSegment setQualifier(@NotNull StorageQualifier qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public GlslMemberSegment setMember(@NotNull Member member) {
        this.member = member;
        return this;
    }

    public GlslMemberSegment setLayout(LayoutQualifier layout) {
        member.setLayout(layout);
        return this;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public GlslMemberSegment setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public GlslValue getValue() {
        return value;
    }

    public GlslMemberSegment setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        StringBuilder varModifiers = new StringBuilder();
        if (modifiers != null) {
            for (String modifier : modifiers) {
                varModifiers.append(modifier).append(" ");
            }
        }

        if (member.getLayout() != null) {
            builder.append(member.getLayout()).append(" ").append(varModifiers).append(qualifier.getTypeName()).append(" ").append(member.getVar());
            if (value != null) {
                builder.append(" = ");
                value.asString(builder);
            }
            builder.append(";");
            return;
        }
        builder.append(varModifiers).append(qualifier.getTypeName()).append(" ").append(member.getVar());
        if (value != null) {
            builder.append(" = ");
            value.asString(builder);
        }
        builder.append(";");
    }

    public GlslMemberSegment template() {
        GlslMemberSegment memberSegment = new GlslMemberSegment(
                qualifier, member.template()
        );
        if (modifiers != null) {
            List<String> cpy = new ArrayList<>(modifiers);
            memberSegment.setModifiers(cpy);
        }
        memberSegment.setValue(value);
        return memberSegment;
    }
}
