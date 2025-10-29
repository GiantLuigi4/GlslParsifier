package tfc.glsl.statements;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.StatementType;

import java.util.List;

public class BodyStatement extends GlslStatement {
    List<GlslStatement> body;

    public BodyStatement(List<GlslStatement> body) {
        super(StatementType.BODY);
        this.body = body;
    }

    public List<GlslStatement> getBody() {
        return body;
    }

    public BodyStatement setBody(List<GlslStatement> body) {
        this.body = body;
        return this;
    }

    @Override
    public void asString(StringBuilder builder, int indentLevel) {
        builder.append("\t".repeat(indentLevel)).append("{\n");
        for (GlslStatement glslStatement : body) {
            glslStatement.asString(builder, indentLevel + 1);
            builder.append("\n");
        }
        builder.append("\t".repeat(indentLevel)).append("}");
    }
}
