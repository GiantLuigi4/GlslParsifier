package tfc.glsl.value;

import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;
import tfc.glsl.parse.GlslToken;

import java.util.ArrayList;
import java.util.List;

public class CreateArrayValue extends GlslValue {
    GlslValue arrayType;
    List<GlslValue> values;

    public CreateArrayValue(GlslValue arrayType, List<GlslValue> values) {
        super(ValueType.CREATE_ARRAY);
        this.arrayType = arrayType;
        this.values = values;
    }

    @Override
    public void asString(StringBuilder builder) {
        arrayType.asString(builder);
        builder.append("[](");
        for (int i = 0; i < values.size(); i++) {
            values.get(i).asString(builder);
            if (i != values.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
    }

    @Override
    public GlslValue duplicate() {
        List<GlslValue> dup = new ArrayList<>();
        for (GlslValue value : values) {
            if (value != null)
                dup.add(value.duplicate());
            else dup.add(null);
        }
        return new CreateArrayValue(arrayType.duplicate(), dup);
    }

    public GlslValue getArrayType() {
        return arrayType;
    }

    public CreateArrayValue setArrayType(GlslValue arrayType) {
        this.arrayType = arrayType;
        return this;
    }

    public List<GlslValue> getValues() {
        return values;
    }

    public CreateArrayValue setValues(List<GlslValue> values) {
        this.values = values;
        return this;
    }
}
