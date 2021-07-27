/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class VarDeclarationInitialized extends VarDeclaration {

    /**
     * This will create a new instance for an Initialized Variable Declaration
     * Notice that the type will be inferred according to the expression type,
     * and then the variable will be bound to that type
     * @param iAST The Identifier to bind the variable to
     * @param eAST The initial expression (and hence the type) of the variable
     * @param thePosition Where it is located in the source file
     */
    public VarDeclarationInitialized (Identifier iAST, Expression eAST,
                                      SourcePosition thePosition) {
        super (iAST, null, thePosition);
        E = eAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitVarDeclarationInitialized(this, o);
    }
    public Expression E;
}
