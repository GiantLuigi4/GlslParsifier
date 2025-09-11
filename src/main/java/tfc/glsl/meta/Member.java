package tfc.glsl.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Member {
    @Nullable LayoutQualifier layout;
    @NotNull VarSpecifier var;

    public Member(@NotNull VarSpecifier var) {
        this.var = var;
    }

    public Member setLayout(LayoutQualifier layout) {
        this.layout = layout;
        return this;
    }

    public Member setVar(@NotNull VarSpecifier var) {
        this.var = var;
        return this;
    }

    public LayoutQualifier getLayout() {
        return layout;
    }

    public VarSpecifier getVar() {
        return var;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (layout != null) builder.append(layout).append(" ");
        builder.append(var);
        return builder.toString();
    }

    public Member template() {
        return new Member(var.template()).setLayout(layout);
    }
}
