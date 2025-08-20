package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.SegmentType;
import tfc.glsl.meta.LayoutQualifier;
import tfc.glsl.meta.Member;
import tfc.glsl.meta.VarSpecifier;
import tfc.glsl.meta.enums.StorageQualifier;

public class GlslMemberSegment extends GlslSegment {
    @NotNull StorageQualifier qualifier;
    @NotNull Member member;

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

    @Override
    public void asString(StringBuilder builder) {
        if (member.getLayout() != null) {
            builder.append(member.getLayout()).append(" ").append(qualifier.getTypeName()).append(" ").append(member.getVar()).append(";");
            return;
        }
        builder.append(qualifier.getTypeName()).append(" ").append(member.getVar()).append(";");
    }
}
