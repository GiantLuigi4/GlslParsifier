package tfc.glsl.visitor;

import tfc.glsl.base.GlslValue;
import tfc.glsl.value.*;

public interface GlslValueVisitor {
    void visitToken(TokenValue value);

    void visitConstant(ConstantValue value);

    void visitBoolean(BooleanValue value);

    void visitIncrement(IncValue value);

    void visitParenthesis(ParenthValue value);

    void visitAssignment(AssignmentValue value);

    void visitTernary(TernaryValue ternaryValue);

    void visitCall(MethodCallValue callValue);

    void visitOperation(OperationValue operationValue);

    void visitArrayAccess(AccessArrayValue accessArrayValue);

    void visitMemberAccess(AccessMemberValue accessMemberValue);

    void visitUnary(UnaryOperation unaryOperation);

    void visitArrayCreation(CreateArrayValue createArrayValue);

    default void visitValue(GlslValue value) {
        switch (value.getValueType()) {
            case TOKEN -> visitToken((TokenValue) value);
            case CONSTANT -> visitConstant((ConstantValue) value);
            case BOOLEAN -> visitBoolean((BooleanValue) value);
            case INC -> {
                IncValue incValue = (IncValue) value;
                visitIncrement(incValue);
                visitValue(incValue.getRef());
            }
            case PARENTH -> {
                ParenthValue parenthValue = (ParenthValue) value;
                visitParenthesis(parenthValue);
                visitValue(parenthValue.getValue());
            }
            case ASSIGNMENT -> {
                AssignmentValue assignmentValue = (AssignmentValue) value;
                visitAssignment(assignmentValue);
                visitValue(assignmentValue.getValue());
                visitValue(assignmentValue.getRef());
            }
            case TERNARY -> {
                TernaryValue ternaryValue = (TernaryValue) value;
                visitTernary(ternaryValue);
                visitValue(ternaryValue.getCondition());
                visitValue(ternaryValue.getValueA());
                visitValue(ternaryValue.getValueB());
            }
            case FUNCTION -> {
                MethodCallValue callValue = (MethodCallValue) value;
                visitCall(callValue);
                visitValue(callValue.getName());
                for (GlslValue param : callValue.getParams()) {
                    visitValue(param);
                }
            }
            case OPERATION -> {
                OperationValue operationValue = (OperationValue) value;
                visitOperation(operationValue);
                visitValue(operationValue.getLeft());
                visitValue(operationValue.getRight());
            }
            case ACCESS_ARRAY -> {
                AccessArrayValue accessArrayValue = (AccessArrayValue) value;
                visitArrayAccess(accessArrayValue);
                visitValue(accessArrayValue.getObject());
                visitValue(accessArrayValue.getAccess());
            }
            case ACCESS_MEMBER -> {
                AccessMemberValue accessMemberValue = (AccessMemberValue) value;
                visitMemberAccess(accessMemberValue);
                visitValue(accessMemberValue.getMember());
                visitValue(accessMemberValue.getObject());
            }
            case UNARY_OPERATION -> {
                UnaryOperation unaryOperation = (UnaryOperation) value;
                visitUnary(unaryOperation);
                visitValue(unaryOperation.getValue());
            }
            case CREATE_ARRAY -> {
                CreateArrayValue createArrayValue = (CreateArrayValue) value;
                visitArrayCreation(createArrayValue);
                visitValue(createArrayValue.getArrayType());
                for (GlslValue arg : createArrayValue.getValues()) {
                    visitValue(arg);
                }
            }
            default -> throw new RuntimeException("Unrecognized value type");
        }
    }
}
