package tfc.glsl;

import tfc.glsl.base.GlslSegment;

import java.util.ArrayList;
import java.util.List;

public class GlslFile {
    String version;
    List<GlslSegment> segments = new ArrayList<>();

    public GlslFile(String version) {
        this.version = version;
    }

    public void addSegment(GlslSegment segment) {
        segments.add(segment);
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
