/*
 * @(#)SingleFieldTypeDenoter.java                2.1 2003/10/07
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

public class SingleFieldTypeDenoter extends FieldTypeDenoter {

    public SingleFieldTypeDenoter(Identifier iAST, TypeDenoter tAST,
            SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        T = tAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitSingleFieldTypeDenoter(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SingleFieldTypeDenoter) {
            SingleFieldTypeDenoter ft = (SingleFieldTypeDenoter) obj;
            return (this.I.spelling.compareTo(ft.I.spelling) == 0)
                    && this.T.equals(ft.T);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.I);
        hash = 47 * hash + Objects.hashCode(this.T);
        return hash;
    }

    public Identifier I;
    public TypeDenoter T;
}
