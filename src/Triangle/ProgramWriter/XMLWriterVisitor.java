package Triangle.ProgramWriter;

import Triangle.AbstractSyntaxTrees.*;

import java.io.FileWriter;
import java.io.IOException;

public class XMLWriterVisitor implements Visitor {

    private final FileWriter fileWriter;

    public XMLWriterVisitor(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Programs ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // PROGRAMS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitProgram(Program ast, Object o) {
      writeToXMLFile("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
      writeToXMLFile("<Program>\n");
        ast.C.visit(this, null);
      writeToXMLFile("</Program>\n");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Commands ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // COMMANDS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitAssignCommand(AssignCommand ast, Object o) {
      writeToXMLFile("<AssignCommand>\n");
        ast.V.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</AssignCommand>\n");
      return null;
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
      writeToXMLFile("<CallCommand>\n");
        ast.I.visit(this, null);
        ast.APS.visit(this, null);
      writeToXMLFile("</CallCommand>\n");
      return null;
    }

    @Override
    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
      writeToXMLFile("<EmptyCommand/>\n");
      return null;
    }

    @Override
    public Object visitIfCommand(IfCommand ast, Object o) {
      writeToXMLFile("<IfCommand>\n");
        ast.E.visit(this, null);
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
      writeToXMLFile("</IfCommand>\n");
      return null;
    }

    @Override
    public Object visitLetCommand(LetCommand ast, Object o) {
      writeToXMLFile("<LetCommand>\n");
        ast.D.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</LetCommand>\n");
      return null;
    }

    @Override
    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
      writeToXMLFile("<SequentialCommand>\n");
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
      writeToXMLFile("</SequentialCommand>\n");
        return null;
    }

    @Override
    public Object visitWhileLoopCommand(WhileLoopCommand ast, Object o) {
      writeToXMLFile("<WhileLoopCommand>\n");
        ast.E.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</WhileLoopCommand>\n");
        return null;
    }

    @Override
    public Object visitDoWhileLoopCommand(DoWhileLoopCommand ast, Object o) {
      writeToXMLFile("<Do-WhileLoopCommand>\n");
        ast.E.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</Do-WhileLoopCommand>\n");
      return null;
    }

    @Override
    public Object visitUntilLoopCommand(UntilLoopCommand ast, Object o) {
      writeToXMLFile("<UntilLoopCommand>\n");
        ast.E.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</UntilLoopCommand>\n");
      return null;
    }

    @Override
    public Object visitDoUntilLoopCommand(DoUntilLoopCommand ast, Object o) {
      writeToXMLFile("<Do-UntilLoopCommand>\n");
        ast.E.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</Do-UntilLoopCommand>\n");
      return null;
    }

    @Override
    public Object visitForLoopCommand(ForLoopCommand ast, Object o) {
      writeToXMLFile("<ForLoopCommand>\n");
        ast.InitialDeclaration.visit(this, null);
        ast.HaltingExpression.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</ForLoopCommand>\n");
      return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Expressions ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
      writeToXMLFile("<ArrayExpression>\n");
        ast.AA.visit(this, null);
      writeToXMLFile("</ArrayExpression>\n");
      return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
      writeToXMLFile("<BinaryExpression>\n");
        ast.E1.visit(this, null);
        ast.O.visit(this, null);
        ast.E2.visit(this, null);        
      writeToXMLFile("</BinaryExpression>\n");
      return null;
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
      writeToXMLFile("<CallExpression>\n");
        ast.I.visit(this, null);
        ast.APS.visit(this, null);
      writeToXMLFile("</CallExpression>\n");
      return null;
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
      writeToXMLFile("<CharacterExpression>\n");
        ast.CL.visit(this, null);
      writeToXMLFile("</CharacterExpression>\n");
      return null;
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
      writeToXMLFile("<EmptyExpression/>\n");
      return null;
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
      writeToXMLFile("<IfExpression>\n");
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        ast.E3.visit(this, null);
      writeToXMLFile("</IfExpression>\n");
      return null;
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
      writeToXMLFile("<IntegerExpression>\n");
        ast.IL.visit(this, null);
      writeToXMLFile("</IntegerExpression>\n");
      return null;
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
      writeToXMLFile("<LetExpression>\n");
        ast.D.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</LetExpression>\n");
      return null;
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
      writeToXMLFile("<RecordExpression>\n");
        ast.RA.visit(this, null);
      writeToXMLFile("</RecordExpression>\n");
      return null;
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
      writeToXMLFile("<UnaryExpression>\n");
        ast.O.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</UnaryExpression>\n");
      return null;
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
      writeToXMLFile("<VnameExpression>\n");
        ast.V.visit(this, null);
      writeToXMLFile("</VnameExpression>\n");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Declarations ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // DECLARATIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o) {
      writeToXMLFile("BinaryOperatorDeclaration>\n");
        ast.ARG1.visit(this, null);
        ast.O.visit(this, null);
        ast.ARG2.visit(this, null);
        ast.RES.visit(this, null);
      writeToXMLFile("</BinaryOperatorDeclaration>\n");
      return null;
    }

    @Override
    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
      writeToXMLFile("<ConstantDeclaration>\n");
        ast.I.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</ConstantDeclaration>\n");
      return null;
    }

    @Override
    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
      writeToXMLFile("<FunctionDeclaration>\n");
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
        ast.T.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</FunctionDeclaration>\n");
      return null;
    }

    @Override
    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
      writeToXMLFile("<ProcedureDeclaration>\n");
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
        ast.C.visit(this, null);
      writeToXMLFile("</ProcedureDeclaration>\n");
      return null;
    }

    @Override
    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
      writeToXMLFile("<SequentialDeclaration>\n");
        ast.D1.visit(this, null);
        ast.D2.visit(this, null);
      writeToXMLFile("</SequentialDeclaration>\n");
      return null;
    }

    @Override
    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
      writeToXMLFile("<TypeDeclaration>\n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</TypeDeclaration>\n");
      return null;
    }

    @Override
    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
      writeToXMLFile("<UnaryOperatorDeclaration>\n");
        ast.ARG.visit(this, null);
        ast.O.visit(this, null);
        ast.RES.visit(this, null);
      writeToXMLFile("</UnaryOperatorDeclaration>\n");
      return null;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration ast, Object o) {
      writeToXMLFile("<VariableDeclaration>\n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</VariableDeclaration>\n");
      return null;
    }
    
    @Override
    public Object visitVarDeclarationInitialized(VarDeclarationInitialized ast, Object o) {
      writeToXMLFile("<VarDeclarationInitialized>\n");
        ast.I.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</VarDeclarationInitialized>\n");
      return null;
    }

    @Override
    public Object visitRecursiveDeclaration(RecursiveDeclaration ast, Object o) {
      writeToXMLFile("<RecursiveDeclaration>\n");
        ast.D.visit(this, null);
      writeToXMLFile("</RecursiveDeclaration>\n");
      return null;
    }

    @Override
    public Object visitLocalDeclaration(LocalDeclaration ast, Object o) {
      writeToXMLFile("<LocalDeclaration>\n");
        ast.dAST1.visit(this, null);
        ast.dAST2.visit(this, null);
      writeToXMLFile("</LocalDeclaration>\n");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Aggregates ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // ARRAY AGGREGATES
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast, Object o) {
      writeToXMLFile("<MultipleArrayAggregate>\n");
        ast.E.visit(this, null);
        ast.AA.visit(this, null);
      writeToXMLFile("</MultipleArrayAggregate>\n");
      return null;
    }

    @Override
    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
      writeToXMLFile("<SingleArrayAggregate>\n");
        ast.E.visit(this, null);
      writeToXMLFile("</SingleArrayAggregate>\n");
      return null;
    }

    @Override
    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast, Object o) {
      writeToXMLFile("<MultipleRecordAggregate>\n");
        ast.I.visit(this, null);
        ast.E.visit(this, null);
        ast.RA.visit(this, null);
      writeToXMLFile("</MultipleRecordAggregate>\n");
      return null;
    }

    @Override
    public Object visitSingleRecordAggregate(SingleRecordAggregate ast, Object o) {
      writeToXMLFile("<SingleRecordAggregate>\n");
        ast.I.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</SingleRecordAggregate\n>");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Parameters ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // PARAMETERS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
      writeToXMLFile("<ConstantFormalParameters>/n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</ConstantFormalParameters>/n");
      return null;
    }

    @Override
    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
      writeToXMLFile("<FunctionFormalParameter>\n");
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</FunctionFormalParameter>\n");
      return null;
    }

    @Override
    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
      writeToXMLFile("<ProcedureFormalParameter>\n");
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
      writeToXMLFile("</ProcedureFormalParameter>\n");
      return null;
    }

    @Override
    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
      writeToXMLFile("<VariableFormalParameter>\n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</VariableFormalParameter>\n");
      return null;
    }

    @Override
    public Object visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Object o) {
      writeToXMLFile("<EmptyFormalParameterSequence/>\n");
      return null;
    }

    @Override
    public Object visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Object o) {
      writeToXMLFile("<MultipleFormalParameterSequence>\n");
        ast.FP.visit(this, null);
        ast.FPS.visit(this, null);
      writeToXMLFile("</MultipleFormalParameterSequence>\n");
      return null;
    }

    @Override
    public Object visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Object o) {
      writeToXMLFile("<SingleFormalParameterSequence>\n");
        ast.FP.visit(this, null);
      writeToXMLFile("</SingleFormalParameterSequence>\n");
      return null;
    }

    @Override
    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
      writeToXMLFile("<ConstantActualParameter>\n");
        ast.E.visit(this, null);
      writeToXMLFile("</ConstantActualParameter>\n");
      return null;
    }

    @Override
    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
      writeToXMLFile("<FunctionActualParameter>\n");
        ast.I.visit(this, null);
      writeToXMLFile("</FunctionActualParameter>\n");
      return null;
    }

    @Override
    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
      writeToXMLFile("<ProcedureActualParameter>\n");
        ast.I.visit(this, null);
      writeToXMLFile("</ProcedureActualParameter>\n");
      return null;
    }

    @Override
    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
      writeToXMLFile("<VariableActualParameter>\n");
        ast.V.visit(this, null);
      writeToXMLFile("</VariableActualParameter>\n");
      return null;
    }

    @Override
    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
      writeToXMLFile("<EmptyActualParameterSequence/>\n");
      return null;
    }

    @Override
    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
      writeToXMLFile("<MultipleActualParameterSequence>\n");
        ast.AP.visit(this, null);
        ast.APS.visit(this, null);
      writeToXMLFile("</MultipleActualParameterSequence>\n");
      return null;
    }

    @Override
    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
      writeToXMLFile("<SingleActualParameterSequence>\n");
        ast.AP.visit(this, null);
      writeToXMLFile("</SingleActualParameterSequence>\n");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Type Denoters ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TYPE-DENOTERS
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
      writeToXMLFile("<AnyTypeDenoter/>\n");
      return null;
    }

    @Override
    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
      writeToXMLFile("<ArrayTypeDenoter>\n");
        ast.IL.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</ArrayTypeDenoter>\n");
      return null;
    }

    @Override
    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
      writeToXMLFile("<BoolTypeDenoter/>\n");
      return null;
    }

    @Override
    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
      writeToXMLFile("<CharTypeDenoter/>\n");
      return null;
    }

    @Override
    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
      writeToXMLFile("<ErrorTypeDenoter/>\n");
      return null;
    }

    @Override
    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast, Object o) {
      writeToXMLFile("<SimpleTypeDenoter>\n");
        ast.I.visit(this, null);
      writeToXMLFile("</SimpleTypeDenoter>\n");
      return null;
    }

    @Override
    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
      writeToXMLFile("<IntTypeDenoter/>\n");
      return null;
    }

    @Override
    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
      writeToXMLFile("<RecordTypeDenoter>\n");
        ast.FT.visit(this, null);
      writeToXMLFile("</RecordTypeDenoter>\n");
      return null;
    }

    @Override
    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Object o) {
      writeToXMLFile("<MultipleFieldTypeDenoter>\n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
        ast.FT.visit(this, null);
      writeToXMLFile("</MultipleFieldTypeDenoter>\n");
      return null;
    }

    @Override
    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Object o) {
      writeToXMLFile("<SingleFieldTypeDenoter>\n");
        ast.I.visit(this, null);
        ast.T.visit(this, null);
      writeToXMLFile("</SingleFieldTypeDenoter>\n");
      return null;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Literals, Identifiers and Operators ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // LITERALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    
    @Override
    public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
      writeToXMLFile("<CharaterLiteral ".concat("value=\"").concat(ast.spelling).concat("\"/>\n"));
      return null;
    }

    @Override
    public Object visitIdentifier(Identifier ast, Object o) {
      writeToXMLFile("<Identifier ".concat("value=\"").concat(ast.spelling).concat("\"/>\n"));
      return null;
    }
    
    @Override
    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
      writeToXMLFile("<IntegerLiteral ".concat("value=\"").concat(ast.spelling).concat("\"/>\n"));
      return null;
    }

    @Override
    public Object visitOperator(Operator ast, Object o) {
      writeToXMLFile("<Operator ".concat("value=\"").concat(ast.spelling).concat("\"/>\n"));
      return null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Values or Variables Names ">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // VALUE-OR-VARIABLE NAMES
    //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object visitDotVname(DotVname ast, Object o) {
      writeToXMLFile("<DotVname>\n");
        ast.I.visit(this, null);
        ast.V.visit(this, null);
      writeToXMLFile("</DotVname>\n");
      return null;
    }

    @Override
    public Object visitSimpleVname(SimpleVname ast, Object o) {
      writeToXMLFile("<SimpleVname>\n");
        ast.I.visit(this, null);
      writeToXMLFile("</SimpleVname>\n");
      return null;
    }

    @Override
    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
      writeToXMLFile("<SubscriptVname>\n");
        ast.V.visit(this, null);
        ast.E.visit(this, null);
      writeToXMLFile("</SubscriptVname>\n");
      return null;
    }

    // </editor-fold>

    private void writeToXMLFile(String content){
        try {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
