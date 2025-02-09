/*
 * @(#)Parser.java                        2.1 2003/10/07
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
package Triangle.SyntacticAnalyzer;

import Triangle.ErrorReporter;
import Triangle.AbstractSyntaxTrees.*;

public class Parser {

    private final Scanner lexicalAnalyser;
    private final ErrorReporter errorReporter;
    private Token currentToken;
    private SourcePosition previousTokenPosition;

    public Parser(Scanner lexer, ErrorReporter reporter) {
        lexicalAnalyser = lexer;
        errorReporter = reporter;
        previousTokenPosition = new SourcePosition();
    }

    //<editor-fold defaultstate="collapsed" desc="Methods">
    // accept checks whether the current token matches tokenExpected.
    // If so, fetches the next token.
    // If not, reports a syntactic error.
    private void accept(int tokenExpected) throws SyntaxError {
        if (currentToken.kind == tokenExpected) {
            previousTokenPosition = currentToken.position;
            currentToken = lexicalAnalyser.scan();
        } else {
            syntacticError("\"%\" expected here", Token.spell(tokenExpected));
        }
    }

    private void acceptIt() {
        previousTokenPosition = currentToken.position;
        currentToken = lexicalAnalyser.scan();
    }

    // start records the position of the start of a phrase.
    // This is defined to be the position of the first
    // character of the first token of the phrase.
    private void start(SourcePosition position) {
        position.start = currentToken.position.start;
    }

    // finish records the position of the end of a phrase.
    // This is defined to be the position of the last
    // character of the last token of the phrase.
    private void finish(SourcePosition position) {
        position.finish = previousTokenPosition.finish;
    }

    private void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
        SourcePosition pos = currentToken.position;
        errorReporter.reportError(messageTemplate, tokenQuoted, pos);
        throw (new SyntaxError());
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Programs">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // PROGRAMS
    //
    ///////////////////////////////////////////////////////////////////////////////
    public Program parseProgram() {

        Program programAST;

        previousTokenPosition.start = 0;
        previousTokenPosition.finish = 0;
        currentToken = lexicalAnalyser.scan();

        try {
            Command cAST = parseCommand();
            programAST = new Program(cAST, previousTokenPosition);
            if (currentToken.kind != Token.EOT) {
                syntacticError("\"%\" not expected after end of program",
                        currentToken.spelling);
            }
        } catch (SyntaxError s) {
            return null;
        }
        return programAST;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Literals">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // LITERALS
    //
    ///////////////////////////////////////////////////////////////////////////////
    // parseIntegerLiteral parses an integer-literal, and constructs
    // a leaf AST to represent it.
    private IntegerLiteral parseIntegerLiteral() throws SyntaxError {
        IntegerLiteral IL;

        if (currentToken.kind == Token.INTLITERAL) {
            previousTokenPosition = currentToken.position;
            String spelling = currentToken.spelling;
            IL = new IntegerLiteral(spelling, previousTokenPosition);
            currentToken = lexicalAnalyser.scan();
        } else {
            IL = null;
            syntacticError("integer literal expected here", "");
        }
        return IL;
    }

    // parseCharacterLiteral parses a character-literal, and constructs a leaf
    // AST to represent it.
    private CharacterLiteral parseCharacterLiteral() throws SyntaxError {
        CharacterLiteral CL;

        if (currentToken.kind == Token.CHARLITERAL) {
            previousTokenPosition = currentToken.position;
            String spelling = currentToken.spelling;
            CL = new CharacterLiteral(spelling, previousTokenPosition);
            currentToken = lexicalAnalyser.scan();
        } else {
            CL = null;
            syntacticError("character literal expected here", "");
        }
        return CL;
    }

    // parseIdentifier parses an identifier, and constructs a leaf AST to
    // represent it.
    private Identifier parseIdentifier() throws SyntaxError {
        Identifier I;

        if (currentToken.kind == Token.IDENTIFIER) {
            previousTokenPosition = currentToken.position;
            String spelling = currentToken.spelling;
            I = new Identifier(spelling, previousTokenPosition);
            currentToken = lexicalAnalyser.scan();
        } else {
            I = null;
            syntacticError("identifier expected here", "");
        }
        return I;
    }

    // parseOperator parses an operator, and constructs a leaf AST to
    // represent it.
    private Operator parseOperator() throws SyntaxError {
        Operator O;

        if (currentToken.kind == Token.OPERATOR) {
            previousTokenPosition = currentToken.position;
            String spelling = currentToken.spelling;
            O = new Operator(spelling, previousTokenPosition);
            currentToken = lexicalAnalyser.scan();
        } else {
            O = null;
            syntacticError("operator expected here", "");
        }
        return O;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Commands">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // COMMANDS
    //
    ///////////////////////////////////////////////////////////////////////////////
// parseCommand parses the command, and constructs an AST
// to represent its phrase structure.
    private Command parseCommand() throws SyntaxError {
        Command commandAST; // in case there's a syntactic error

        SourcePosition commandPos = new SourcePosition();

        start(commandPos);
        commandAST = parseSingleCommand();
        while (currentToken.kind == Token.SEMICOLON) {
            acceptIt();
            Command c2AST = parseSingleCommand();
            finish(commandPos);
            commandAST = new SequentialCommand(commandAST, c2AST, commandPos);
        }
        return commandAST;
    }

    private Command parseSingleCommand() throws SyntaxError {
        Command commandAST = null; // in case there's a syntactic error

        SourcePosition commandPos = new SourcePosition();
        start(commandPos);

        switch (currentToken.kind) {
            case Token.IDENTIFIER ->  {
                Identifier iAST = parseIdentifier();
                if (currentToken.kind == Token.LPAREN) {
                    acceptIt();
                    ActualParameterSequence apsAST = parseActualParameterSequence();
                    accept(Token.RPAREN);
                    finish(commandPos);
                    commandAST = new CallCommand(iAST, apsAST, commandPos);
                } else {
                    Vname vAST = parseRestOfVname(iAST);
                    accept(Token.BECOMES);
                    Expression eAST = parseExpression();
                    finish(commandPos);
                    commandAST = new AssignCommand(vAST, eAST, commandPos);
                }
                break;
            }
            case Token.LOOP ->  {
                //Loop token gets accepted, and we move to next token
                acceptIt();
                commandAST = this.parseLoopCommand();
                accept(Token.REPEAT);
                break;
            }
            case Token.LET ->  {
                acceptIt();
                Declaration dAST = parseDeclaration();
                accept(Token.IN);
                Command cAST = parseCommand();
                finish(commandPos);
                accept(Token.END);
                commandAST = new LetCommand(dAST, cAST, commandPos);
                break;
            }
            case Token.IF ->  {
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.THEN);
                Command c1AST = parseCommand();
                accept(Token.ELSE);
                Command c2AST = parseCommand();
                finish(commandPos);
                accept(Token.END);
                commandAST = new IfCommand(eAST, c1AST, c2AST, commandPos);
                break;
            }
            case Token.SKIP ->  {
                acceptIt();
                commandAST = new EmptyCommand(commandPos);
                break;
            }
            default -> {
                syntacticError("\"%\" cannot start a command",
                        currentToken.spelling);
                break;
            }
        }
        return commandAST;
    }

    private Command parseLoopCommand() throws SyntaxError {
        Command commandAST = null;

        SourcePosition commandPos = new SourcePosition();
        start(commandPos);

        switch (currentToken.kind) {
            case Token.WHILE, Token.UNTIL -> {
                //Loop kind will store the kind of loop the user has asked
                int loopKind = currentToken.kind;
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.DO);
                Command cAST = parseCommand();
                finish(commandPos);
                commandAST = loopKind == Token.WHILE
                        ? new WhileLoopCommand(eAST, cAST, commandPos)
                        : new UntilLoopCommand(eAST, cAST, commandPos);
                break;
            }
            case Token.DO -> {
                acceptIt();
                Command cAST = parseCommand();
                finish(commandPos);
                if (!(currentToken.kind == Token.WHILE || currentToken.kind == Token.UNTIL)) {
                    syntacticError("Unexpected \"%\"", currentToken.spelling);
                }
                int loopKind = currentToken.kind;
                acceptIt();
                Expression eAST = parseExpression();
                commandAST = loopKind == Token.WHILE
                        ? new DoWhileLoopCommand(eAST, cAST, commandPos)
                        : new DoUntilLoopCommand(eAST, cAST, commandPos);
                break;
            }
            case Token.FOR -> {
                acceptIt();
                Identifier identifier = parseIdentifier();
                accept(Token.IS);
                Expression idenAST = parseExpression();
                ConstDeclaration initialDeclaration = new ConstDeclaration(identifier, idenAST, commandPos);
                accept(Token.TO);
                Expression eAST = parseExpression();
                accept(Token.DO);
                Command cAST = parseCommand();
                commandAST = new ForLoopCommand(initialDeclaration, eAST, cAST, commandPos);
                break;
            }
            default -> {
                syntacticError("Unexpected \"%\"", currentToken.spelling);
                break;
            }
        }
        return commandAST;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Expressions">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////
    private Expression parseExpression() throws SyntaxError {
        Expression expressionAST;

        SourcePosition expressionPos = new SourcePosition();

        start(expressionPos);

        switch (currentToken.kind) {

            case Token.LET ->  {
                acceptIt();
                Declaration dAST = parseDeclaration();
                accept(Token.IN);
                Expression eAST = parseExpression();
                finish(expressionPos);
                expressionAST = new LetExpression(dAST, eAST, expressionPos);
                break;
            }
            case Token.IF ->  {
                acceptIt();
                Expression e1AST = parseExpression();
                accept(Token.THEN);
                Expression e2AST = parseExpression();
                accept(Token.ELSE);
                Expression e3AST = parseExpression();
                finish(expressionPos);
                expressionAST = new IfExpression(e1AST, e2AST, e3AST, expressionPos);
                break;
            }
            default -> {
                expressionAST = parseSecondaryExpression();
                break;
            }
        }
        return expressionAST;
    }

    private Expression parseSecondaryExpression() throws SyntaxError {
        Expression expressionAST;

        SourcePosition expressionPos = new SourcePosition();
        start(expressionPos);

        expressionAST = parsePrimaryExpression();
        while (currentToken.kind == Token.OPERATOR) {
            Operator opAST = parseOperator();
            Expression e2AST = parsePrimaryExpression();
            expressionAST = new BinaryExpression(expressionAST, opAST, e2AST,
                    expressionPos);
        }
        return expressionAST;
    }

    private Expression parsePrimaryExpression() throws SyntaxError {
        Expression expressionAST = null; // in case there's a syntactic error

        SourcePosition expressionPos = new SourcePosition();
        start(expressionPos);

        switch (currentToken.kind) {
            case Token.INTLITERAL ->  {
                IntegerLiteral ilAST = parseIntegerLiteral();
                finish(expressionPos);
                expressionAST = new IntegerExpression(ilAST, expressionPos);
                break;
            }
            case Token.CHARLITERAL ->  {
                CharacterLiteral clAST = parseCharacterLiteral();
                finish(expressionPos);
                expressionAST = new CharacterExpression(clAST, expressionPos);
                break;
            }
            case Token.LBRACKET ->  {
                acceptIt();
                ArrayAggregate aaAST = parseArrayAggregate();
                accept(Token.RBRACKET);
                finish(expressionPos);
                expressionAST = new ArrayExpression(aaAST, expressionPos);
                break;
            }
            case Token.LCURLY ->  {
                acceptIt();
                RecordAggregate raAST = parseRecordAggregate();
                accept(Token.RCURLY);
                finish(expressionPos);
                expressionAST = new RecordExpression(raAST, expressionPos);
                break;
            }
            case Token.IDENTIFIER ->  {
                Identifier iAST = parseIdentifier();
                if (currentToken.kind == Token.LPAREN) {
                    acceptIt();
                    ActualParameterSequence apsAST = parseActualParameterSequence();
                    accept(Token.RPAREN);
                    finish(expressionPos);
                    expressionAST = new CallExpression(iAST, apsAST, expressionPos);

                } else {
                    Vname vAST = parseRestOfVname(iAST);
                    finish(expressionPos);
                    expressionAST = new VnameExpression(vAST, expressionPos);
                }
                break;
            }
            case Token.OPERATOR ->  {
                Operator opAST = parseOperator();
                Expression eAST = parsePrimaryExpression();
                finish(expressionPos);
                expressionAST = new UnaryExpression(opAST, eAST, expressionPos);
                break;
            }
            case Token.LPAREN ->  {
                acceptIt();
                expressionAST = parseExpression();
                accept(Token.RPAREN);
                break;
            }
            default -> {
                syntacticError("\"%\" cannot start an expression",
                        currentToken.spelling);
                break;
            }
        }
        return expressionAST;
    }

    private RecordAggregate parseRecordAggregate() throws SyntaxError {
        RecordAggregate aggregateAST;

        SourcePosition aggregatePos = new SourcePosition();
        start(aggregatePos);

        Identifier iAST = parseIdentifier();
        accept(Token.IS);
        Expression eAST = parseExpression();

        if (currentToken.kind == Token.COMMA) {
            acceptIt();
            RecordAggregate aAST = parseRecordAggregate();
            finish(aggregatePos);
            aggregateAST = new MultipleRecordAggregate(iAST, eAST, aAST, aggregatePos);
        } else {
            finish(aggregatePos);
            aggregateAST = new SingleRecordAggregate(iAST, eAST, aggregatePos);
        }
        return aggregateAST;
    }

    private ArrayAggregate parseArrayAggregate() throws SyntaxError {
        ArrayAggregate aggregateAST;

        SourcePosition aggregatePos = new SourcePosition();
        start(aggregatePos);

        Expression eAST = parseExpression();
        if (currentToken.kind == Token.COMMA) {
            acceptIt();
            ArrayAggregate aAST = parseArrayAggregate();
            finish(aggregatePos);
            aggregateAST = new MultipleArrayAggregate(eAST, aAST, aggregatePos);
        } else {
            finish(aggregatePos);
            aggregateAST = new SingleArrayAggregate(eAST, aggregatePos);
        }
        return aggregateAST;
    }
    
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Value or Variables">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // VALUE-OR-VARIABLE NAMES
    //
    ///////////////////////////////////////////////////////////////////////////////
    private Vname parseVname() throws SyntaxError {
        Vname vnameAST;
        Identifier iAST = parseIdentifier();
        vnameAST = parseRestOfVname(iAST);
        return vnameAST;
    }

    private Vname parseRestOfVname(Identifier identifierAST) throws SyntaxError {
        SourcePosition vnamePos;
        vnamePos = identifierAST.position;
        Vname vAST = new SimpleVname(identifierAST, vnamePos);

        while (currentToken.kind == Token.DOT
                || currentToken.kind == Token.LBRACKET) {

            if (currentToken.kind == Token.DOT) {
                acceptIt();
                Identifier iAST = parseIdentifier();
                vAST = new DotVname(vAST, iAST, vnamePos);
            } else {
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.RBRACKET);
                finish(vnamePos);
                vAST = new SubscriptVname(vAST, eAST, vnamePos);
            }
        }
        return vAST;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Declarations">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // DECLARATIONS
    //
    ///////////////////////////////////////////////////////////////////////////////
    //New rule created for Compound-Declaration.
    private Declaration parseCompoundDeclaration() throws SyntaxError {
        Declaration declarationAST = null;

        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);
        switch (currentToken.kind) {
            case Token.RECURSIVE ->  {
                acceptIt();
                declarationAST = parseProcFuncs();
                accept(Token.END);
                finish(declarationPos);
                declarationAST = new RecursiveDeclaration(declarationAST, declarationPos);
                break;
            }
            case Token.LOCAL ->  {
                acceptIt();
                Declaration dAST1 = parseDeclaration();
                accept(Token.IN);
                Declaration dAST2 = parseDeclaration();
                accept(Token.END);
                finish(declarationPos);
                declarationAST = new LocalDeclaration(dAST1, dAST2, declarationPos);
                break;
            }
            case Token.CONST, Token.VAR, Token.TYPE, Token.FUNC, Token.PROC -> {
                declarationAST = parseSingleDeclaration();
                break;
            }
            default ->  {
                syntacticError("\"%\" Cannot start a Compound Declaration",
                currentToken.spelling);
            }
        }
        return declarationAST;
    }

    //This method was modified to work with the new rule named parseCompoundDeclaration, and with a recursive call.
    private Declaration parseDeclaration() throws SyntaxError {
        Declaration declarationAST;

        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);
        declarationAST = parseCompoundDeclaration();
        while (currentToken.kind == Token.SEMICOLON) {
            acceptIt();
            Declaration d2AST = parseCompoundDeclaration();
            finish(declarationPos);
            declarationAST = new SequentialDeclaration(declarationAST, d2AST,
                    declarationPos);
        }
        return declarationAST;
    }

    /**
     * This will check for ProcFuncs, it may return one or more
     * @return An AST representing ProcFunc, which may be sequential
     * @throws SyntaxError If there is no procfunc or there was a wrong token
     */
    private Declaration parseProcFuncs() throws SyntaxError {
        Declaration declarationAST = null; // in case there's a syntactic error

        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);

        if (currentToken.kind == Token.PROC || currentToken.kind == Token.FUNC) {
            declarationAST = parseSingleProcFunc();
            finish(declarationPos);
        } else {
            syntacticError("\"%\" not expected while parsing further proc-funcs, expected \"PROC\" or \"FUNC\"",
                    currentToken.spelling);
        }

        do {
            accept(Token.AND);
            if (currentToken.kind == Token.PROC || currentToken.kind == Token.FUNC) {
                start(declarationPos);
                Declaration dAST2 = parseSingleProcFunc();
                finish(declarationPos);
                declarationAST = new SequentialDeclaration(declarationAST, dAST2, declarationPos);
            } else {
                syntacticError("\"%\" not expected while parsing further proc-funcs, expected \"PROC\" or \"FUNC\"",
                        currentToken.spelling);
            }
        } while (currentToken.kind == Token.AND);

        return declarationAST;
    }

    /**
     * This will check for a ProcFunc
     * @return An AST representing a single ProcFunc
     * @throws SyntaxError If there is no procfunc or there was a wrong token
     */
    private Declaration parseSingleProcFunc() throws SyntaxError {
        Declaration declarationAST = null; // in case there's a syntactic error

        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);
        switch (currentToken.kind) {
            case Token.PROC ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();

                accept(Token.RPAREN);
                accept(Token.IS);
                Command cAST = parseCommand();
                accept(Token.END);
                finish(declarationPos);
                declarationAST = new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
                break;
            }
            case Token.FUNC ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.RPAREN);
                accept(Token.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                accept(Token.IS);
                Expression eAST = parseExpression();
                finish(declarationPos);
                declarationAST = new FuncDeclaration(iAST, fpsAST, tAST, eAST,
                        declarationPos);
                break;
            }
        }
        return declarationAST;
    }

    private Declaration parseSingleDeclaration() throws SyntaxError {
        Declaration declarationAST = null; // in case there's a syntactic error

        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);

        switch (currentToken.kind) {

            case Token.CONST ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.IS);
                Expression eAST = parseExpression();
                finish(declarationPos);
                declarationAST = new ConstDeclaration(iAST, eAST, declarationPos);
                break;
            }
            case Token.VAR ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();

                switch (currentToken.kind) {
                    case Token.COLON ->  {
                        acceptIt();
                        TypeDenoter tAST = parseTypeDenoter();
                        finish(declarationPos);
                        declarationAST = new VarDeclaration(iAST, tAST, declarationPos);
                        break;
                    }
                    case Token.INIT ->  {
                        acceptIt();
                        Expression eAST = parseExpression();
                        finish(declarationPos);
                        declarationAST = new VarDeclarationInitialized(iAST, eAST, declarationPos);
                        break;
                    }
                }
                break;
            }
            case Token.TYPE ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.IS);
                TypeDenoter tAST = parseTypeDenoter();
                finish(declarationPos);
                declarationAST = new TypeDeclaration(iAST, tAST, declarationPos);
                break;
            }
            case Token.FUNC ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.RPAREN);
                accept(Token.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                accept(Token.IS);
                Expression eAST = parseExpression();
                finish(declarationPos);
                declarationAST = new FuncDeclaration(iAST, fpsAST, tAST, eAST,
                        declarationPos);
                break;
            }
            case Token.PROC ->  {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.RPAREN);
                accept(Token.IS);
                Command cAST = parseCommand();
                accept(Token.END);
                finish(declarationPos);
                declarationAST = new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
                break;
            }
            default -> {
                syntacticError("\"%\" cannot start a declaration",
                        currentToken.spelling);
                break;
            }
        }
        return declarationAST;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Parameters">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // PARAMETERS
    //
    ///////////////////////////////////////////////////////////////////////////////
    private FormalParameterSequence parseFormalParameterSequence() throws SyntaxError {
        FormalParameterSequence formalsAST;

        SourcePosition formalsPos = new SourcePosition();

        start(formalsPos);
        if (currentToken.kind == Token.RPAREN) {
            finish(formalsPos);
            formalsAST = new EmptyFormalParameterSequence(formalsPos);

        } else {
            formalsAST = parseProperFormalParameterSequence();
        }
        return formalsAST;
    }

    private FormalParameterSequence parseProperFormalParameterSequence() throws SyntaxError {
        FormalParameterSequence formalsAST;

        SourcePosition formalsPos = new SourcePosition();
        start(formalsPos);
        FormalParameter fpAST = parseFormalParameter();
        if (currentToken.kind == Token.COMMA) {
            acceptIt();
            FormalParameterSequence fpsAST = parseProperFormalParameterSequence();
            finish(formalsPos);
            formalsAST = new MultipleFormalParameterSequence(fpAST, fpsAST,
                    formalsPos);

        } else {
            finish(formalsPos);
            formalsAST = new SingleFormalParameterSequence(fpAST, formalsPos);
        }
        return formalsAST;
    }

    private FormalParameter parseFormalParameter() throws SyntaxError {
        FormalParameter formalAST = null; // in case there's a syntactic error;

        SourcePosition formalPos = new SourcePosition();
        start(formalPos);

        switch (currentToken.kind) {
            case Token.IDENTIFIER -> {
                Identifier iAST = parseIdentifier();
                accept(Token.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                formalAST = new ConstFormalParameter(iAST, tAST, formalPos);
                break;
            }
            case Token.VAR -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                formalAST = new VarFormalParameter(iAST, tAST, formalPos);
                break;
            }
            case Token.PROC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.RPAREN);
                finish(formalPos);
                formalAST = new ProcFormalParameter(iAST, fpsAST, formalPos);
                break;
            }
            case Token.FUNC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.RPAREN);
                accept(Token.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                formalAST = new FuncFormalParameter(iAST, fpsAST, tAST, formalPos);
                break;
            }
            default -> {
                syntacticError("\"%\" cannot start a formal parameter",
                        currentToken.spelling);
                break;
            }
        }
        return formalAST;
    }

    private ActualParameterSequence parseActualParameterSequence() throws SyntaxError {
        ActualParameterSequence actualsAST;

        SourcePosition actualsPos = new SourcePosition();

        start(actualsPos);
        if (currentToken.kind == Token.RPAREN) {
            finish(actualsPos);
            actualsAST = new EmptyActualParameterSequence(actualsPos);

        } else {
            actualsAST = parseProperActualParameterSequence();
        }
        return actualsAST;
    }

    private ActualParameterSequence parseProperActualParameterSequence() throws SyntaxError {
        ActualParameterSequence actualsAST;

        SourcePosition actualsPos = new SourcePosition();

        start(actualsPos);
        ActualParameter apAST = parseActualParameter();
        if (currentToken.kind == Token.COMMA) {
            acceptIt();
            ActualParameterSequence apsAST = parseProperActualParameterSequence();
            finish(actualsPos);
            actualsAST = new MultipleActualParameterSequence(apAST, apsAST,
                    actualsPos);
        } else {
            finish(actualsPos);
            actualsAST = new SingleActualParameterSequence(apAST, actualsPos);
        }
        return actualsAST;
    }

    private ActualParameter parseActualParameter() throws SyntaxError {
        ActualParameter actualAST = null; // in case there's a syntactic error

        SourcePosition actualPos = new SourcePosition();

        start(actualPos);

        switch (currentToken.kind) {

            case    Token.IDENTIFIER, Token.INTLITERAL, Token.CHARLITERAL,
                    Token.OPERATOR, Token.LET, Token.IF, Token.LPAREN,
                    Token.LBRACKET, Token.LCURLY -> {
                Expression eAST = parseExpression();
                finish(actualPos);
                actualAST = new ConstActualParameter(eAST, actualPos);
                break;
            }
            case Token.VAR -> {
                acceptIt();
                Vname vAST = parseVname();
                finish(actualPos);
                actualAST = new VarActualParameter(vAST, actualPos);
                break;
            }
            case Token.PROC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                finish(actualPos);
                actualAST = new ProcActualParameter(iAST, actualPos);
                break;
            }
            case Token.FUNC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                finish(actualPos);
                actualAST = new FuncActualParameter(iAST, actualPos);
                break;
            }
            default -> { 
                syntacticError("\"%\" cannot start an actual parameter",
                    currentToken.spelling);
                break;
            }
        }
        return actualAST;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Type Denoters">
    ///////////////////////////////////////////////////////////////////////////////
    //
    // TYPE-DENOTERS
    //
    ///////////////////////////////////////////////////////////////////////////////
    private TypeDenoter parseTypeDenoter() throws SyntaxError {
        TypeDenoter typeAST = null; // in case there's a syntactic error
        SourcePosition typePos = new SourcePosition();
        start(typePos);

        switch (currentToken.kind) {
            case Token.IDENTIFIER ->  {
                Identifier iAST = parseIdentifier();
                finish(typePos);
                typeAST = new SimpleTypeDenoter(iAST, typePos);
                break;
            }
            case Token.ARRAY ->  {
                acceptIt();
                IntegerLiteral ilAST = parseIntegerLiteral();
                accept(Token.OF);
                TypeDenoter tAST = parseTypeDenoter();
                finish(typePos);
                typeAST = new ArrayTypeDenoter(ilAST, tAST, typePos);
                break;
            }
            case Token.RECORD ->  {
                acceptIt();
                FieldTypeDenoter fAST = parseFieldTypeDenoter();
                accept(Token.END);
                finish(typePos);
                typeAST = new RecordTypeDenoter(fAST, typePos);
                break;
            }
            default ->  {
                syntacticError("\"%\" cannot start a type denoter",
                        currentToken.spelling);
                break;
            }
        }
        return typeAST;
    }

    private FieldTypeDenoter parseFieldTypeDenoter() throws SyntaxError {
        FieldTypeDenoter fieldAST;

        SourcePosition fieldPos = new SourcePosition();

        start(fieldPos);
        Identifier iAST = parseIdentifier();
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        if (currentToken.kind == Token.COMMA) {
            acceptIt();
            FieldTypeDenoter fAST = parseFieldTypeDenoter();
            finish(fieldPos);
            fieldAST = new MultipleFieldTypeDenoter(iAST, tAST, fAST, fieldPos);
        } else {
            finish(fieldPos);
            fieldAST = new SingleFieldTypeDenoter(iAST, tAST, fieldPos);
        }
        return fieldAST;
    }

    //</editor-fold>
}
