/*
 * IDE-Triangle v1.0
 * TableDetails.java
 */
package Core.Visitors;

import Triangle.AbstractSyntaxTrees.*;
import Triangle.CodeGenerator.Field;
import Triangle.CodeGenerator.KnownAddress;
import Triangle.CodeGenerator.KnownAddressWithValue;
import Triangle.CodeGenerator.KnownRoutine;
import Triangle.CodeGenerator.KnownValue;
import Triangle.CodeGenerator.UnknownAddress;
import Triangle.CodeGenerator.UnknownRoutine;
import Triangle.CodeGenerator.UnknownValue;

import javax.swing.table.DefaultTableModel;

/**
 * Implements the Triangle Visitor interface, which is used to visit an entire
 * AST.
 *
 * Generates a DefaultTableModel, used to draw a JTable.
 *
 * @author Luis Leopoldo Perez <luiperpe@ns.isi.ulatina.ac.cr>
 */
public class TableVisitor implements Visitor {

    /**
     * Creates a new instance of TableDetails
     */
    public TableVisitor() {
    }

    // <editor-fold defaultstate="collapsed" desc=" Commands ">
    // Commands
    @Override
    public Object visitAssignCommand(AssignCommand ast, Object o) {
        ast.V.visit(this, null);
        ast.E.visit(this, null);

        return (null);
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
        ast.I.visit(this, null);
        ast.APS.visit(this, null);

        return (null);
    }

    @Override
    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return (null);
    }

    @Override
    public Object visitIfCommand(IfCommand ast, Object o) {
        ast.E.visit(this, null);
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);

        return (null);
    }

    @Override
    public Object visitLetCommand(LetCommand ast, Object o) {
        ast.D.visit(this, null);
        ast.C.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);

        return (null);
    }

    @Override
    public Object visitForLoopCommand(ForLoopCommand ast, Object o) {
        ast.InitialDeclaration.visit(this, null);
        ast.HaltingExpression.visit(this, null);
        ast.C.visit(this, null);

        return (null);
    }

    @Override
    public Object visitWhileLoopCommand(WhileLoopCommand ast, Object o) {
        ast.E.visit(this, null);
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Object visitDoWhileLoopCommand(DoWhileLoopCommand ast, Object o) {
        ast.C.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Object visitUntilLoopCommand(UntilLoopCommand ast, Object o) {
        ast.E.visit(this, null);
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Object visitDoUntilLoopCommand(DoUntilLoopCommand ast, Object o) {
        ast.C.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Expressions ">
    // Expressions
    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        ast.AA.visit(this, null);

        return (null);
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        ast.O.visit(this, null);

        return (null);
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
        ast.I.visit(this, null);
        ast.APS.visit(this, null);

        return (null);
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        ast.CL.visit(this, null);

        return (null);
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return (null);
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        ast.E3.visit(this, null);

        return (null);
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        return (null);
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
        ast.D.visit(this, null);
        ast.E.visit(this, null);

        return (null);
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
        ast.RA.visit(this, null);

        return (null);
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        ast.E.visit(this, null);
        ast.O.visit(this, null);

        return (null);
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
        ast.V.visit(this, null);

        return (null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Declarations ">
    // Declarations
    @Override
    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o) {
        return (null);
    }

    @Override
    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
        String name = ast.I.spelling;
        String type = "N/A";
        try {
            int size = (ast.entity != null ? ast.entity.size : 0);
            int level = -1;
            int displacement = -1;
            int value = -1;

            if (ast.entity instanceof KnownValue) {
                type = "KnownValue";
                value = ((KnownValue) ast.entity).value;
            } else if (ast.entity instanceof UnknownValue) {
                type = "UnknownValue";
                level = ((UnknownValue) ast.entity).address.level;
                displacement = ((UnknownValue) ast.entity).address.displacement;
            }
            addIdentifier(name, type, size, level, displacement, value);
        } catch (NullPointerException e) {
        }

        ast.E.visit(this, null);
        ast.I.visit(this, null);

        return (null);
    }

    @Override
    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "KnownRoutine",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((KnownRoutine) ast.entity).address.level,
                    ((KnownRoutine) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }
        ast.T.visit(this, null);
        ast.FPS.visit(this, null);
        ast.E.visit(this, null);

        return (null);
    }

    @Override
    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
        try {
            addIdentifier(ast.I.spelling, "KnownRoutine",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((KnownRoutine) ast.entity).address.level,
                    ((KnownRoutine) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }

        ast.FPS.visit(this, null);
        ast.C.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
        ast.D1.visit(this, null);
        ast.D2.visit(this, null);

        return (null);
    }

    @Override
    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
        return (null);
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "KnownAddress",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((KnownAddress) ast.entity).address.level,
                    ((KnownAddress) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }

        ast.T.visit(this, null);
        return (null);
    }

    @Override
    public Object visitVarDeclarationInitialized(VarDeclarationInitialized ast, Object o) {
        try {
            if (ast.entity instanceof KnownAddressWithValue) {
                addIdentifier(ast.I.spelling,
                        "KnownAddressWithValue",
                        (ast.entity != null ? ast.entity.size : 0),
                        ((KnownAddressWithValue) ast.entity).address.level,
                        ((KnownAddressWithValue) ast.entity).address.displacement,
                        ((KnownAddressWithValue) ast.entity).value);
            } else if (ast.entity instanceof KnownAddress) {
                addIdentifier(ast.I.spelling,
                        "KnownAddress",
                        (ast.entity != null ? ast.entity.size : 0),
                        ((KnownAddress) ast.entity).address.level,
                        ((KnownAddress) ast.entity).address.displacement,
                        -1);
            }

        } catch (NullPointerException e) {
        }

        ast.T.visit(this, null);
        return (null);
    }

    @Override
    public Object visitRecursiveDeclaration(RecursiveDeclaration ast, Object o) {
        ast.D.visit(this, null);
        return null;
    }

    @Override
    public Object visitLocalDeclaration(LocalDeclaration ast, Object o) {
        ast.dAST1.visit(this, null);
        ast.dAST2.visit(this, null);
        return (null);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Aggregates ">
    // Array Aggregates
    @Override
    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast, Object o) {
        ast.AA.visit(this, null);
        ast.E.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
        ast.E.visit(this, null);

        return (null);
    }

    // Record Aggregates
    @Override
    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast, Object o) {
        ast.E.visit(this, null);
        ast.I.visit(this, null);
        ast.RA.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSingleRecordAggregate(SingleRecordAggregate ast, Object o) {
        ast.E.visit(this, null);
        ast.I.visit(this, null);

        return (null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Parameters ">
    // Formal Parameters
    @Override
    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "UnknownValue",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((UnknownValue) ast.entity).address.level,
                    ((UnknownValue) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }

        ast.I.visit(this, null);
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "UnknownRoutine",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((UnknownRoutine) ast.entity).address.level,
                    ((UnknownRoutine) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }
        ast.FPS.visit(this, null);
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "UnknownRoutine",
                    (ast.entity != null ? ast.entity.size : 0),
                    ((UnknownRoutine) ast.entity).address.level,
                    ((UnknownRoutine) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }
        ast.FPS.visit(this, null);

        return (null);
    }

    @Override
    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "UnknownAddress",
                    ast.T.entity.size,
                    ((UnknownAddress) ast.entity).address.level,
                    ((UnknownAddress) ast.entity).address.displacement,
                    -1);
        } catch (NullPointerException e) {
        }
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Object o) {
        return (null);
    }

    @Override
    public Object visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Object o) {
        ast.FP.visit(this, null);
        ast.FPS.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Object o) {
        ast.FP.visit(this, null);

        return (null);
    }

    // Actual Parameters
    @Override
    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
        ast.E.visit(this, null);

        return (null);
    }

    @Override
    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
        ast.I.visit(this, null);

        return (null);
    }

    @Override
    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
        ast.I.visit(this, null);

        return (null);
    }

    @Override
    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
        ast.V.visit(this, null);

        return (null);
    }

    @Override
    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
        return (null);
    }

    @Override
    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
        ast.AP.visit(this, null);
        ast.APS.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
        ast.AP.visit(this, null);

        return (null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Type Denoters ">
    // Type Denoters
    @Override
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        return (null);
    }

    @Override
    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        ast.IL.visit(this, null);
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        return (null);
    }

    @Override
    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        return (null);
    }

    @Override
    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
        return (null);
    }

    @Override
    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast, Object o) {
        ast.I.visit(this, null);
        return (null);
    }

    @Override
    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
        return (null);
    }

    @Override
    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
        ast.FT.visit(this, null);
        return (null);
    }

    @Override
    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "Field",
                    (ast.entity != null ? ast.entity.size : 0),
                    -1, ((Field) ast.entity).fieldOffset, -1);
        } catch (NullPointerException e) {
        }
        ast.FT.visit(this, null);
        ast.I.visit(this, null);
        ast.T.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Object o) {
        try {
            addIdentifier(ast.I.spelling,
                    "Field",
                    (ast.entity != null ? ast.entity.size : 0),
                    -1, ((Field) ast.entity).fieldOffset, -1);
        } catch (NullPointerException e) {
        }
        ast.I.visit(this, null);
        ast.T.visit(this, null);

        return (null);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Literals, Identifiers and Operators ">
    // Literals, Identifiers and Operators
    @Override
    public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
        return (null);
    }

    @Override
    public Object visitIdentifier(Identifier ast, Object o) {
        return (null);
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return (null);
    }

    @Override
    public Object visitOperator(Operator ast, Object o) {
        ast.decl.visit(this, null);

        return (null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Values or Variable Names ">
    // Value-or-variable names
    @Override
    public Object visitDotVname(DotVname ast, Object o) {
        ast.I.visit(this, null);
        ast.V.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSimpleVname(SimpleVname ast, Object o) {
        ast.I.visit(this, null);

        return (null);
    }

    @Override
    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        ast.E.visit(this, null);
        ast.V.visit(this, null);

        return (null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Table Creation Methods ">
    // Programs
    @Override
    public Object visitProgram(Program ast, Object o) {
        ast.C.visit(this, null);

        return (null);
    }

    /**
     * Adds an identifier to the table.
     */
    private void addIdentifier(String name, String type, int size, int level, int displacement, int value) {
        boolean exists = false;

        for (int i = 0; (i < model.getRowCount() && !exists); i++) {
            if (((String) model.getValueAt(i, 0)).compareTo(name) == 0) {
                exists = true;
            }
        }

        if (!exists) {
            model.addRow(new String[]{name,
                type,
                String.valueOf(size),
                (level < 0 ? " " : String.valueOf(level)),
                (displacement < 0 ? " " : String.valueOf(displacement)),
                (value < 0 ? " " : String.valueOf(value))});
        }
    }

    /**
     * Returns the filled table model.
     *
     * @param ast An AST representing the Program Structure
     * @return A new DefaultTableModel with all the variables found in ast
     */
    public DefaultTableModel getTable(Program ast) {
        model = new DefaultTableModel((new String[]{"Name", "Type", "Size", "Level", "Displacement", "Value"}), 0);
        visitProgram(ast, null);

        return (model);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Attributes ">
    private DefaultTableModel model;
    // </editor-fold>
}
