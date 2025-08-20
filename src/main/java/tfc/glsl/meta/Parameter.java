package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;

public class Parameter {
    String qualifier;
    @NotNull VarSpecifier var;

    public Parameter(@NotNull VarSpecifier var) {
        this.var = var;
    }

    public String getQualifier() {
        return qualifier;
    }

    public Parameter setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public VarSpecifier getVar() {
        return var;
    }

    public Parameter setVar(VarSpecifier var) {
        this.var = var;
        return this;
    }

    @Override
    public String toString() {
        if (qualifier != null)
            return qualifier + " " + var;
        return var.toString();
    }
}
