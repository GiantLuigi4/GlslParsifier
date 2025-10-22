package tfc.glsl.visitor;

import tfc.glsl.base.GlslStatement;
import tfc.glsl.segments.ArbitrarySegment;
import tfc.glsl.statements.*;

public interface GlslStatementVisitor {
    void visitAssignment(AssignmentStatement statement);

    void visitInc(IncStatement statement);

    void visitVarDef(VarDefStatement statement);

    void visitContinue(ContinueStatement statement);

    void visitBreak(ContinueStatement statement);

    void visitWhile(WhileStatement statement);

    void visitReturn(ReturnStatement statement);

    void visitSwitch(SwitchStatement statement);

    void visitDiscard(DiscardStatement statement);

    void visitForLoop(ForStatement statement);

    void visitArbitraryStatement(ArbitraryStatement statement);

    void visitConditionalStatement(ConditionalStatement statement);

    void visitMethodCall(MethodCallStatement statement);

    default void visitStatement(GlslStatement statement) {
        switch (statement.getStatementType()) {
            case ASSIGNMENT -> visitAssignment((AssignmentStatement) statement);
            case INC -> visitInc((IncStatement) statement);
            case VAR_DEF -> visitVarDef((VarDefStatement) statement);
            case CONTINUE -> visitContinue((ContinueStatement) statement);
            case BREAK -> visitBreak((ContinueStatement) statement);
            case WHILE -> {
                WhileStatement loop = (WhileStatement) statement;
                visitWhile(loop);
                for (GlslStatement glslStatement : loop.getBlock()) {
                    visitStatement(glslStatement);
                }
            }
            case RETURN -> visitReturn((ReturnStatement) statement);
            case SWITCH -> {
                visitSwitch((SwitchStatement) statement);
                throw new RuntimeException("TODO");
            }
            case DISCARD -> visitDiscard((DiscardStatement) statement);
            case FOR_LOOP -> {
                ForStatement loop = (ForStatement) statement;
                visitForLoop(loop);
                visitStatement(loop.getVarDef());
                visitStatement(loop.getIncrement());
                for (GlslStatement glslStatement : loop.getBody())
                    visitStatement(glslStatement);
            }
            case ARBITRARY -> visitArbitraryStatement((ArbitraryStatement) statement);
            case CONDITIONAL -> {
                visitConditionalStatement((ConditionalStatement) statement);
                for (ConditionalStatement.ConditionalCode conditionalCode : ((ConditionalStatement) statement).getChain()) {
                    for (GlslStatement conditionalCodeStatement : conditionalCode.getStatements()) {
                        visitStatement(conditionalCodeStatement);
                    }
                }
            }
            case METHOD_CALL -> visitMethodCall((MethodCallStatement) statement);
        }
    }

    default void visitSingleStatement(GlslStatement statement) {
        switch (statement.getStatementType()) {
            case ASSIGNMENT -> visitAssignment((AssignmentStatement) statement);
            case INC -> visitInc((IncStatement) statement);
            case VAR_DEF -> visitVarDef((VarDefStatement) statement);
            case CONTINUE -> visitContinue((ContinueStatement) statement);
            case BREAK -> visitBreak((ContinueStatement) statement);
            case WHILE -> visitWhile((WhileStatement) statement);
            case RETURN -> visitReturn((ReturnStatement) statement);
            case SWITCH -> visitSwitch((SwitchStatement) statement);
            case DISCARD -> visitDiscard((DiscardStatement) statement);
            case FOR_LOOP -> visitForLoop((ForStatement) statement);
            case ARBITRARY -> visitArbitraryStatement((ArbitraryStatement) statement);
            case CONDITIONAL -> visitConditionalStatement((ConditionalStatement) statement);
            case METHOD_CALL -> visitMethodCall((MethodCallStatement) statement);
        }
    }
}
