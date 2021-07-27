package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.Expression;
import Triangle.AbstractSyntaxTrees.TypeDenoter;

public class FutureCallExpression {
    private final TypeDenoter typeDenoterToCheck;
    private final Expression E;

    /**
     * Constructor for a call expression with a future declaration
     * @param typeDenoterToCheck The expected type for the call expression to have
     * @param e The call expression itself
     */
    public FutureCallExpression(TypeDenoter typeDenoterToCheck, Expression e) {
        this.typeDenoterToCheck = typeDenoterToCheck;
        E = e;
    }

    public TypeDenoter getTypeDenoterToCheck() {
        return typeDenoterToCheck;
    }

    public Expression getE() {
        return E;
    }
}
