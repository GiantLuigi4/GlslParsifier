package tfc.glsl.base;

public abstract class GlslSegment {
    private final SegmentType type;

    public GlslSegment(SegmentType type) {
        this.type = type;
    }

    public SegmentType getSegmentType() {
        return type;
    }

    public abstract String asString();

    @Override
    public String toString() {
        return asString();
    }
}
