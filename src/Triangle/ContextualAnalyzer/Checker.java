/*
 * @(#)Checker.java                        2.1 2003/10/07
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
package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.*;
import Triangle.ErrorReporter;
import Triangle.StdEnvironment;
import Triangle.SyntacticAnalyzer.SourcePosition;

import java.util.ArrayList;

public final class Checker implements Visitor {

    // <editor-fold defaultstate="collapsed" desc="Commands">
    // Always returns null. Does not use the given object.
    @Override
    public Object visitAssignCommand(AssignCommand ast, Object o) {
        TypeDenoter variableType = (TypeDenoter) ast.V.visit(this, null);
        TypeDenoter expressionType = (TypeDenoter) ast.E.visit(this, null);
        if (!ast.V.variable) {
            reporter.reportError("LHS of assignment is not a variable", "", ast.V.position);
        }
        if (!expressionType.equals(variableType)) {
            reporter.reportError("assignment incompatibilty", "", ast.position);
        }
        return null;
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
        Declaration binding;

        if (o != null) {
            binding = (Declaration) o;
        } else {
            binding = (Declaration) ast.I.visit(this, null);
        }

        if (binding == null) {
            if (idTable.getRecLevel() > 0) {
                idTable.addPendingCall(new PendingCallCommand(new IdentificationTable(idTable), ast));
            } else {
                reportUndeclared(ast.I);
            }
        } else if (binding instanceof ProcDeclaration) {
            ast.APS.visit(this, ((ProcDeclaration) binding).FPS);
        } else if (binding instanceof ProcFormalParameter) {
            ast.APS.visit(this, ((ProcFormalParameter) binding).FPS);
        } else {
            reporter.reportError("\"%\" is not a procedure identifier",
                    ast.I.spelling, ast.I.position);
        }
        return null;
    }

    @Override
    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitIfCommand(IfCommand ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        if (!eType.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "", ast.E.position);
        }
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Object visitLetCommand(LetCommand ast, Object o) {
        idTable.openScope();
        ast.D.visit(this, null);
        ast.C.visit(this, null);
        idTable.closeScope();
        return null;
    }

    @Override
    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Object visitForLoopCommand(ForLoopCommand ast, Object o) {
        TypeDenoter initialExpressionType = (TypeDenoter) ast.InitialDeclaration.E.visit(this, null);
        TypeDenoter haltExpressionType = (TypeDenoter) ast.HaltingExpression.visit(this, null);
        if (!initialExpressionType.equals(StdEnvironment.integerType)) {
            reporter.reportError("Integer expression expected here", "", ast.InitialDeclaration.E.position);
        }
        if (!haltExpressionType.equals(StdEnvironment.integerType)) {
            reporter.reportError("Integer expression expected here", "", ast.HaltingExpression.position);
        }
        idTable.openScope();
        idTable.enter(ast.InitialDeclaration.I.spelling, ast.InitialDeclaration);
        ast.C.visit(this, null);
        idTable.closeScope();
        return null;
    }

    @Override
    public Object visitWhileLoopCommand(WhileLoopCommand ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);

        if (!eType.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "", ast.E.position);
        }

        ast.C.visit(this, null);

        return null;
    }

    @Override
    public Object visitDoWhileLoopCommand(DoWhileLoopCommand ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);

        if (!eType.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "", ast.E.position);
        }

        ast.C.visit(this, null);

        return null;
    }

    @Override
    public Object visitUntilLoopCommand(UntilLoopCommand ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);

        if (!eType.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "", ast.E.position);
        }

        ast.C.visit(this, null);

        return null;
    }

    @Override
    public Object visitDoUntilLoopCommand(DoUntilLoopCommand ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);

        if (!eType.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "", ast.E.position);
        }

        ast.C.visit(this, null);

        return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Expressions">
    // Returns the TypeDenoter denoting the type of the expression. Does
    // not use the given object.
    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        TypeDenoter elemType = (TypeDenoter) ast.AA.visit(this, null);
        IntegerLiteral il = new IntegerLiteral(Integer.toString(ast.AA.elemCount),
                ast.position);
        ast.type = new ArrayTypeDenoter(il, elemType, ast.position);
        return ast.type;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object o) {

        TypeDenoter e1Type = (TypeDenoter) ast.E1.visit(this, null);
        TypeDenoter e2Type = (TypeDenoter) ast.E2.visit(this, null);
        Declaration binding = (Declaration) ast.O.visit(this, BinaryOperatorDeclaration.class);
        
        if (binding == null) {
            reportUndeclared(ast.O);
            ast.type = StdEnvironment.errorType;
        } else {
            if (!(binding instanceof BinaryOperatorDeclaration)) {
                reporter.reportError("\"%\" is not a binary operator",
                        ast.O.spelling, ast.O.position);
                ast.type = StdEnvironment.errorType;
            } else {
                BinaryOperatorDeclaration binaryOperator = (BinaryOperatorDeclaration) binding;
                if (binaryOperator.ARG1 == StdEnvironment.anyType) {
                    // this operator must be "=" or "\="
                    if (!e1Type.equals(e2Type)) {
                        reporter.reportError("incompatible argument types for \"%\"",
                                ast.O.spelling, ast.position);
                    }
                } else if (!e1Type.equals(binaryOperator.ARG1)) {
                    reporter.reportError("wrong argument type for \"%\"",
                            ast.O.spelling, ast.E1.position);
                } else if (!e2Type.equals(binaryOperator.ARG2)) {
                    reporter.reportError("wrong argument type for \"%\"",
                            ast.O.spelling, ast.E2.position);
                }
                ast.type = binaryOperator.RES;
            }
        }
        return ast.type;
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
        //Bind it to either the parameter, or by searching it
        Declaration binding = o != null ? (Declaration) o : (Declaration) ast.I.visit(this, null);
        
        if (binding == null) {
            if (idTable.getRecLevel() > 0) {
                idTable.addPendingCall(new PendingCallExpression(new IdentificationTable(idTable), ast));
            } else {
                reportUndeclared(ast.I);
                ast.type = StdEnvironment.errorType;
            }

        } else if (binding instanceof FuncDeclaration) {
            ast.APS.visit(this, ((FuncDeclaration) binding).FPS);
            ast.type = ((FuncDeclaration) binding).T;

            //I filter the expressions that are the same as the ast, whose types
            //don't match, and issue an error for each of them.
            idTable.futureCallExpressions.stream().filter(
                    fCE -> fCE.getE() == ast && 
                           !fCE.getTypeDenoterToCheck().equals(ast.type)
                ).forEach(wrongTypeFunction -> reporter.reportError("body of function \"%\" has wrong type", ast.I.spelling, ast.position)
            );

        } else if (binding instanceof FuncFormalParameter) {
            ast.APS.visit(this, ((FuncFormalParameter) binding).FPS);
            ast.type = ((FuncFormalParameter) binding).T;
        } else {
            reporter.reportError("\"%\" is not a function identifier",
                    ast.I.spelling, ast.I.position);
            ast.type = StdEnvironment.errorType;
        }
        return ast.type;
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        ast.type = StdEnvironment.charType;
        return ast.type;
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
        TypeDenoter e1Type = (TypeDenoter) ast.E1.visit(this, null);
        if (!e1Type.equals(StdEnvironment.booleanType)) {
            reporter.reportError("Boolean expression expected here", "",
                    ast.E1.position);
        }
        TypeDenoter e2Type = (TypeDenoter) ast.E2.visit(this, null);
        TypeDenoter e3Type = (TypeDenoter) ast.E3.visit(this, null);
        if (!e2Type.equals(e3Type)) {
            reporter.reportError("incompatible limbs in if-expression", "", ast.position);
        }
        ast.type = e2Type;
        return ast.type;
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        ast.type = StdEnvironment.integerType;
        return ast.type;
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
        idTable.openScope();
        ast.D.visit(this, null);
        ast.type = (TypeDenoter) ast.E.visit(this, null);
        idTable.closeScope();
        return ast.type;
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
        FieldTypeDenoter rType = (FieldTypeDenoter) ast.RA.visit(this, null);
        ast.type = new RecordTypeDenoter(rType, ast.position);
        return ast.type;
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {

        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        Declaration binding = (Declaration) ast.O.visit(this, UnaryOperatorDeclaration.class);
        if (binding == null) {
            reportUndeclared(ast.O);
            ast.type = StdEnvironment.errorType;
        } else if (!(binding instanceof UnaryOperatorDeclaration)) {
            ast.type = StdEnvironment.errorType;
            reporter.reportError("\"%\" is not a unary operator",
                    ast.O.spelling, ast.O.position);
        } else {
            UnaryOperatorDeclaration ubinding = (UnaryOperatorDeclaration) binding;
            if (!eType.equals(ubinding.ARG)) {
                reporter.reportError("wrong argument type for \"%\"",
                        ast.O.spelling, ast.O.position);
            }
            ast.type = ubinding.RES;
        }
        return ast.type;
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
        ast.type = (TypeDenoter) ast.V.visit(this, null);
        return ast.type;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Declarations">
    // Always returns null. Does not use the given object.
    
    //@TODO: Implement
    @Override
    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o) {
        return null;//For now, every declaration is correct
    }

    @Override
    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast); // permits recursion
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }

        idTable.openScope();
        ast.FPS.visit(this, null);

        visitPendingCalls(ast.I);

        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        idTable.closeScope();

        //It is a future call, thus we don't know its type yet
        if (eType == null) {
            idTable.addFutureCallExp(new FutureCallExpression(ast.T, ast.E));
        } else if (!ast.T.equals(eType)) {
            reporter.reportError("body of function \"%\" has wrong type",
                    ast.I.spelling, ast.E.position);
        }
        return null;
    }

    @Override
    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
        idTable.enter(ast.I.spelling, ast); // permits recursion
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }

        idTable.openScope();

        ast.FPS.visit(this, null);

        visitPendingCalls(ast.I);

        ast.C.visit(this, null);
        idTable.closeScope();
        return null;
    }

    @Override
    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
        ast.D1.visit(this, null);
        ast.D2.visit(this, null);
        return null;
    }

    @Override
    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    //@TODO: Implement
    @Override
    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
        return null;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }

        return null;
    }

    @Override
    public Object visitVarDeclarationInitialized(VarDeclarationInitialized ast, Object o) {
        ast.T = (TypeDenoter) ast.E.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("identifier \"%\" already declared",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitRecursiveDeclaration(RecursiveDeclaration ast, Object o) {
        idTable.openRecursiveScope();
        ast.D.visit(this, null);
        idTable.closeRecursiveScope();

        if (idTable.getRecLevel() == 0) {
            idTable.pendingCalls.forEach(pendingCall ->{
                reporter.reportError("\"%\" is not a procedure or function identifier", 
                    pendingCall.getProcFuncIdentifier().spelling, pendingCall.getProcFuncIdentifier().position);
            });
            //Reset the list, so the compiler can keep going on checking errors
            idTable.pendingCalls = new ArrayList<>();
        }
        return null;
    }

    @Override
    public Object visitLocalDeclaration(LocalDeclaration ast, Object o) {
        idTable.openScope();
        ast.dAST1.visit(this, null);
        idTable.openScope();
        ast.dAST2.visit(this, null);
        idTable.closeLocalScope();
        return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Aggregates">
    // Array Aggregates
    // Returns the TypeDenoter for the Array Aggregate. Does not use the
    // given object.
    @Override
    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        TypeDenoter elemType = (TypeDenoter) ast.AA.visit(this, null);
        ast.elemCount = ast.AA.elemCount + 1;
        if (!eType.equals(elemType)) {
            reporter.reportError("incompatible array-aggregate element", "", ast.E.position);
        }
        return elemType;
    }

    @Override
    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
        TypeDenoter elemType = (TypeDenoter) ast.E.visit(this, null);
        ast.elemCount = 1;
        return elemType;
    }

    // Record Aggregates
    // Returns the TypeDenoter for the Record Aggregate. Does not use the
    // given object.
    @Override
    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        FieldTypeDenoter rType = (FieldTypeDenoter) ast.RA.visit(this, null);
        TypeDenoter fType = checkFieldIdentifier(rType, ast.I);
        if (fType != StdEnvironment.errorType) {
            //It found a type for the field type, and thus an error
            reporter.reportError("duplicate field \"%\" in record",
                    ast.I.spelling, ast.I.position);
        }
        ast.type = new MultipleFieldTypeDenoter(ast.I, eType, rType, ast.position);
        return ast.type;
    }

    @Override
    public Object visitSingleRecordAggregate(SingleRecordAggregate ast, Object o) {
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        ast.type = new SingleFieldTypeDenoter(ast.I, eType, ast.position);
        return ast.type;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Parameters">
    // Formal Parameters
    // Always returns null. Does not use the given object.
    @Override
    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("duplicated formal parameter \"%\"",ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
        idTable.openScope();
        ast.FPS.visit(this, null);
        idTable.closeScope();
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("duplicated formal parameter \"%\"",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
        idTable.openScope();
        ast.FPS.visit(this, null);
        idTable.closeScope();
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("duplicated formal parameter \"%\"",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        idTable.enter(ast.I.spelling, ast);
        if (ast.duplicated) {
            reporter.reportError("duplicated formal parameter \"%\"",
                    ast.I.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Object o) {
        return null;
    }

    @Override
    public Object visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Object o) {
        ast.FP.visit(this, null);
        ast.FPS.visit(this, null);
        return null;
    }

    @Override
    public Object visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Object o) {
        ast.FP.visit(this, null);
        return null;
    }

    // Actual Parameters
    // Always returns null. Uses the given FormalParameter.
    @Override
    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
        FormalParameter fp = (FormalParameter) o;
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);

        //It is a future call, thus we don't know its type yet
        if (eType == null) {
            idTable.addFutureCallExp(new FutureCallExpression(((ConstFormalParameter) fp).T, ast.E));
            return null;
        }

        if (!(fp instanceof ConstFormalParameter)) {
            reporter.reportError("const actual parameter not expected here", "",
                    ast.position);
        } else if (!eType.equals(((ConstFormalParameter) fp).T)) {
            reporter.reportError("wrong type for const actual parameter", "",
                    ast.E.position);
        }
        return null;
    }

    @Override
    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
        FormalParameter fp = (FormalParameter) o;

        Declaration binding = (Declaration) ast.I.visit(this, null);
        if (binding == null) {
            reportUndeclared(ast.I);
        } else if (!(binding instanceof FuncDeclaration
                || binding instanceof FuncFormalParameter)) {
            reporter.reportError("\"%\" is not a function identifier",
                    ast.I.spelling, ast.I.position);
        } else if (!(fp instanceof FuncFormalParameter)) {
            reporter.reportError("func actual parameter not expected here", "",
                    ast.position);
        } else {
            FormalParameterSequence FPS;
            TypeDenoter T;
            if (binding instanceof FuncDeclaration) {
                FPS = ((FuncDeclaration) binding).FPS;
                T = ((FuncDeclaration) binding).T;
            } else {
                FPS = ((FuncFormalParameter) binding).FPS;
                T = ((FuncFormalParameter) binding).T;
            }
            //@TODO: Check this section
            if (!FPS.equals(((FuncFormalParameter) fp).FPS)) {
                reporter.reportError("wrong signature for function \"%\"",
                        ast.I.spelling, ast.I.position);
            } else if (!T.equals(((FuncFormalParameter) fp).T)) {
                reporter.reportError("wrong type for function \"%\"",
                        ast.I.spelling, ast.I.position);
            }
        }
        return null;
    }

    @Override
    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
        FormalParameter fp = (FormalParameter) o;

        Declaration binding = (Declaration) ast.I.visit(this, null);
        if (binding == null) {
            reportUndeclared(ast.I);
        } else if (!(binding instanceof ProcDeclaration
                || binding instanceof ProcFormalParameter)) {
            reporter.reportError("\"%\" is not a procedure identifier",
                    ast.I.spelling, ast.I.position);
        } else if (!(fp instanceof ProcFormalParameter)) {
            reporter.reportError("proc actual parameter not expected here", "",
                    ast.position);
        } else {
            FormalParameterSequence FPS;
            if (binding instanceof ProcDeclaration) {
                FPS = ((ProcDeclaration) binding).FPS;
            } else {
                FPS = ((ProcFormalParameter) binding).FPS;
            }
            if (!FPS.equals(((ProcFormalParameter) fp).FPS)) {
                reporter.reportError("wrong signature for procedure \"%\"",
                        ast.I.spelling, ast.I.position);
            }
        }
        return null;
    }

    @Override
    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
        FormalParameter fp = (FormalParameter) o;

        TypeDenoter vType = (TypeDenoter) ast.V.visit(this, null);
        if (!ast.V.variable) {
            reporter.reportError("actual parameter is not a variable", "",
                    ast.V.position);
        } else if (!(fp instanceof VarFormalParameter)) {
            reporter.reportError("var actual parameter not expected here", "",
                    ast.V.position);
        } else if (!vType.equals(((VarFormalParameter) fp).T)) {
            reporter.reportError("wrong type for var actual parameter", "",
                    ast.V.position);
        }
        return null;
    }

    @Override
    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
        FormalParameterSequence fps = (FormalParameterSequence) o;
        if (!(fps instanceof EmptyFormalParameterSequence)) {
            reporter.reportError("too few actual parameters", "", ast.position);
        }
        return null;
    }

    @Override
    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
        FormalParameterSequence fps = (FormalParameterSequence) o;
        if (!(fps instanceof MultipleFormalParameterSequence)) {
            reporter.reportError("too many actual parameters", "", ast.position);
        } else {
            ast.AP.visit(this, ((MultipleFormalParameterSequence) fps).FP);
            ast.APS.visit(this, ((MultipleFormalParameterSequence) fps).FPS);
        }
        return null;
    }

    @Override
    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
        FormalParameterSequence fps = (FormalParameterSequence) o;
        if (!(fps instanceof SingleFormalParameterSequence)) {
            reporter.reportError("incorrect number of actual parameters", "", ast.position);
        } else {
            ast.AP.visit(this, ((SingleFormalParameterSequence) fps).FP);
        }
        return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Type Denoters">
    // These return the expanded version of the TypeDenoter. Don't use
    // the given object
    @Override
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        return StdEnvironment.anyType;
    }

    @Override
    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        if (Integer.parseInt(ast.IL.spelling) == 0) {
            reporter.reportError("arrays must not be empty", "", ast.IL.position);
        }
        return ast;
    }

    @Override
    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        return StdEnvironment.booleanType;
    }

    @Override
    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        return StdEnvironment.charType;
    }

    @Override
    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
        return StdEnvironment.errorType;
    }

    @Override
    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast, Object o) {
        Declaration binding = (Declaration) ast.I.visit(this, null);
        if (binding == null) {
            reportUndeclared(ast.I);
            return StdEnvironment.errorType;
        }
        if (!(binding instanceof TypeDeclaration)) {
            reporter.reportError("\"%\" is not a type identifier",
                    ast.I.spelling, ast.I.position);
            return StdEnvironment.errorType;
        }
        return ((TypeDeclaration) binding).T;
    }

    @Override
    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
        return StdEnvironment.integerType;
    }

    @Override
    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
        ast.FT = (FieldTypeDenoter) ast.FT.visit(this, null);
        return ast;
    }

    @Override
    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        ast.FT.visit(this, null);
        return ast;
    }

    @Override
    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Object o) {
        ast.T = (TypeDenoter) ast.T.visit(this, null);
        return ast;
    }
    
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Literals, Identifiers and Operators">
    @Override
    public Object visitCharacterLiteral(CharacterLiteral CL, Object o) {
        return StdEnvironment.charType;
    }

    @Override
    public Object visitIdentifier(Identifier I, Object o) {
        Declaration binding = idTable.retrieve(I.spelling);
        if (binding != null) {
            I.decl = binding;
        }
        return binding;
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteral IL, Object o) {
        return StdEnvironment.integerType;
    }

    @Override
    public Object visitOperator(Operator O, Object o) {
        Declaration binding;
        if (o != null) {
            binding = idTable.retrieve(O.spelling, (Class) o);
        } else {
            binding = idTable.retrieve(O.spelling);
        }
        if (binding != null) {
            O.decl = binding;
        }
        return binding;
    }
    
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Value-or-variable names">
    // Determines the address of a named object (constant or variable).
    // This consists of a base object, to which 0 or more field-selection
    // or array-indexing operations may be applied (if it is a record or
    // array).  As much as possible of the address computation is done at
    // compile-time. Code is generated only when necessary to evaluate
    // index expressions at run-time.
    // currentLevel is the routine level where the v-name occurs.
    // frameSize is the anticipated size of the local stack frame when
    // the object is addressed at run-time.
    // It returns the description of the base object.
    // offset is set to the total of any field offsets (plus any offsets
    // due to index expressions that happen to be literals).
    // indexed is set to true iff there are any index expressions (other
    // than literals). In that case code is generated to compute the
    // offset due to these indexing operations at run-time.
    // Returns the TypeDenoter of the Vname. Does not use the
    // given object.
    @Override
    public Object visitDotVname(DotVname ast, Object o) {
        ast.type = null;
        TypeDenoter vType = (TypeDenoter) ast.V.visit(this, null);
        ast.variable = ast.V.variable;
        if (!(vType instanceof RecordTypeDenoter)) {
            reporter.reportError("record expected here", "", ast.V.position);
        } else {
            ast.type = checkFieldIdentifier(((RecordTypeDenoter) vType).FT, ast.I);
            if (ast.type == StdEnvironment.errorType) {
                reporter.reportError("no field \"%\" in this record type",
                        ast.I.spelling, ast.I.position);
            }
        }
        return ast.type;
    }

    @Override
    public Object visitSimpleVname(SimpleVname ast, Object o) {
        ast.variable = false;
        ast.type = StdEnvironment.errorType;
        Declaration binding = (Declaration) ast.I.visit(this, null);
        if (binding == null) {
            reportUndeclared(ast.I);
        } else if (binding instanceof ConstDeclaration) {
            ast.type = ((ConstDeclaration) binding).E.type;
            //ast.variable = false;
        } else if (binding instanceof ConstFormalParameter) {
            ast.type = ((ConstFormalParameter) binding).T;
            //ast.variable = false;
        } else if (binding instanceof VarDeclaration) {
            ast.type = ((VarDeclaration) binding).T;
            ast.variable = true;
        } else if (binding instanceof VarFormalParameter) {
            ast.type = ((VarFormalParameter) binding).T;
            ast.variable = true;
        } else {
            reporter.reportError("\"%\" is not a const or var identifier",
                    ast.I.spelling, ast.I.position);
        }
        return ast.type;
    }

    @Override
    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        TypeDenoter vType = (TypeDenoter) ast.V.visit(this, null);
        ast.variable = ast.V.variable;
        TypeDenoter eType = (TypeDenoter) ast.E.visit(this, null);
        if (vType != StdEnvironment.errorType) {
            if (!(vType instanceof ArrayTypeDenoter)) {
                reporter.reportError("array expected here", "", ast.V.position);
            } else {
                if (eType != StdEnvironment.integerType) {
                    reporter.reportError("Integer expression expected here", "",
                            ast.E.position);
                }
                ast.type = ((ArrayTypeDenoter) vType).T;
            }
        }
        return ast.type;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Auxiliar Methods">
    // Programs
    @Override
    public Object visitProgram(Program ast, Object o) {
        ast.C.visit(this, null);
        return null;
    }

    // Checks whether the source program, represented by its AST, satisfies the
    // language's scope rules and type rules.
    // Also decorates the AST as follows:
    //  (a) Each applied occurrence of an identifier or operator is linked to
    //      the corresponding declaration of that identifier or operator.
    //  (b) Each expression and value-or-variable-name is decorated by its type.
    //  (c) Each type identifier is replaced by the type it denotes.
    // Types are represented by small ASTs.
    public void check(Program ast) {
        ast.visit(this, null);
    }

    /////////////////////////////////////////////////////////////////////////////
    public Checker(ErrorReporter reporter) {
        this.reporter = reporter;
        this.idTable = new IdentificationTable();
        establishStdEnvironment();
    }

    private IdentificationTable idTable;
    private static final SourcePosition DUMMYPOS = new SourcePosition();
    private final ErrorReporter reporter;

    // Reports that the identifier or operator used at a leaf of the AST
    // has not been declared.
    private void reportUndeclared(Terminal leaf) {
        reporter.reportError("\"%\" is not declared", leaf.spelling, leaf.position);
    }

    private static TypeDenoter checkFieldIdentifier(FieldTypeDenoter ast, Identifier I) {
        if (ast instanceof MultipleFieldTypeDenoter) {
            MultipleFieldTypeDenoter ft = (MultipleFieldTypeDenoter) ast;
            if (ft.I.spelling.equals(I.spelling)) {
                I.decl = ast;
                return ft.T;
            } else {
                return checkFieldIdentifier(ft.FT, I);
            }
        } else if (ast instanceof SingleFieldTypeDenoter) {
            SingleFieldTypeDenoter ft = (SingleFieldTypeDenoter) ast;
            if (ft.I.spelling.compareTo(I.spelling) == 0) {
                I.decl = ast;
                return ft.T;
            }
        }
        return StdEnvironment.errorType;
    }

    /**
     * Will be called in the moment a new identifier has been declared, and then
     * this will remove and then visit any pending calls in the current identifier
     * level. Note that some variables may have been declared between the pendingCall
     * being made and the identifier being declared, thus the revert and change of the
     * identification table
     */
    private void visitPendingCalls(Identifier I) {
        ArrayList<PendingCall> pendingCalls = idTable.checkPendingCalls(I);
        IdentificationTable currentIdTable = this.idTable;

        pendingCalls.forEach(pC -> {
            Declaration procFuncDecl = (Declaration) pC.getProcFuncIdentifier().visit(this, null);
            this.idTable = pC.getCallContextIdTable(); //Sets the Id Table as how it was in the moment of the call
            pC.visitPendingCall(this, procFuncDecl); //Visit each of them. Pass the visit of the proc to bind it to the call
            this.idTable = currentIdTable; //Sets the id table back
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Standard Environment">
    // Creates a small AST to represent the "declaration" of a standard
    // type, and enters it in the identification table.
    private TypeDeclaration declareStdType(String id, TypeDenoter typedenoter) {
        TypeDeclaration binding = new TypeDeclaration(new Identifier(id, DUMMYPOS),
                                                        typedenoter,
                                                        DUMMYPOS);
        idTable.enter(id, binding);
        return binding;
    }

    // Creates a small AST to represent the "declaration" of a standard
    // constant, and enters it in the identification table.
    private ConstDeclaration declareStdConst(String id, TypeDenoter constType) {
        // constExpr used only as a placeholder for constType
        IntegerExpression constExpr = new IntegerExpression(null, DUMMYPOS);
        constExpr.type = constType;
        
        ConstDeclaration binding = new ConstDeclaration(new Identifier(id, DUMMYPOS),
                                                            constExpr,
                                                            DUMMYPOS);
        idTable.enter(id, binding);
        return binding;
    }

    // Creates a small AST to represent the "declaration" of a standard
    // procedure, and enters it in the identification table.
    private ProcDeclaration declareStdProc(String id, FormalParameterSequence fps) {

        ProcDeclaration binding;

        binding = new ProcDeclaration(new Identifier(id, DUMMYPOS), fps,
                new EmptyCommand(DUMMYPOS), DUMMYPOS);
        idTable.enter(id, binding);
        return binding;
    }

    // Creates a small AST to represent the "declaration" of a standard
    // function, and enters it in the identification table.
    private FuncDeclaration declareStdFunc(String id, FormalParameterSequence fps,
            TypeDenoter resultType) {

        FuncDeclaration binding;

        binding = new FuncDeclaration(new Identifier(id, DUMMYPOS), fps, resultType,
                new EmptyExpression(DUMMYPOS), DUMMYPOS);
        idTable.enter(id, binding);
        return binding;
    }

    // Creates a small AST to represent the "declaration" of a
    // unary operator, and enters it in the identification table.
    // This "declaration" summarises the operator's type info.
    private UnaryOperatorDeclaration declareStdUnaryOp(String op, TypeDenoter argType, TypeDenoter resultType) {

        UnaryOperatorDeclaration binding;

        binding = new UnaryOperatorDeclaration(new Operator(op, DUMMYPOS),
                argType, resultType, DUMMYPOS);
        idTable.enter(op, binding);
        return binding;
    }

    // Creates a small AST to represent the "declaration" of a
    // binary operator, and enters it in the identification table.
    // This "declaration" summarises the operator's type info.
    private BinaryOperatorDeclaration declareStdBinaryOp(String op, TypeDenoter arg1Type, TypeDenoter arg2type, TypeDenoter resultType) {

        BinaryOperatorDeclaration binding;

        binding = new BinaryOperatorDeclaration(new Operator(op, DUMMYPOS),
                arg1Type, arg2type, resultType, DUMMYPOS);
        idTable.enter(op, binding);
        return binding;
    }

    // Creates small ASTs to represent the standard types.
    // Creates small ASTs to represent "declarations" of standard types,
    // constants, procedures, functions, and operators.
    // Enters these "declarations" in the identification table.
    private final static Identifier DUMMYIDENTIFIER = new Identifier("", DUMMYPOS);

    private void establishStdEnvironment() {

        // idTable.startIdentification();
        StdEnvironment.booleanType = new BoolTypeDenoter(DUMMYPOS);
        StdEnvironment.integerType = new IntTypeDenoter(DUMMYPOS);
        StdEnvironment.charType = new CharTypeDenoter(DUMMYPOS);
        StdEnvironment.anyType = new AnyTypeDenoter(DUMMYPOS);
        StdEnvironment.errorType = new ErrorTypeDenoter(DUMMYPOS);

        StdEnvironment.booleanDecl = declareStdType("Boolean", StdEnvironment.booleanType);
        StdEnvironment.falseDecl = declareStdConst("false", StdEnvironment.booleanType);
        StdEnvironment.trueDecl = declareStdConst("true", StdEnvironment.booleanType);
        StdEnvironment.notDecl = declareStdUnaryOp("\\", StdEnvironment.booleanType, StdEnvironment.booleanType);
        StdEnvironment.negDecl = declareStdUnaryOp("-", StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.andDecl = declareStdBinaryOp("/\\", StdEnvironment.booleanType, StdEnvironment.booleanType, StdEnvironment.booleanType);
        StdEnvironment.orDecl = declareStdBinaryOp("\\/", StdEnvironment.booleanType, StdEnvironment.booleanType, StdEnvironment.booleanType);

        StdEnvironment.integerDecl = declareStdType("Integer", StdEnvironment.integerType);
        StdEnvironment.maxintDecl = declareStdConst("maxint", StdEnvironment.integerType);
        StdEnvironment.addDecl = declareStdBinaryOp("+", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.subtractDecl = declareStdBinaryOp("-", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.multiplyDecl = declareStdBinaryOp("*", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.divideDecl = declareStdBinaryOp("/", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.moduloDecl = declareStdBinaryOp("//", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.integerType);
        StdEnvironment.lessDecl = declareStdBinaryOp("<", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.booleanType);
        StdEnvironment.notgreaterDecl = declareStdBinaryOp("<=", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.booleanType);
        StdEnvironment.greaterDecl = declareStdBinaryOp(">", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.booleanType);
        StdEnvironment.notlessDecl = declareStdBinaryOp(">=", StdEnvironment.integerType, StdEnvironment.integerType, StdEnvironment.booleanType);

        StdEnvironment.charDecl = declareStdType("Char", StdEnvironment.charType);
        StdEnvironment.chrDecl = declareStdFunc("chr", new SingleFormalParameterSequence(
                new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS), DUMMYPOS), StdEnvironment.charType);
        StdEnvironment.ordDecl = declareStdFunc("ord", new SingleFormalParameterSequence(
                new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.charType, DUMMYPOS), DUMMYPOS), StdEnvironment.integerType);
        StdEnvironment.eofDecl = declareStdFunc("eof", new EmptyFormalParameterSequence(DUMMYPOS), StdEnvironment.booleanType);
        StdEnvironment.eolDecl = declareStdFunc("eol", new EmptyFormalParameterSequence(DUMMYPOS), StdEnvironment.booleanType);
        StdEnvironment.getDecl = declareStdProc("get", new SingleFormalParameterSequence(
                new VarFormalParameter(DUMMYIDENTIFIER, StdEnvironment.charType, DUMMYPOS), DUMMYPOS));
        StdEnvironment.putDecl = declareStdProc("put", new SingleFormalParameterSequence(
                new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.charType, DUMMYPOS), DUMMYPOS));
        StdEnvironment.getintDecl = declareStdProc("getint", new SingleFormalParameterSequence(
                new VarFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS), DUMMYPOS));
        StdEnvironment.putintDecl = declareStdProc("putint", new SingleFormalParameterSequence(
                new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS), DUMMYPOS));
        StdEnvironment.geteolDecl = declareStdProc("geteol", new EmptyFormalParameterSequence(DUMMYPOS));
        StdEnvironment.puteolDecl = declareStdProc("puteol", new EmptyFormalParameterSequence(DUMMYPOS));
        StdEnvironment.equalDecl = declareStdBinaryOp("=", StdEnvironment.anyType, StdEnvironment.anyType, StdEnvironment.booleanType);
        StdEnvironment.unequalDecl = declareStdBinaryOp("\\=", StdEnvironment.anyType, StdEnvironment.anyType, StdEnvironment.booleanType);
    }

    // </editor-fold>
}
