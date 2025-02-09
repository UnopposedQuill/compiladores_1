/*
 * @(#)SimpleTypeDenoter.java                        2.1 2003/10/07
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

public class SimpleTypeDenoter extends TypeDenoter {

    public SimpleTypeDenoter(Identifier iAST, SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitSimpleTypeDenoter(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        return false; // should not happen
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.I);
        return hash;
    }

    public Identifier I;
}
