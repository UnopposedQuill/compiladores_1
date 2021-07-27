package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.Identifier;
import Triangle.AbstractSyntaxTrees.Visitor;

public abstract class PendingCall {
    private final IdentificationTable callContextIdTable;
    private int level;

    public PendingCall(IdentificationTable callContextIdTable) {
        this.callContextIdTable = callContextIdTable;
        //setCallContextIdTable(callContextIdTable);
        this.level = callContextIdTable.getLevel();
    }

    public abstract void visitPendingCall(Visitor v, Object o);

    public abstract Identifier getProcFuncIdentifier();

    public IdentificationTable getCallContextIdTable() {
        return callContextIdTable;
    }

    public int getLevel() {
        return level;
    }
}
