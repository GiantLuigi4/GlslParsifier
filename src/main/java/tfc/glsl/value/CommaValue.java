package tfc.glsl.value;

import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;
import tfc.glsl.util.DuplicationUtil;

import java.util.List;

public class CommaValue extends GlslValue {
    List<GlslValue> values;

    public CommaValue(List<GlslValue> values) {
        super(ValueType.COMMA);
        this.values = values;
    }

    public List<GlslValue> getValues() {
        return values;
    }

    public CommaValue setValues(List<GlslValue> values) {
        this.values = values;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        for (int i = 0; i < values.size(); i++) {
            values.get(i).asString(builder);
            if (i != values.size() - 1) {
                builder.append(", ");
            }
        }
    }

    @Override
    public GlslValue duplicate() {
        return new CommaValue(DuplicationUtil.duplicateValues(values));
    }
}
