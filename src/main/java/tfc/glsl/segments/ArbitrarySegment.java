package tfc.glsl.segments;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.SegmentType;

public class ArbitrarySegment extends GlslSegment {
    @NotNull String text;

    public ArbitrarySegment(@NotNull String text) {
        super(SegmentType.ARBITRARY);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ArbitrarySegment setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String asString() {
        return text;
    }
}
