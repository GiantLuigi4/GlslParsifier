package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarSpecifier {
    @NotNull String type;
    @NotNull String name;
    @Nullable String arrayData;

    public VarSpecifier(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    public VarSpecifier setType(@NotNull String type) {
        this.type = type;
        return this;
    }

    public VarSpecifier setName(@NotNull String name) {
        this.name = name;
        return this;
    }

    public VarSpecifier setArrayData(@NotNull String arrayData) {
        this.arrayData = arrayData;
        return this;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getArrayData() {
        return arrayData;
    }

    @Override
    public String toString() {
        if (arrayData != null)
            return type + " " + name + arrayData;
        return type + " " + name;
    }
}
