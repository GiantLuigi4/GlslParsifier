package tfc.glsl.statements;

import org.jetbrains.annotations.Nullable;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;

import java.util.ArrayList;
import java.util.List;

public class ConditionalStatement extends GlslStatement {
    List<ConditionalCode> chain = new ArrayList<>();

    public ConditionalStatement() {
        super(StatementType.CONDITIONAL);
    }

    public void addStep(ConditionalCode conditionalCode) {
        chain.add(conditionalCode);
    }

    public ConditionalCode getLastConditional() {
        return chain.get(chain.size() - 1);
    }

    public static class ConditionalCode {
        @Nullable GlslValue condition;
        List<GlslStatement> statements = new ArrayList<>();

        public ConditionalCode(@Nullable GlslValue condition) {
            this.condition = condition;
        }

        public GlslValue getCondition() {
            return condition;
        }

        public ConditionalCode setCondition(GlslValue condition) {
            this.condition = condition;
            return this;
        }

        public List<GlslStatement> getStatements() {
            return statements;
        }

        public void addStatement(GlslStatement statement) {
            statements.add(statement);
        }
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        boolean first = true;

        builder.append("\t".repeat(indentLevel)).append("if ");
        for (ConditionalCode conditionalCode : chain) {
            if (!first) {
                builder.append(" else");
                if (conditionalCode.condition != null)
                    builder.append(" if ");
            }
            if (conditionalCode.condition != null) {
                builder.append("(");
                conditionalCode.condition.asString(builder);
                builder.append(")");
            }

            builder.append(" {\n");
            for (GlslStatement statement : conditionalCode.statements) {
                statement.asString(builder, indentLevel + 1);
                builder.append("\n");
            }
            builder.append("\t".repeat(indentLevel)).append("}");
            first = false;
        }
    }

    public List<ConditionalCode> getChain() {
        return chain;
    }
}
