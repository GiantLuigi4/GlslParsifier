package tfc.glsl.visitor;

import tfc.glsl.base.GlslSegment;
import tfc.glsl.segments.*;

public interface GlslSegmentVisitor {
    void visitMember(GlslMemberSegment segment);

    void visitVar(GlslVarSegment segment);

    void visitCode(GlslCodeSegment segment);

    void visitBlock(GlslBlockSegment segment);

    void visitArbitrary(ArbitrarySegment segment);

    default void visitSegment(GlslSegment segment) {
        switch (segment.getSegmentType()) {
            case MEMBER_DEF -> visitMember((GlslMemberSegment) segment);
            case VAR_DEF -> visitVar((GlslVarSegment) segment);
            case CODE -> visitCode((GlslCodeSegment) segment);
            case BLOCK_DEF -> visitBlock((GlslBlockSegment) segment);
            case ARBITRARY -> visitArbitrary((ArbitrarySegment) segment);
        }
    }
}
