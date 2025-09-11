package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;

import java.util.ArrayList;
import java.util.List;

public class WhileStatement extends GlslStatement {
    @NotNull GlslValue condition;
    @NotNull List<GlslStatement> block = new ArrayList<>();

    public WhileStatement(@NotNull GlslValue condition) {
        super(StatementType.WHILE);
        this.condition = condition;
    }

    public GlslValue getCondition() {
        return condition;
    }

    public WhileStatement setCondition(@NotNull GlslValue condition) {
        this.condition = condition;
        return this;
    }

    public void addStatement(GlslStatement statement) {
        block.add(statement);
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("while (");
        condition.asString(builder);
        builder.append(") {\n");
        for (GlslStatement glslStatement : block) {
            glslStatement.asString(builder, indentLevel + 1);
            builder.append("\n");
        }
        builder.append("\t".repeat(indentLevel)).append("}");
    }
}
