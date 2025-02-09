/*
 * @(#)ConstFormalParameter.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */
package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;
import java.util.Objects;

public class ConstFormalParameter extends FormalParameter {

    public ConstFormalParameter(Identifier iAST, TypeDenoter tAST,
            SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        T = tAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitConstFormalParameter(this, o);
    }

    public Identifier I;
    public TypeDenoter T;

    /**
     * Checks if the formal parameter is of the same type, important for
     * parameter type checking in checker.
     *
     * @param fpAST Type to check against.
     * @return True if they are the same type.
     */
    @Override
    public boolean equals(Object fpAST) {
        if (fpAST instanceof ConstFormalParameter) {
            ConstFormalParameter cfpAST = (ConstFormalParameter) fpAST;
            return T.equals(cfpAST.T);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.I);
        hash = 97 * hash + Objects.hashCode(this.T);
        return hash;
    }
}
