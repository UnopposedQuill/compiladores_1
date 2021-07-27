package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.CallExpression;
import Triangle.AbstractSyntaxTrees.Identifier;
import Triangle.AbstractSyntaxTrees.Visitor;

public class PendingCallExpression extends PendingCall{

    private final CallExpression callExpression;

    public PendingCallExpression(IdentificationTable callContextIdTable, CallExpression callExpression) {
        super(callContextIdTable);
        this.callExpression = callExpression;
    }

    @Override
    public void visitPendingCall(Visitor v, Object o) {
        callExpression.visit(v,o);
    }

    @Override
    public Identifier getProcFuncIdentifier() {
        return this.callExpression.I;
    }
}
