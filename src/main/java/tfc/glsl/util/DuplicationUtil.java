package tfc.glsl.util;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.meta.Parameter;
import tfc.glsl.meta.VarSpecifier;

import java.util.ArrayList;
import java.util.List;

public class DuplicationUtil {
    public static List<GlslStatement> duplicateBody(List<GlslStatement> statements) {
        List<GlslStatement> nv = new ArrayList<>(statements.size());
        for (GlslStatement glslStatement : statements) {
            nv.add(glslStatement.duplicate());
        }
        return nv;
    }

    public static List<Parameter> duplicateParams(List<Parameter> params) {
        List<Parameter> nv = new ArrayList<>(params.size());
        for (Parameter param : params) {
            nv.add(new Parameter(
                    dupVarSpec(param.getVar())
            ).setQualifier(param.getQualifier()));
        }
        return nv;
    }

    public static VarSpecifier dupVarSpec(VarSpecifier var) {
        List<String> modifs = var.getModifiers();
        return new VarSpecifier(var.getType(), var.getName())
                .setArray(var.getArray()).setModifiers(
                        modifs != null ? new ArrayList<>(modifs) : modifs
                );
    }
}
