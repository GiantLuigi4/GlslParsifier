package tfc.glsl.base;

public abstract class GlslValue {
    private final ValueType type;

    public GlslValue(ValueType type) {
        this.type = type;
    }

    public ValueType getValueType() {
        return type;
    }

    public abstract void asString(StringBuilder builder);

    public final String asString() {
        StringBuilder builder = new StringBuilder();
        asString(builder);
        return builder.toString();
    }

    @Override
    public String toString() {
//        return asString();
        throw new RuntimeException();
    }

    public boolean constResolvable() {
        return false;
    }
}
