package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;
import tfc.glsl.util.DuplicationUtil;

import java.util.ArrayList;
import java.util.List;

public class ForStatement extends GlslStatement {
    GlslStatement varDef;
    GlslValue comparison;
    GlslStatement increment;
    List<GlslStatement> body = new ArrayList<>();

    public ForStatement(GlslStatement varDef, GlslValue comparison, GlslStatement increment) {
        super(StatementType.FOR_LOOP);
        this.varDef = varDef;
        this.comparison = comparison;
        this.increment = increment;
    }

    public GlslStatement getVarDef() {
        return varDef;
    }

    public ForStatement setVarDef(GlslStatement varDef) {
        this.varDef = varDef;
        return this;
    }

    public GlslValue getComparison() {
        return comparison;
    }

    public ForStatement setComparison(GlslValue comparison) {
        this.comparison = comparison;
        return this;
    }

    public GlslStatement getIncrement() {
        return increment;
    }

    public ForStatement setIncrement(GlslStatement increment) {
        this.increment = increment;
        return this;
    }

    public ForStatement addStatement(GlslStatement statement) {
        body.add(statement);
        return this;
    }

    public List<GlslStatement> getBody() {
        return body;
    }

    public ForStatement setBody(List<GlslStatement> body) {
        this.body = body;
        return this;
    }

    protected String toStr(Object statement, boolean endSemi) {
        if (statement == null) {
            return ";";
        }
        String str = statement.toString();
        if (endSemi) {
            if (!str.endsWith(";")) return str + ";";
            return str;
        } else {
            if (str.endsWith(";")) return str.substring(0, str.length() - 1);
            return str;
        }
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("for (");
        builder.append(toStr(varDef.asString(), true));
        builder.append(" ");
        builder.append(toStr(comparison.asString(), true));
        builder.append(" ");
        builder.append(toStr(increment.asString(), false));
        builder.append(") {\n");

        for (GlslStatement glslStatement : body) {
            glslStatement.asString(builder, indentLevel + 1);
            builder.append("\n");
        }
        builder.append("\t".repeat(indentLevel)).append("}");
    }

    @Override
    public GlslStatement duplicate() {
        return new ForStatement(
                varDef.duplicate(),
                comparison.duplicate(),
                increment.duplicate()
        ).setBody(DuplicationUtil.duplicateBody(body));
    }
}
