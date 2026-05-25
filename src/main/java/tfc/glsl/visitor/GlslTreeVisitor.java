package tfc.glsl.visitor;

import tfc.glsl.GlslFile;
import tfc.glsl.base.GlslSegment;
import tfc.glsl.base.GlslStatement;
import tfc.glsl.base.GlslValue;
import tfc.glsl.segments.*;
import tfc.glsl.statements.*;

public class GlslTreeVisitor {
    GlslValueVisitor valueVisitor;
    GlslStatementVisitor statementVisitor;
    GlslSegmentVisitor segmentVisitor;
    GlslStatementVisitor statementInnerVisitor = new GlslStatementVisitor() {
        @Override
        public void visitAssignment(AssignmentStatement statement) {
            statementVisitor.visitAssignment(statement);
            valueVisitor.visitValue(statement.getRef());
            valueVisitor.visitValue(statement.getValue());
        }

        @Override
        public void visitInc(IncStatement statement) {
            statementVisitor.visitInc(statement);
            valueVisitor.visitValue(statement.getRef());
        }

        @Override
        public void visitVarDef(VarDefStatement statement) {
            statementVisitor.visitVarDef(statement);
            if (statement.getValue() != null)
                valueVisitor.visitValue(statement.getValue());
        }

        @Override
        public void visitContinue(ContinueStatement statement) {
            statementVisitor.visitContinue(statement);
        }

        @Override
        public void visitBreak(BreakStatement statement) {
            statementVisitor.visitBreak(statement);
        }

        @Override
        public void visitWhile(WhileStatement statement) {
            statementVisitor.visitWhile(statement);
            valueVisitor.visitValue(statement.getCondition());
        }

        @Override
        public void visitReturn(ReturnStatement statement) {
            statementVisitor.visitReturn(statement);
            if (statement.getValue() != null) {
                valueVisitor.visitValue(statement.getValue());
            }
        }

        @Override
        public void visitSwitch(SwitchStatement statement) {
            statementVisitor.visitSwitch(statement);
            valueVisitor.visitValue(statement.getValue());
            for (SwitchStatement.SwitchCase aCase : statement.getCases()) {
				if (aCase.getValue() != null)
	                valueVisitor.visitValue(aCase.getValue());
            }
        }

        @Override
        public void visitDiscard(DiscardStatement statement) {
            statementVisitor.visitDiscard(statement);
        }

        @Override
        public void visitForLoop(ForStatement statement) {
            statementVisitor.visitForLoop(statement);
            valueVisitor.visitValue(statement.getComparison());
        }

        @Override
        public void visitArbitraryStatement(ArbitraryStatement statement) {
            statementVisitor.visitArbitraryStatement(statement);
        }

        @Override
        public void visitConditionalStatement(ConditionalStatement statement) {
            statementVisitor.visitConditionalStatement(statement);
            for (ConditionalStatement.ConditionalCode conditionalCode : statement.getChain()) {
                if (conditionalCode.getCondition() != null)
                    valueVisitor.visitValue(conditionalCode.getCondition());
            }
        }

        @Override
        public void visitMethodCall(MethodCallStatement statement) {
            statementVisitor.visitMethodCall(statement);
            valueVisitor.visitValue(statement.getValue());
        }
    };
    GlslSegmentVisitor segmentInnerVisitor = new GlslSegmentVisitorAdapter() {
        @Override
        public void visitMember(GlslMemberSegment segment) {
            if (segment.getValue() != null) {
                valueVisitor.visitValue(segment.getValue());
            }
        }

        @Override
        public void visitVar(GlslVarSegment segment) {
            if (segment.getValue() != null) {
                valueVisitor.visitValue(segment.getValue());
            }
        }

        @Override
        public void visitCode(GlslCodeSegment segment) {
            for (GlslStatement statement : segment.getStatements()) {
                statementInnerVisitor.visitStatement(statement);
            }
        }

        @Override
        public void visitBlock(GlslBlockSegment segment) {
            // no value
        }

        @Override
        public void visitArbitrary(ArbitrarySegment segment) {
            // no value
        }
    };

    public GlslTreeVisitor(GlslValueVisitor valueVisitor, GlslStatementVisitor statementVisitor, GlslSegmentVisitor segmentVisitor) {
        this.valueVisitor = valueVisitor;
        this.statementVisitor = statementVisitor;
        this.segmentVisitor = segmentVisitor;

        if (valueVisitor == null) this.valueVisitor = new GlslValueVisitorAdapter() {};
        if (segmentVisitor == null) this.segmentVisitor = new GlslSegmentVisitorAdapter() {};
        if (statementVisitor == null) this.statementVisitor = new GlslStatementVisitorAdapter() {};
    }

    public void visit(GlslValue value) {
        valueVisitor.visitValue(value);
    }

    public void visit(GlslStatement statement) {
        statementInnerVisitor.visitStatement(statement);
    }

    public void visit(GlslSegment segment) {
        segmentInnerVisitor.visitSegment(segment);
    }

    public void visit(GlslFile file) {
        for (GlslSegment segment : file.getSegments()) {
            segmentVisitor.visitSegment(segment);
            segmentInnerVisitor.visitSegment(segment);
        }
    }
}
