package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.CallCommand;
import Triangle.AbstractSyntaxTrees.Identifier;
import Triangle.AbstractSyntaxTrees.Visitor;

public class PendingCallCommand extends PendingCall {

    private final CallCommand callCommand;

    public PendingCallCommand(IdentificationTable callContextIdTable, CallCommand callCommand) {
        super(callContextIdTable);
        this.callCommand = callCommand;
    }

    @Override
    public void visitPendingCall(Visitor v, Object o) {
        callCommand.visit(v, o);
    }

    @Override
    public Identifier getProcFuncIdentifier() {
        return this.callCommand.I;
    }
}
