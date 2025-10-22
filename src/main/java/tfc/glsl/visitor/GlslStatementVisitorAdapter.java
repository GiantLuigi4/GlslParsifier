package tfc.glsl.visitor;

import tfc.glsl.segments.*;
import tfc.glsl.statements.*;

public abstract class GlslStatementVisitorAdapter implements GlslStatementVisitor {
    @Override
    public void visitAssignment(AssignmentStatement statement) {

    }

    @Override
    public void visitInc(IncStatement statement) {

    }

    @Override
    public void visitVarDef(VarDefStatement statement) {

    }

    @Override
    public void visitContinue(ContinueStatement statement) {

    }

    @Override
    public void visitBreak(ContinueStatement statement) {

    }

    @Override
    public void visitWhile(WhileStatement statement) {

    }

    @Override
    public void visitReturn(ReturnStatement statement) {

    }

    @Override
    public void visitSwitch(SwitchStatement statement) {

    }

    @Override
    public void visitDiscard(DiscardStatement statement) {

    }

    @Override
    public void visitForLoop(ForStatement statement) {

    }

    @Override
    public void visitArbitraryStatement(ArbitraryStatement statement) {

    }

    @Override
    public void visitConditionalStatement(ConditionalStatement statement) {

    }

    @Override
    public void visitMethodCall(MethodCallStatement statement) {

    }
}
