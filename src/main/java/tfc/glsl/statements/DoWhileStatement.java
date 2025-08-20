package tfc.glsl.statements;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;

import java.util.ArrayList;
import java.util.List;

public class DoWhileStatement extends GlslStatement {
    @NotNull GlslValue condition;
    @NotNull List<GlslStatement> block = new ArrayList<>();

    public DoWhileStatement(@NotNull GlslValue condition) {
        super(StatementType.WHILE);
        this.condition = condition;
    }

    public GlslValue getCondition() {
        return condition;
    }

    public DoWhileStatement setCondition(@NotNull GlslValue condition) {
        this.condition = condition;
        return this;
    }

    public void addStatement(GlslStatement statement) {
        block.add(statement);
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("do {\n");
        for (GlslStatement glslStatement : block) {
            glslStatement.asString(builder, indentLevel + 1);
            builder.append("\n");
        }
        builder.append("\t".repeat(indentLevel)).append("} while (");
        builder.append(condition);
        builder.append(");");
    }
}
