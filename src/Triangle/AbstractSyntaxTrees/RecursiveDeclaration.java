package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class RecursiveDeclaration extends Declaration {

    public Declaration D;

    public RecursiveDeclaration(Declaration D, SourcePosition thePosition) {
        super(thePosition);
        this.D = D;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitRecursiveDeclaration(this, o);
    }
}
