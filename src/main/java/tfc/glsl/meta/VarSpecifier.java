package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarSpecifier {
    @NotNull String type;
    @NotNull String name;
    @Nullable ArraySpecifier array;

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

    public VarSpecifier setArray(ArraySpecifier array) {
        this.array = array;
        return this;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ArraySpecifier getArray() {
        return array;
    }

    @Override
    public String toString() {
        if (array != null) {
            StringBuilder builder = new StringBuilder();
            array.asString(builder);
            return type + builder + " " + name;
        }
        return type + " " + name;
    }
}
