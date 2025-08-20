package tfc.glsl.meta;

import java.util.ArrayList;
import java.util.List;

public class LayoutQualifier {
    List<String> segments = new ArrayList<>();

    public LayoutQualifier addSegment(String segmentData) {
        segments.add(segmentData);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int len = segments.size();
        builder.append("layout (");
        for (int i = 0; i < len; i++) {
            builder.append(segments.get(i));
            if (i != len - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
