package tfc.glsl.visitor;

import tfc.glsl.value.*;

public abstract class GlslValueVisitorAdapter implements GlslValueVisitor {
    @Override
    public void visitToken(TokenValue value) {

    }

    @Override
    public void visitConstant(ConstantValue value) {

    }

    @Override
    public void visitBoolean(BooleanValue value) {

    }

    @Override
    public void visitIncrement(IncValue value) {

    }

    @Override
    public void visitParenthesis(ParenthValue value) {

    }

    @Override
    public void visitAssignment(AssignmentValue value) {

    }

    @Override
    public void visitTernary(TernaryValue ternaryValue) {

    }

    @Override
    public void visitCall(MethodCallValue callValue) {

    }

    @Override
    public void visitOperation(OperationValue operationValue) {

    }

    @Override
    public void visitArrayAccess(AccessArrayValue accessArrayValue) {

    }

    @Override
    public void visitMemberAccess(AccessMemberValue accessMemberValue) {

    }

    @Override
    public void visitUnary(UnaryOperation unaryOperation) {

    }

    @Override
    public void visitArrayCreation(CreateArrayValue createArrayValue) {

    }

    @Override
    public void visitComma(CommaValue createArrayValue) {

    }
}
