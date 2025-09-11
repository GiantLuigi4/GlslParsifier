package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VarSpecifier {
    @Nullable List<String> modifiers;
    @NotNull String type;
    @NotNull String name;
    @Nullable ArraySpecifier array;

    public VarSpecifier(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public VarSpecifier setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
        return this;
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
        StringBuilder varModifiers = new StringBuilder();
        if (modifiers != null) {
            for (String modifier : modifiers) {
                varModifiers.append(modifier).append(" ");
            }
        }

        if (array != null) {
            StringBuilder builder = new StringBuilder();
            array.asString(builder);
            return varModifiers + type + builder + " " + name;
        }
        return varModifiers + type + " " + name;
    }

    public String accessStr() {
        StringBuilder varModifiers = new StringBuilder();
        if (modifiers != null) {
            for (String modifier : modifiers) {
                varModifiers.append(modifier).append(" ");
            }
        }
        return varModifiers.toString();
    }

    public String varStr() {
        if (array != null) {
            StringBuilder builder = new StringBuilder();
            array.asString(builder);
            return type + builder + " " + name;
        }
        return type + " " + name;
    }

    public VarSpecifier template() {
        VarSpecifier res = new VarSpecifier(
                type, name
        );
        if (array != null) {
            res.setArray(array.template());
        }
        if (modifiers != null) {
            List<String> cpy = new ArrayList<>(modifiers);
            res.setModifiers(cpy);
        }
        return res;
    }
}
