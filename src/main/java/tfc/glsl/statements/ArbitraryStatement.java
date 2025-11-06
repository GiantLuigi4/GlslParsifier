package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;

public class ArbitraryStatement extends GlslStatement {
    @NotNull String text;

    public ArbitraryStatement(@NotNull String text) {
        super(StatementType.ARBITRARY);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ArbitraryStatement setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append(text);
    }

    @Override
    public GlslStatement duplicate() {
        return new ArbitraryStatement(text);
    }
}
