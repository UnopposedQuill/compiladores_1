/*
 * @(#)FuncFormalParameter.java                2.1 2003/10/07
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

public class FuncFormalParameter extends FormalParameter {

    public FuncFormalParameter(Identifier iAST, FormalParameterSequence fpsAST,
            TypeDenoter tAST, SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        FPS = fpsAST;
        T = tAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitFuncFormalParameter(this, o);
    }

    @Override
    public boolean equals(Object fpAST) {
        if (fpAST instanceof FuncFormalParameter) {
            FuncFormalParameter ffpAST = (FuncFormalParameter) fpAST;
            return FPS.equals(ffpAST.FPS) && T.equals(ffpAST.T);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.I);
        hash = 61 * hash + Objects.hashCode(this.FPS);
        hash = 61 * hash + Objects.hashCode(this.T);
        return hash;
    }

    public Identifier I;
    public FormalParameterSequence FPS;
    public TypeDenoter T;
}
