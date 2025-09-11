package tfc.glsl.base;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GlslSegment {
    private final SegmentType type;

    public GlslSegment(SegmentType type) {
        this.type = type;
    }

    public SegmentType getSegmentType() {
        return type;
    }

    public abstract void asString(StringBuilder builder);

    public String asString() {
        StringBuilder builder = new StringBuilder();
        asString(builder);
        return builder.toString();
    }

    @Override
    public String toString() {
//        return asString();
        throw new RuntimeException();
    }
}
