/*
 * @(#)Encoder.java                        2.1 2003/10/07
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

package Triangle.CodeGenerator;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import TAM.Instruction;
import TAM.Machine;
import Triangle.AbstractSyntaxTrees.*;
import Triangle.ErrorReporter;
import Triangle.StdEnvironment;
import Triangle.SyntacticAnalyzer.SourcePosition;

public final class Encoder implements Visitor {

  // <editor-fold defaultstate="collapsed" desc="Commands">

  @Override
  public Object visitAssignCommand(AssignCommand ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.E.visit(this, frame);
    encodeStore(ast.V, new Frame (frame, valSize), valSize);
    return null;
  }

  @Override
  public Object visitCallCommand(CallCommand ast, Object o) {
    Frame frame = (Frame) o;
    Integer argsSize = (Integer) ast.APS.visit(this, frame);
    ast.I.visit(this, new Frame(frame.level, argsSize));
    return null;
  }

  @Override
  public Object visitEmptyCommand(EmptyCommand ast, Object o) {
    return null;
  }

  @Override
  public Object visitIfCommand(IfCommand ast, Object o) {
    Frame frame = (Frame) o;
    int jumpifAddr, jumpAddr;
    ast.E.visit(this, frame);
    jumpifAddr = nextInstrAddr;
    emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, 0);
    ast.C1.visit(this, frame);
    jumpAddr = nextInstrAddr;
    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    patchD(jumpifAddr, nextInstrAddr);
    ast.C2.visit(this, frame);
    patchD(jumpAddr, nextInstrAddr);
    return null;
  }

  @Override
  public Object visitLetCommand(LetCommand ast, Object o) {
    Frame frame = (Frame) o;
    int extraSize = ((Integer) ast.D.visit(this, frame));
    ast.C.visit(this, new Frame(frame, extraSize));
    if (extraSize > 0)
      emit(Machine.POPop, 0, 0, extraSize);
    return null;
  }

  @Override
  public Object visitSequentialCommand(SequentialCommand ast, Object o) {
    ast.C1.visit(this,o);
    ast.C2.visit(this,o);
    return null;
  }

  @Override
  public Object visitForLoopCommand(ForLoopCommand ast, Object o) {
    Frame frame = (Frame) o; // Load the frame
    
    // Load the halting expression
    int haltingExpressionSize = (Integer) ast.HaltingExpression.visit(this, frame);
    frame = new Frame(frame, haltingExpressionSize);// Modify the frame
    
    // Load the initial expression
    int initialExpressionSize = (Integer) ast.InitialDeclaration.E.visit(this, frame);
    ast.InitialDeclaration.entity = new UnknownValue(initialExpressionSize, frame.level, frame.size);
    frame = new Frame(frame, initialExpressionSize);// Modify the frame
    
    // I need to keep on these addresses
    int jumpAddr, loopAddr;
    jumpAddr = nextInstrAddr;
    emit(Machine.JUMPop, 0, Machine.SBr, 0);
    loopAddr = nextInstrAddr;
    ast.C.visit(this, frame);//Command
    
    // Now, update control variable value, it's on top of the Stack
    emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.succDisplacement);//Increase value of control
    
    patchD(jumpAddr, nextInstrAddr);//patch incomplete jump
    
    // Finally check if I need to keep going
    //Load initial Expression Value
    emit(Machine.LOADop, 1, Machine.STr, -1);
    //Load halting Expression Value
    emit(Machine.LOADop, 1, Machine.STr, -3);
    //call "lower or equal than" operation, Check initial <= halting
    emit(Machine.CALLop, 0, Machine.PBr, Machine.leDisplacement);
    emit(Machine.JUMPIFop, 1, Machine.SBr, loopAddr);//Conditional jump
    
    // Pop the space for the halting and initial expressions
    emit(Machine.POPop, 0, 0, initialExpressionSize + haltingExpressionSize);
    return null;
  }

  @Override
  public Object visitWhileLoopCommand(LoopCommand ast, Object o) {
    Frame frame = (Frame) o;
    int jumpAddr, loopAddr;
    jumpAddr = nextInstrAddr;
    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    loopAddr = nextInstrAddr;
    ast.C.visit(this, frame);
    patchD(jumpAddr, nextInstrAddr);
    ast.E.visit(this, frame);
    emit(Machine.JUMPIFop, Machine.trueRep, Machine.CBr, loopAddr);
    return null;
  }

  @Override
  public Object visitDoWhileLoopCommand(LoopCommand ast, Object o) {
    Frame frame = (Frame) o;
    int loopAddr;
    loopAddr = nextInstrAddr;
    ast.C.visit(this, frame);
    ast.E.visit(this, frame);
    emit(Machine.JUMPIFop, Machine.trueRep, Machine.CBr, loopAddr);
    return null;
  }


  @Override
  public Object visitUntilLoopCommand(LoopCommand ast, Object o) {
    Frame frame = (Frame) o;
    int jumpAddr, loopAddr;
    jumpAddr = nextInstrAddr;
    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    loopAddr = nextInstrAddr;
    ast.C.visit(this, frame);
    patchD(jumpAddr, nextInstrAddr);
    ast.E.visit(this, frame);
    emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, loopAddr);
    return null;
  }

  @Override
  public Object visitDoUntilLoopCommand(LoopCommand ast, Object o) {
    Frame frame = (Frame) o;
    int loopAddr;
    loopAddr = nextInstrAddr;
    ast.C.visit(this, frame);
    ast.E.visit(this, frame);
    emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, loopAddr);
    return null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Expressions">
  @Override
  public Object visitArrayExpression(ArrayExpression ast, Object o) {
    ast.type.visit(this, null);
    return ast.AA.visit(this, o);
  }

  @Override
  public Object visitBinaryExpression(BinaryExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    int valSize1 = ((Integer) ast.E1.visit(this, frame));
    Frame frame1 = new Frame(frame, valSize1);
    int valSize2 = ((Integer) ast.E2.visit(this, frame1));
    Frame frame2 = new Frame(frame.level, valSize1 + valSize2);
    ast.O.visit(this, frame2);
    return valSize;
  }

  @Override
  public Object visitCallExpression(CallExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    Integer argsSize = (Integer) ast.APS.visit(this, frame);
    ast.I.visit(this, new Frame(frame.level, argsSize));
    return valSize;
  }

  @Override
  public Object visitCharacterExpression(CharacterExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    emit(Machine.LOADLop, 0, 0, ast.CL.spelling.charAt(1));
    return valSize;
  }
  @Override
  public Object visitEmptyExpression(EmptyExpression ast, Object o) {
    return 0;
  }

  @Override
  public Object visitIfExpression(IfExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize;
    int jumpifAddr, jumpAddr;

    ast.type.visit(this, null);
    ast.E1.visit(this, frame);
    jumpifAddr = nextInstrAddr;
    emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, 0);
    //valSize = (Integer) ast.E2.visit(this, frame);
    jumpAddr = nextInstrAddr;
    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    patchD(jumpifAddr, nextInstrAddr);
    valSize = (Integer) ast.E3.visit(this, frame);
    patchD(jumpAddr, nextInstrAddr);
    return valSize;
  }

  @Override
  public Object visitIntegerExpression(IntegerExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    emit(Machine.LOADLop, 0, 0, Integer.parseInt(ast.IL.spelling));
    return valSize;
  }

  @Override
  public Object visitLetExpression(LetExpression ast, Object o) {
    Frame frame = (Frame) o;
    ast.type.visit(this, null);
    int extraSize = ((Integer) ast.D.visit(this, frame));
    Frame frame1 = new Frame(frame, extraSize);
    Integer valSize = (Integer) ast.E.visit(this, frame1);
    if (extraSize > 0)
      emit(Machine.POPop, valSize, 0, extraSize);
    return valSize;
  }

  @Override
  public Object visitRecordExpression(RecordExpression ast, Object o){
    ast.type.visit(this, null);
    return ast.RA.visit(this, o);
  }

  @Override
  public Object visitUnaryExpression(UnaryExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    ast.E.visit(this, frame);
    ast.O.visit(this, new Frame(frame.level, valSize));
    return valSize;
  }

  @Override
  public Object visitVnameExpression(VnameExpression ast, Object o) {
    Frame frame = (Frame) o;
    Integer valSize = (Integer) ast.type.visit(this, null);
    encodeFetch(ast.V, frame, valSize);
    return valSize;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Declarations">

  @Override
  public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o){
    return 0;
  }

  @Override
  public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
    Frame frame = (Frame) o;
    int extraSize = 0;

    if (ast.E instanceof CharacterExpression) {
      CharacterLiteral CL = ((CharacterExpression) ast.E).CL;
      ast.entity = new KnownValue(Machine.characterSize,
              characterValuation(CL.spelling));
    } else if (ast.E instanceof IntegerExpression) {
      IntegerLiteral IL = ((IntegerExpression) ast.E).IL;
      ast.entity = new KnownValue(Machine.integerSize,
              Integer.parseInt(IL.spelling));
    } else {
      int valSize = ((Integer) ast.E.visit(this, frame));
      ast.entity = new UnknownValue(valSize, frame.level, frame.size);
      extraSize = valSize;
    }
    writeTableDetails(ast);
    return extraSize;
  }

  @Override
  public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
    Frame frame = (Frame) o;
    int jumpAddr = nextInstrAddr;
    int argsSize = 0, valSize = 0;

    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    ast.entity = new KnownRoutine(Machine.closureSize, frame.level, nextInstrAddr);
    writeTableDetails(ast);
    if (frame.level == Machine.maxRoutineLevel)
      reporter.reportRestriction("can't nest routines more than 7 deep");
    else {
      Frame frame1 = new Frame(frame.level + 1, 0);
      argsSize = ((Integer) ast.FPS.visit(this, frame1));
      Frame frame2 = new Frame(frame.level + 1, Machine.linkDataSize);
      valSize = ((Integer) ast.E.visit(this, frame2));
    }
    emit(Machine.RETURNop, valSize, 0, argsSize);
    patchD(jumpAddr, nextInstrAddr);
    return 0;
  }

  @Override
  public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
    Frame frame = (Frame) o;
    int jumpAddr = nextInstrAddr;
    int argsSize = 0;

    emit(Machine.JUMPop, 0, Machine.CBr, 0);
    ast.entity = new KnownRoutine (Machine.closureSize, frame.level,
            nextInstrAddr);
    writeTableDetails(ast);
    if (frame.level == Machine.maxRoutineLevel)
      reporter.reportRestriction("can't nest routines so deeply");
    else {
      Frame frame1 = new Frame(frame.level + 1, 0);
      argsSize = ((Integer) ast.FPS.visit(this, frame1));
      Frame frame2 = new Frame(frame.level + 1, Machine.linkDataSize);
      ast.C.visit(this, frame2);
    }


    emit(Machine.RETURNop, 0, 0, argsSize);
    patchD(jumpAddr, nextInstrAddr);
    return 0;
  }

  @Override
  public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
    Frame frame = (Frame) o;
    int extraSize1, extraSize2;

    extraSize1 = ((Integer) ast.D1.visit(this, frame));
    Frame frame1 = new Frame (frame, extraSize1);
    extraSize2 = ((Integer) ast.D2.visit(this, frame1));
    return extraSize1 + extraSize2;
  }

  @Override
  public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
    // just to ensure the type's representation is decided
    ast.T.visit(this, null);
    return 0;
  }

  @Override
  public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
    return 0;
  }

  @Override
  public Object visitVarDeclaration(VarDeclaration ast, Object o) {
    Frame frame = (Frame) o;
    int extraSize;

    extraSize = ((Integer) ast.T.visit(this, null));
    emit(Machine.PUSHop, 0, 0, extraSize);
    ast.entity = new KnownAddress(Machine.addressSize, frame.level, frame.size);
    writeTableDetails(ast);
    return extraSize;
  }

  @Override
  public Object visitVarDeclarationInitialized(VarDeclarationInitialized ast, Object o) {
    Frame frame = (Frame) o;

    // Visiting the expression will allow me to know how much space it needs
    // And leaves it on top of the stack
    int extraSize = (Integer) ast.E.visit(this, frame);

    if (ast.E instanceof CharacterExpression) {
      CharacterLiteral CL = ((CharacterExpression) ast.E).CL;
      ast.entity = new KnownAddressWithValue(Machine.addressSize,
              frame.level,
              frame.size,
              characterValuation(CL.spelling)
      );

    } else if (ast.E instanceof IntegerExpression) {
      IntegerLiteral IL = ((IntegerExpression) ast.E).IL;
      ast.entity = new KnownAddressWithValue(Machine.addressSize,
              frame.level,
              frame.size,
              Integer.parseInt(IL.spelling)
      );
    } else {
      ast.entity = new KnownAddress(extraSize, frame.level, frame.size);
    }
    writeTableDetails(ast);
    return extraSize;
  }

  @Override
  public Object visitRecursiveDeclaration(RecursiveDeclaration ast, Object o) {
    Frame frame = (Frame) o;

    //Store the instruction address at the start of the encoding of the recursive decl.
    int currentInstrAddress = nextInstrAddr;

    //First pass. This pass sets the declarations entrance addresses and emits instruction placeholders for future calls
    ast.D.visit(this, frame);

    //Reset the instruction address to start the second pass
    nextInstrAddr = currentInstrAddress;

    //Second pass. Emits the actual instructions by replacing the placeholders
    return (Integer) ast.D.visit(this, frame);
  }

  @Override
  public Object visitLocalDeclaration(LocalDeclaration ast, Object o) {
    Frame frame = (Frame) o;

    int extraSize1, extraSize2;

    extraSize1 = ((Integer) ast.dAST1.visit(this, frame));
    Frame frame1 = new Frame (frame, extraSize1);
    extraSize2 = ((Integer) ast.dAST2.visit(this, frame1));
    return extraSize1 + extraSize2;

  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Aggregates">

  // Array Aggregates
  @Override
  public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast,
                                            Object o) {
    Frame frame = (Frame) o;
    int elemSize = ((Integer) ast.E.visit(this, frame));
    Frame frame1 = new Frame(frame, elemSize);
    int arraySize = ((Integer) ast.AA.visit(this, frame1));
    return elemSize + arraySize;
  }

  @Override
  public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
    return ast.E.visit(this, o);
  }


  // Record Aggregates
  @Override
  public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast,
                                             Object o) {
    Frame frame = (Frame) o;
    int fieldSize = ((Integer) ast.E.visit(this, frame));
    Frame frame1 = new Frame (frame, fieldSize);
    int recordSize = ((Integer) ast.RA.visit(this, frame1));
    return fieldSize + recordSize;
  }

  @Override
  public Object visitSingleRecordAggregate(SingleRecordAggregate ast,
                                           Object o) {
    return ast.E.visit(this, o);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Parameters">

  // Formal Parameters
  @Override
  public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
    Frame frame = (Frame) o;
    int valSize = ((Integer) ast.T.visit(this, null));
    ast.entity = new UnknownValue (valSize, frame.level, -frame.size - valSize);
    writeTableDetails(ast);
    return valSize;
  }

  @Override
  public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
    Frame frame = (Frame) o;
    int argsSize = Machine.closureSize;
    ast.entity = new UnknownRoutine (Machine.closureSize, frame.level,
            -frame.size - argsSize);
    writeTableDetails(ast);
    return argsSize;
  }

  @Override
  public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
    Frame frame = (Frame) o;
    int argsSize = Machine.closureSize;
    ast.entity = new UnknownRoutine (Machine.closureSize, frame.level,
            -frame.size - argsSize);
    writeTableDetails(ast);
    return argsSize;
  }

  @Override
  public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
    Frame frame = (Frame) o;
    ast.T.visit(this, null);
    ast.entity = new UnknownAddress (Machine.addressSize, frame.level,
            -frame.size - Machine.addressSize);
    writeTableDetails(ast);
    return Machine.addressSize;
  }

  @Override
  public Object visitEmptyFormalParameterSequence(
          EmptyFormalParameterSequence ast, Object o) {
    return 0;
  }

  @Override
  public Object visitMultipleFormalParameterSequence(
          MultipleFormalParameterSequence ast, Object o) {
    Frame frame = (Frame) o;
    int argsSize1 = ((Integer) ast.FPS.visit(this, frame));
    Frame frame1 = new Frame(frame, argsSize1);
    int argsSize2 = ((Integer) ast.FP.visit(this, frame1));
    return argsSize1 + argsSize2;
  }

  @Override
  public Object visitSingleFormalParameterSequence(
          SingleFormalParameterSequence ast, Object o) {
    return ast.FP.visit (this, o);
  }


  // Actual Parameters
  @Override
  public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
    return ast.E.visit (this, o);
  }

  @Override
  public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
    Frame frame = (Frame) o;
    if (ast.I.decl.entity instanceof KnownRoutine) {
      ObjectAddress address = ((KnownRoutine) ast.I.decl.entity).address;
      // static link, code address
      emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level), 0);
      emit(Machine.LOADAop, 0, Machine.CBr, address.displacement);
    } else if (ast.I.decl.entity instanceof UnknownRoutine) {
      ObjectAddress address = ((UnknownRoutine) ast.I.decl.entity).address;
      emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
              address.level), address.displacement);
    } else if (ast.I.decl.entity instanceof PrimitiveRoutine) {
      int displacement = ((PrimitiveRoutine) ast.I.decl.entity).displacement;
      // static link, code address
      emit(Machine.LOADAop, 0, Machine.SBr, 0);
      emit(Machine.LOADAop, 0, Machine.PBr, displacement);
    }
    return Machine.closureSize;
  }

  @Override
  public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
    Frame frame = (Frame) o;
    if (ast.I.decl.entity instanceof KnownRoutine) {
      ObjectAddress address = ((KnownRoutine) ast.I.decl.entity).address;
      // static link, code address
      emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level), 0);
      emit(Machine.LOADAop, 0, Machine.CBr, address.displacement);
    } else if (ast.I.decl.entity instanceof UnknownRoutine) {
      ObjectAddress address = ((UnknownRoutine) ast.I.decl.entity).address;
      emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
              address.level), address.displacement);
    } else if (ast.I.decl.entity instanceof PrimitiveRoutine) {
      int displacement = ((PrimitiveRoutine) ast.I.decl.entity).displacement;
      // static link, code address
      emit(Machine.LOADAop, 0, Machine.SBr, 0);
      emit(Machine.LOADAop, 0, Machine.PBr, displacement);
    }
    return Machine.closureSize;
  }

  @Override
  public Object visitVarActualParameter(VarActualParameter ast, Object o) {
    encodeFetchAddress(ast.V, (Frame) o);
    return Machine.addressSize;
  }

  @Override
  public Object visitEmptyActualParameterSequence(
          EmptyActualParameterSequence ast, Object o) {
    return 0;
  }

  @Override
  public Object visitMultipleActualParameterSequence(
          MultipleActualParameterSequence ast, Object o) {
    Frame frame = (Frame) o;
    int argsSize1 = ((Integer) ast.AP.visit(this, frame));
    Frame frame1 = new Frame (frame, argsSize1);
    int argsSize2 = ((Integer) ast.APS.visit(this, frame1));
    return argsSize1 + argsSize2;
  }

  @Override
  public Object visitSingleActualParameterSequence(
          SingleActualParameterSequence ast, Object o) {
    return ast.AP.visit (this, o);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Type Denoters and Variables">
  // Type Denoters
  @Override
  public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
    return 0;
  }

  @Override
  public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
    int typeSize;
    if (ast.entity == null) {
      int elemSize = ((Integer) ast.T.visit(this, null));
      typeSize = Integer.parseInt(ast.IL.spelling) * elemSize;
      ast.entity = new TypeRepresentation(typeSize);
      writeTableDetails(ast);
    } else
      typeSize = ast.entity.size;
    return typeSize;
  }

  @Override
  public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
    if (ast.entity == null) {
      ast.entity = new TypeRepresentation(Machine.booleanSize);
      writeTableDetails(ast);
    }
    return Machine.booleanSize;
  }

  @Override
  public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
    if (ast.entity == null) {
      ast.entity = new TypeRepresentation(Machine.characterSize);
      writeTableDetails(ast);
    }
    return Machine.characterSize;
  }

  @Override
  public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
    return 0;
  }

  @Override
  public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast,
                                       Object o) {
    return 0;
  }

  @Override
  public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
    if (ast.entity == null) {
      ast.entity = new TypeRepresentation(Machine.integerSize);
      writeTableDetails(ast);
    }
    return Machine.integerSize;
  }

  @Override
  public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
    int typeSize;
    if (ast.entity == null) {
      typeSize = ((Integer) ast.FT.visit(this, 0));
      ast.entity = new TypeRepresentation(typeSize);
      writeTableDetails(ast);
    } else
      typeSize = ast.entity.size;
    return typeSize;
  }

  @Override
  public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast,
                                              Object o) {
    int offset = ((Integer) o);
    int fieldSize;

    if (ast.entity == null) {
      fieldSize = ((Integer) ast.T.visit(this, null));
      ast.entity = new Field (fieldSize, offset);
      writeTableDetails(ast);
    } else
      fieldSize = ast.entity.size;

    Integer offset1 = offset + fieldSize;
    int recSize = ((Integer) ast.FT.visit(this, offset1));
    return fieldSize + recSize;
  }

  @Override
  public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast,
                                            Object o) {
    int offset = ((Integer) o);
    int fieldSize;

    if (ast.entity == null) {
      fieldSize = ((Integer) ast.T.visit(this, null));
      ast.entity = new Field (fieldSize, offset);
      writeTableDetails(ast);
    } else
      fieldSize = ast.entity.size;

    return fieldSize;
  }


  // Literals, Identifiers and Operators
  @Override
  public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
    return null;
  }

  @Override
  public Object visitIdentifier(Identifier ast, Object o) {
    Frame frame = (Frame)o;


    //Declaration is null, it hasn't been visited yet
    if(ast.decl.entity == null)
      //Add to pending calls. Need to store: frame's level, I ast
      emit(Machine.CALLop, 0, Machine.CBr, 0); //Emit the instruction, but needs to be patched later


    else if (ast.decl.entity instanceof KnownRoutine) {
      ObjectAddress address = ((KnownRoutine) ast.decl.entity).address;
      emit(Machine.CALLop, displayRegister(frame.level, address.level),
              Machine.CBr, address.displacement);
    } else if (ast.decl.entity instanceof UnknownRoutine) {
      ObjectAddress address = ((UnknownRoutine) ast.decl.entity).address;
      emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
              address.level), address.displacement);
      emit(Machine.CALLIop, 0, 0, 0);
    } else if (ast.decl.entity instanceof PrimitiveRoutine) {
      int displacement = ((PrimitiveRoutine) ast.decl.entity).displacement;
      if (displacement != Machine.idDisplacement)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
    } else if (ast.decl.entity instanceof EqualityRoutine) { // "=" or "\="
      int displacement = ((EqualityRoutine) ast.decl.entity).displacement;
      emit(Machine.LOADLop, 0, 0, frame.size / 2);
      emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
    }
    return null;
  }

  @Override
  public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
    return null;
  }

  @Override
  public Object visitOperator(Operator ast, Object o) {
    Frame frame = (Frame) o;
    if (ast.decl.entity instanceof KnownRoutine) {
      ObjectAddress address = ((KnownRoutine) ast.decl.entity).address;
      emit(Machine.CALLop, displayRegister (frame.level, address.level),
              Machine.CBr, address.displacement);
    } else if (ast.decl.entity instanceof UnknownRoutine) {
      ObjectAddress address = ((UnknownRoutine) ast.decl.entity).address;
      emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
              address.level), address.displacement);
      emit(Machine.CALLIop, 0, 0, 0);
    } else if (ast.decl.entity instanceof PrimitiveRoutine) {
      int displacement = ((PrimitiveRoutine) ast.decl.entity).displacement;
      if (displacement != Machine.idDisplacement)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
    } else if (ast.decl.entity instanceof EqualityRoutine) { // "=" or "\="
      int displacement = ((EqualityRoutine) ast.decl.entity).displacement;
      emit(Machine.LOADLop, 0, 0, frame.size / 2);
      emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
    }
    return null;
  }

  // Value-or-variable names
  @Override
  public Object visitDotVname(DotVname ast, Object o) {
    Frame frame = (Frame) o;
    RuntimeEntity baseObject = (RuntimeEntity) ast.V.visit(this, frame);
    ast.offset = ast.V.offset + ((Field) ast.I.decl.entity).fieldOffset;
    // I.decl points to the appropriate record field
    ast.indexed = ast.V.indexed;
    return baseObject;
  }

  @Override
  public Object visitSimpleVname(SimpleVname ast, Object o) {
    ast.offset = 0;
    ast.indexed = false;
    return ast.I.decl.entity;
  }

  @Override
  public Object visitSubscriptVname(SubscriptVname ast, Object o) {
    Frame frame = (Frame) o;
    RuntimeEntity baseObject;
    int elemSize, indexSize;

    baseObject = (RuntimeEntity) ast.V.visit(this, frame);
    ast.offset = ast.V.offset;
    ast.indexed = ast.V.indexed;
    elemSize = ((Integer) ast.type.visit(this, null));
    if (ast.E instanceof IntegerExpression) {
      IntegerLiteral IL = ((IntegerExpression) ast.E).IL;
      int index = Integer.parseInt(IL.spelling);
      emit(Machine.LOADLop, 0, 0, ast.V.offset + index * elemSize); //Index
      emit(Machine.LOADLop, 0, 0, ast.V.offset); // Lower Bound
      emit(Machine.LOADLop, 0, 0, ast.V.type.entity.size); // Upper bound
      
      // Check its bounds
      emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.indexCheckDisplacement);
      // Keep on generating code
      ast.offset = ast.offset + index * elemSize;
    } else {
      // v-name is indexed by a proper expression, not a literal
      if (ast.indexed)
        frame.size = frame.size + Machine.integerSize;
      indexSize = ((Integer) ast.E.visit(this, frame));
      if (elemSize != 1) {
        emit(Machine.LOADLop, 0, 0, elemSize);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr,
                Machine.multDisplacement);
      }
      if (ast.indexed)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      else
        ast.indexed = true;
    }
    return baseObject;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Auxiliar Methods">

  // Programs
  @Override
  public Object visitProgram(Program ast, Object o) {
    return ast.C.visit(this, o);
  }

  public Encoder (ErrorReporter reporter) {
    this.reporter = reporter;
    nextInstrAddr = Machine.CB;
    elaborateStdEnvironment();
  }

  private final ErrorReporter reporter;

  // Generates code to run a program.
  // showingTable is true iff entity description details
  // are to be displayed.
  public final void encodeRun (Program theAST, boolean showingTable) {
    tableDetailsReqd = showingTable;
    //startCodeGeneration();
    theAST.visit(this, new Frame (0, 0));
    emit(Machine.HALTop, 0, 0, 0);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Standard Environment">
  // Decides run-time representation of a standard constant.
  private void elaborateStdConst (Declaration constDeclaration,
                                  int value) {

    if (constDeclaration instanceof ConstDeclaration) {
      ConstDeclaration decl = (ConstDeclaration) constDeclaration;
      int typeSize = ((Integer) decl.E.type.visit(this, null));
      decl.entity = new KnownValue(typeSize, value);
      writeTableDetails(constDeclaration);
    }
  }

  // Decides run-time representation of a standard routine.
  private void elaborateStdPrimRoutine (Declaration routineDeclaration,
                                        int routineOffset) {
    routineDeclaration.entity = new PrimitiveRoutine (Machine.closureSize, routineOffset);
    writeTableDetails(routineDeclaration);
  }

  private void elaborateStdEqRoutine (Declaration routineDeclaration,
                                      int routineOffset) {
    routineDeclaration.entity = new EqualityRoutine (Machine.closureSize, routineOffset);
    writeTableDetails(routineDeclaration);
  }

  private void elaborateStdRoutine (Declaration routineDeclaration,
                                    int routineOffset) {
    routineDeclaration.entity = new KnownRoutine (Machine.closureSize, 0, routineOffset);
    writeTableDetails(routineDeclaration);
  }

  private void elaborateStdEnvironment() {
    tableDetailsReqd = false;
    elaborateStdConst(StdEnvironment.falseDecl, Machine.falseRep);
    elaborateStdConst(StdEnvironment.trueDecl, Machine.trueRep);
    elaborateStdPrimRoutine(StdEnvironment.notDecl, Machine.notDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.negDecl, Machine.negDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.andDecl, Machine.andDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.orDecl, Machine.orDisplacement);
    elaborateStdConst(StdEnvironment.maxintDecl, Machine.maxintRep);
    elaborateStdPrimRoutine(StdEnvironment.addDecl, Machine.addDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.subtractDecl, Machine.subDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.multiplyDecl, Machine.multDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.divideDecl, Machine.divDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.moduloDecl, Machine.modDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.lessDecl, Machine.ltDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.notgreaterDecl, Machine.leDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.greaterDecl, Machine.gtDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.notlessDecl, Machine.geDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.chrDecl, Machine.idDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.ordDecl, Machine.idDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.eolDecl, Machine.eolDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.eofDecl, Machine.eofDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.getDecl, Machine.getDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.putDecl, Machine.putDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.getintDecl, Machine.getintDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.putintDecl, Machine.putintDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.geteolDecl, Machine.geteolDisplacement);
    elaborateStdPrimRoutine(StdEnvironment.puteolDecl, Machine.puteolDisplacement);
    elaborateStdEqRoutine(StdEnvironment.equalDecl, Machine.eqDisplacement);
    elaborateStdEqRoutine(StdEnvironment.unequalDecl, Machine.neDisplacement);
    
    // Since I didn't want the user to use the indexCheck primitive, I need
    // To declare it and elaborate it here
    Identifier DUMMYIDENTIFIER = new Identifier("", new SourcePosition());
    SourcePosition DUMMYPOS = new SourcePosition();
    
    // Now I'll declare it, It receieves 3 Integer parameters: Index, Lower and Upper bound
    StdEnvironment.indexCheck = new ProcDeclaration(new Identifier("indexCheck", DUMMYPOS),
      new MultipleFormalParameterSequence(new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS),
            new MultipleFormalParameterSequence(new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS),
                                                new SingleFormalParameterSequence(new ConstFormalParameter(DUMMYIDENTIFIER, StdEnvironment.integerType, DUMMYPOS),
                                                                                  DUMMYPOS),
                                                DUMMYPOS),
                                          DUMMYPOS),
      new EmptyCommand(DUMMYPOS),
      DUMMYPOS
    );
    
    // Last but not least I need to elaborate it
    elaborateStdPrimRoutine(StdEnvironment.indexCheck, Machine.indexCheckDisplacement);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Code Generating Methods">
  // Saves the object program in the named file.

  public void saveObjectProgram(String objectName) {
    FileOutputStream objectFile;
    DataOutputStream objectStream;

    int addr;

    try {
      objectFile = new FileOutputStream (objectName);
      objectStream = new DataOutputStream (objectFile);

      for (addr = Machine.CB; addr < nextInstrAddr; addr++)
        Machine.code[addr].write(objectStream);
      objectFile.close();
    } catch (FileNotFoundException s) {
      System.err.println ("Error opening object file: " + s);
    } catch (IOException s) {
      System.err.println ("Error writing object file: " + s);
    }
  }

  boolean tableDetailsReqd;

  public static void writeTableDetails(AST ast) {
  }

  // OBJECT CODE

  // Implementation notes:
  // Object code is generated directly into the TAM Code Store, starting at CB.
  // The address of the next instruction is held in nextInstrAddr.

  private int nextInstrAddr;

  // Appends an instruction, with the given fields, to the object code.
  private void emit (int op, int n, int r, int d) {
    Instruction nextInstr = new Instruction();
    if (n > 255) {
      reporter.reportRestriction("length of operand can't exceed 255 words");
      n = 255; // to allow code generation to continue
    }
    nextInstr.op = op;
    nextInstr.n = n;
    nextInstr.r = r;
    nextInstr.d = d;
    if (nextInstrAddr == Machine.PB)
      reporter.reportRestriction("too many instructions for code segment");
    else {
      Machine.code[nextInstrAddr] = nextInstr;
      nextInstrAddr = nextInstrAddr + 1;
    }
  }

  // Patches the d-field of the instruction at address addr.
  private void patchD (int addr, int d) {
    Machine.code[addr].d = d;
  }

  // DATA REPRESENTATION

  public int characterValuation (String spelling) {
    // Returns the machine representation of the given character literal.
    return spelling.charAt(1);
    // since the character literal is of the form 'x'}
  }

  // REGISTERS

  // Returns the register number appropriate for object code at currentLevel
  // to address a data object at objectLevel.
  private int displayRegister (int currentLevel, int objectLevel) {
    if (objectLevel == 0)
      return Machine.SBr;
    else if (currentLevel - objectLevel <= 6)
      return Machine.LBr + currentLevel - objectLevel; // LBr|L1r|...|L6r
    else {
      reporter.reportRestriction("can't access data more than 6 levels out");
      return Machine.L6r;  // to allow code generation to continue
    }
  }

  // Generates code to fetch the value of a named constant or variable
  // and push it on to the stack.
  // currentLevel is the routine level where the vname occurs.
  // frameSize is the anticipated size of the local stack frame when
  // the constant or variable is fetched at run-time.
  // valSize is the size of the constant or variable's value.

  private void encodeStore(Vname V, Frame frame, int valSize) {

    RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);
    // If indexed = true, code will have been generated to load an index value.
    if (valSize > 255) {
      reporter.reportRestriction("can't store values larger than 255 words");
      valSize = 255; // to allow code generation to continue
    }
    if (baseObject instanceof KnownAddress) {
      ObjectAddress address = ((KnownAddress) baseObject).address;
      
      // Indexed means I need to check displacements
      if (V.indexed) {
        //<editor-fold defaultstate="collapsed" desc="Push Array Position">
        // I know the position result is in top of the stack, I'll duplicate it
        emit(Machine.LOADop, 1, Machine.STr, -1);
        
        // Push the base adress to the stack top
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        
        // Add leaves index at the stack top
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Push Lower Bound">
        // Pushes lower bound
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Push Upper Bound">
        // Pushes upper bound
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        emit(Machine.LOADLop, 0, 0, ((SubscriptVname)V).V.type.entity.size); // Loads size of the expression
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        //</editor-fold>
        
        // Check index
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.indexCheckDisplacement);
        
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
                address.displacement + V.offset);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        emit(Machine.STOREIop, valSize, 0, 0);
      } else {
        emit(Machine.STOREop, valSize, displayRegister(frame.level,
                address.level), address.displacement + V.offset);
      }
    } else if (baseObject instanceof UnknownAddress) {
      ObjectAddress address = ((UnknownAddress) baseObject).address;
      emit(Machine.LOADop, Machine.addressSize, displayRegister(frame.level,
              address.level), address.displacement);
      if (V.indexed)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      if (V.offset != 0) {
        emit(Machine.LOADLop, 0, 0, V.offset);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      }
      emit(Machine.STOREIop, valSize, 0, 0);
    }
  }

  // Generates code to fetch the value of a named constant or variable
  // and push it on to the stack.
  // currentLevel is the routine level where the vname occurs.
  // frameSize is the anticipated size of the local stack frame when
  // the constant or variable is fetched at run-time.
  // valSize is the size of the constant or variable's value.

  private void encodeFetch(Vname V, Frame frame, int valSize) {

    RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);
    // If indexed = true, code will have been generated to load an index value.
    if (valSize > 255) {
      reporter.reportRestriction("can't load values larger than 255 words");
      valSize = 255; // to allow code generation to continue
    }
    if (baseObject instanceof KnownValue) {
      // presumably offset = 0 and indexed = false
      int value = ((KnownValue) baseObject).value;
      emit(Machine.LOADLop, 0, 0, value);
    } else if ((baseObject instanceof UnknownValue) ||
            (baseObject instanceof KnownAddress)) {
      ObjectAddress address = (baseObject instanceof UnknownValue) ?
                              ((UnknownValue) baseObject).address :
                              ((KnownAddress) baseObject).address;
      // Indexed means I need to check displacements
      if (V.indexed) {
        //<editor-fold defaultstate="collapsed" desc="Push Array Position">
        // I know the position result is in top of the stack, I'll duplicate it
        emit(Machine.LOADop, 1, Machine.STr, -1);
        
        // Push the base adress to the stack top
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        
        // Add leaves index at the stack top
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Push Lower Bound">
        // Pushes lower bound
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Push Upper Bound">
        // Pushes upper bound
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        emit(Machine.LOADLop, 0, 0, ((SubscriptVname)V).V.type.entity.size); // Loads size of the expression
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        //</editor-fold>
        
        // Check index
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.indexCheckDisplacement);
        
        // Push the base adress to the stack top
        emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
             address.displacement + V.offset);
        // Add the just pushed address with the displacement
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        // Load target value
        emit(Machine.LOADIop, valSize, 0, 0);
      } else
        emit(Machine.LOADop, valSize, displayRegister(frame.level,
          address.level), address.displacement + V.offset);
    } else if (baseObject instanceof UnknownAddress) {
      ObjectAddress address = ((UnknownAddress) baseObject).address;
      emit(Machine.LOADop, Machine.addressSize, displayRegister(frame.level,
              address.level), address.displacement);
      if (V.indexed)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      if (V.offset != 0) {
        emit(Machine.LOADLop, 0, 0, V.offset);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      }
      emit(Machine.LOADIop, valSize, 0, 0);
    }
  }

  // Generates code to compute and push the address of a named variable.
  // vname is the program phrase that names this variable.
  // currentLevel is the routine level where the vname occurs.
  // frameSize is the anticipated size of the local stack frame when
  // the variable is addressed at run-time.

  private void encodeFetchAddress (Vname V, Frame frame) {

    RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);
    // If indexed = true, code will have been generated to load an index value.
    if (baseObject instanceof KnownAddress) {
      ObjectAddress address = ((KnownAddress) baseObject).address;
      emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
              address.displacement + V.offset);
      if (V.indexed)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
    } else if (baseObject instanceof UnknownAddress) {
      ObjectAddress address = ((UnknownAddress) baseObject).address;
      emit(Machine.LOADop, Machine.addressSize,displayRegister(frame.level,
              address.level), address.displacement);
      if (V.indexed)
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      if (V.offset != 0) {
        emit(Machine.LOADLop, 0, 0, V.offset);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
      }
    }
  }

  // </editor-fold>
}
