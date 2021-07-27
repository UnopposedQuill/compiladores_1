/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

/**
 *
 * @author Esteban
 */
public class ForLoopCommand extends Command {

    /**
     * This creates a new LoopCommand AST
     *
     * @param initialDeclaration A ConstantDeclaration representing initial
     * value
     * @param haltingExpression An expression containing the halting value
     * @param cAST It's command
     * @param thePosition Where it can be found in the source
     */
    public ForLoopCommand(ConstDeclaration initialDeclaration, Expression haltingExpression, Command cAST, SourcePosition thePosition) {
        super(thePosition);
        InitialDeclaration = initialDeclaration;
        HaltingExpression = haltingExpression;
        C = cAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitForLoopCommand(this, o);
    }

    public ConstDeclaration InitialDeclaration;
    public Expression HaltingExpression;
    public Command C;
}
