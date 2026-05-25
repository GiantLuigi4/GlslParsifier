package tfc.glsl;

import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.SegmentType;

import java.util.ArrayList;
import java.util.List;

public class GlslFile {
    String version;
    List<GlslSegment> segments = new ArrayList<>();

    public GlslFile(String version) {
        this.version = version;
    }
	
	/**
	 * Convenience function to ensure all extension directives are at the top of the file
	 */
	public void fixDirectives() {
		List<GlslSegment> extensionDirectives = new ArrayList<>();
		
		for (GlslSegment segment : segments) {
			if (segment.getSegmentType() == SegmentType.EXTENSION) {
				extensionDirectives.add(segment);
			}
		}
		
		segments.removeAll(extensionDirectives);
		
		segments.addAll(0, extensionDirectives);
	}
	
    public void addSegment(GlslSegment segment) {
        segments.add(segment);
    }

    public String getVersion() {
        return version;
    }

    public GlslFile setVersion(String version) {
        this.version = version;
        return this;
    }

    public List<GlslSegment> getSegments() {
        return segments;
    }

    public GlslFile setSegments(List<GlslSegment> segments) {
        this.segments = segments;
        return this;
    }

    // to preserve the functionality of debuggers, toString does not return a string representation of the file
    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append("#version ").append(version).append("\n\n");

        int len = segments.size();
        for (int i = 0; i < len; i++) {
            segments.get(i).asString(builder);
            if (i != len - 1) {
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }
}
