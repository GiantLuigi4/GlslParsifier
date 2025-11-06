package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.StatementType;
import tfc.glsl.util.DuplicationUtil;

import java.util.ArrayList;
import java.util.List;

public class SwitchStatement extends GlslStatement {
    GlslValue value;
    List<SwitchCase> cases = new ArrayList<>();

    public SwitchStatement(GlslValue value) {
        super(StatementType.SWITCH);
        this.value = value;
    }

    public GlslValue getValue() {
        return value;
    }

    public SwitchStatement setValue(GlslValue value) {
        this.value = value;
        return this;
    }

    public List<SwitchCase> getCases() {
        return cases;
    }

    public SwitchStatement setCases(List<SwitchCase> cases) {
        this.cases = cases;
        return this;
    }

    public void addCase(SwitchCase switchCase) {
        this.cases.add(switchCase);
    }

    public static class SwitchCase {
        GlslValue value;
        List<GlslStatement> statements = new ArrayList<>();

        public SwitchCase(GlslValue value) {
            this.value = value;
        }

        public GlslValue getValue() {
            return value;
        }

        public SwitchCase setValue(GlslValue value) {
            this.value = value;
            return this;
        }

        public List<GlslStatement> getStatements() {
            return statements;
        }

        public SwitchCase setStatements(List<GlslStatement> statements) {
            this.statements = statements;
            return this;
        }

        public void addStatement(GlslStatement glslStatement) {
            statements.add(glslStatement);
        }

        public SwitchCase duplicate() {
            return new SwitchCase(
                    value.duplicate()
            ).setStatements(DuplicationUtil.duplicateBody(statements));
        }
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel));
        builder.append("switch (");
        value.asString(builder);
        builder.append(") {\n");
        for (SwitchCase aCase : cases) {
            builder.append("\t".repeat(indentLevel));
            builder.append("case ");
            aCase.value.asString(builder);
            builder.append(":\n");

            for (GlslStatement statement : aCase.statements) {
                statement.asString(builder, indentLevel + 1);
                builder.append("\n");
            }
        }
        builder.append("\t".repeat(indentLevel));
        builder.append("}");
    }

    @Override
    public GlslStatement duplicate() {
        List<SwitchCase> nv = new ArrayList<>(cases.size());
        for (SwitchCase aCase : cases) {
            nv.add(aCase.duplicate());
        }
        return new SwitchStatement(
                value.duplicate()
        ).setCases(nv);
    }
}
