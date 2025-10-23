package tfc.glsl.segments;

import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.SegmentType;

public class ExtensionSegment extends GlslSegment {
    String text;

    public ExtensionSegment(String text) {
        super(SegmentType.EXTENSION);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ExtensionSegment setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append("#extension ").append(text);
    }
}
