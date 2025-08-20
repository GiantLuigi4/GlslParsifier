package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class TokenValue extends GlslValue {
    @NotNull String text;

    public TokenValue(@NotNull String text) {
        super(ValueType.TOKEN);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public TokenValue setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append(text);
    }
}
