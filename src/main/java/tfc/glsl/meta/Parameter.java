package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.meta.enums.StorageQualifier;

public class Parameter {
    StorageQualifier qualifier;
    @NotNull VarSpecifier var;

    public Parameter(@NotNull VarSpecifier var) {
        this.var = var;
    }

    public StorageQualifier getQualifier() {
        return qualifier;
    }

    public Parameter setQualifier(StorageQualifier qualifier) {
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
            return var.accessStr() + qualifier + " " + var.varStr();
        return var.toString();
    }
}
