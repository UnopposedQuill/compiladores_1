/*
 * @(#)ProcFormalParameter.java                        2.1 2003/10/07
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

public class ProcFormalParameter extends FormalParameter {

    public Identifier I;
    public FormalParameterSequence FPS;
    
    public ProcFormalParameter(Identifier iAST, FormalParameterSequence fpsAST,
            SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        FPS = fpsAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitProcFormalParameter(this, o);
    }

    @Override
    public boolean equals(Object fpAST) {
        if (fpAST instanceof ProcFormalParameter) {
            ProcFormalParameter pfpAST = (ProcFormalParameter) fpAST;
            return FPS.equals(pfpAST.FPS);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.I);
        hash = 97 * hash + Objects.hashCode(this.FPS);
        return hash;
    }

}
