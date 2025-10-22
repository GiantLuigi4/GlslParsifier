package tfc.glsl.value;

import org.jetbrains.annotations.NotNull;
import tfc.glsl.base.GlslValue;
import tfc.glsl.base.ValueType;

public class OperationValue extends GlslValue {
    @NotNull GlslValue left, right;
    @NotNull String op;

    public OperationValue(@NotNull GlslValue left, @NotNull String op, @NotNull GlslValue right) {
        super(ValueType.OPERATION);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public GlslValue getLeft() {
        return left;
    }

    public OperationValue setLeft(GlslValue left) {
        this.left = left;
        return this;
    }

    public GlslValue getRight() {
        return right;
    }

    public OperationValue setRight(GlslValue right) {
        this.right = right;
        return this;
    }

    public String getOp() {
        return op;
    }

    public OperationValue setOp(String op) {
        this.op = op;
        return this;
    }

    protected String noParen() {
        return left + " " + op + " " + right;
    }

    protected boolean isInterpretationFree(ValueType type) {
        return
//                type == ValueType.TOKEN ||
//                type == ValueType.CONSTANT ||
//                type == ValueType.UNARY_OPERATION ||
//                type == ValueType.PARENTH
                false
                ;
    }

    @Override
    public void asString(StringBuilder builder) {
//        boolean noInterpretLeft = isInterpretationFree(left.getValueType());
//        boolean noInterpretRight = isInterpretationFree(right.getValueType());
//
//        if (left.getValueType() == ValueType.OPERATION || noInterpretLeft) {
//            if (right.getValueType() == ValueType.OPERATION || noInterpretRight) {
//                if (noInterpretLeft || noInterpretRight)
//                    return noParen();
//
//                if (((OperationValue) left).op.equals(((OperationValue) right).op))
//                    return noParen();
//            }
//        }

//        if (noInterpretLeft && noInterpretRight) {
//            return noParen();
//        }

//        builder.append("(");
//        left.asString(builder);
//        builder.append(" ")
//                .append(op)
//                .append(" ");
//        right.asString(builder);
//        builder.append(")");

        boolean constRes = left.constResolvable() && right.constResolvable();
        if (!constRes) {
            builder.append("(");
        }
        left.asString(builder);
        builder.append(" ")
                .append(op)
                .append(" ");
        right.asString(builder);
        if (!constRes) {
            builder.append(")");
        }
    }

    @Override
    public boolean constResolvable() {
        return false;
    }

    @Override
    public GlslValue duplicate() {
        return new OperationValue(
                left.duplicate(), op, right.duplicate()
        );
    }
}
