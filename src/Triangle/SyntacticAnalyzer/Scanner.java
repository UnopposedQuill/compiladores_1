/*
 * @(#)Scanner.java                        2.1 2003/10/07
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


import Triangle.ProgramWriter.HTMLWriter;

public final class Scanner {

  private SourceFile sourceFile;
  private boolean debug;
  private boolean writingHTML;
  private HTMLWriter htmlWriter;

  private char currentChar;
  private StringBuffer currentSpelling;
  private boolean currentlyScanningToken;

  private boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private boolean isDigit(char c) {
    return (c >= '0' && c <= '9');
  }

// isOperator returns true iff the given character is an operator character.

  private boolean isOperator(char c) {
    return (c == '+' || c == '-' || c == '*' || c == '/' ||
	    c == '=' || c == '<' || c == '>' || c == '\\' ||
	    c == '&' || c == '@' || c == '%' || c == '^' ||
	    c == '?');
  }


///////////////////////////////////////////////////////////////////////////////

  public Scanner(SourceFile source) {
    sourceFile = source;
    currentChar = sourceFile.getSource();
    debug = false;
    writingHTML = false;
  }

  public void htmlRun(HTMLWriter htmlWriter) {
    this.writingHTML = true;
    this.htmlWriter = htmlWriter;
    Token token;
    do {
      token = this.scan();
    }while(token.kind != Token.EOT);
    htmlWriter.finishHTML();
  }

  // takeIt appends the current character to the current token, and gets
  // the next character from the source program.

  private void takeIt() {
    if (currentlyScanningToken)
      currentSpelling.append(currentChar);
    currentChar = sourceFile.getSource();
  }

  // scanSeparator skips a single separator.

  private void scanSeparator() {
    switch (currentChar) {
        case '!' -> {
            takeIt();
            String comment = "!";
            while ((currentChar != SourceFile.EOL) && (currentChar != SourceFile.CR) && (currentChar != SourceFile.EOT)) {
              comment = comment + currentChar;
              takeIt();
            }
            if (currentChar == SourceFile.CR) {
              takeIt();
            }
            if (currentChar == SourceFile.EOL) {
              takeIt();
            }
            if(writingHTML)
              this.htmlWriter.writeComment(comment);
            break;
        }
        case '\n' -> {
            if(writingHTML)
                htmlWriter.writeElse("<br>\n");
            takeIt();
            break;
        }
        case '\r' -> {
            takeIt();
            break;
        }
        case ' ', '\t' -> {
            if(writingHTML)
                htmlWriter.writeElse(String.valueOf(currentChar));
            takeIt();
            break;
        }
    }
  }

  private int scanToken() {

    switch (currentChar) {

    case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
    case 'p':  case 'q':  case 'r':  case 's':  case 't':
    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
    case 'z':
    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
    case 'Z':
      takeIt();
      while (isLetter(currentChar) || isDigit(currentChar))
        takeIt();
      return Token.IDENTIFIER;

    case '0':  case '1':  case '2':  case '3':  case '4':
    case '5':  case '6':  case '7':  case '8':  case '9':
      String intLiteral = String.valueOf(currentChar);
      takeIt();
      while (isDigit(currentChar)) {
        intLiteral = intLiteral + currentChar;
        takeIt();
      }
      if(writingHTML)
        htmlWriter.writeLiteral(intLiteral);
      return Token.INTLITERAL;

    case '+':  case '-':  case '*': case '/':  case '=':
    case '<':  case '>':  case '\\':  case '&':  case '@':
    case '%':  case '^':  case '?':
      String operator = String.valueOf(currentChar);
      takeIt();
      while (isOperator(currentChar)) {
        operator = operator + currentChar;
        takeIt();
      }
      if(writingHTML)
        htmlWriter.writeElse(operator);
      return Token.OPERATOR;

    case '\'':
      takeIt();
      String characterLiteral = String.valueOf(currentChar);
      takeIt(); // the quoted character
      if (currentChar == '\'') {
      	takeIt();
      	if(writingHTML)
          htmlWriter.writeLiteral("\'"+characterLiteral+"\'");
        return Token.CHARLITERAL;
      } else
        return Token.ERROR;

    case '.':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.DOT;

    case ':':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      if (currentChar == '=') {
        if(writingHTML)
          htmlWriter.writeElse(String.valueOf(currentChar));
        takeIt();
        return Token.BECOMES;
      } else
        return Token.COLON;

    case ';':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.SEMICOLON;

    case ',':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.COMMA;

    case '~':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.IS;

    case '(':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.LPAREN;

    case ')':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.RPAREN;

    case '[':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.LBRACKET;

    case ']':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.RBRACKET;

    case '{':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.LCURLY;

    case '}':
      if(writingHTML)
        htmlWriter.writeElse(String.valueOf(currentChar));
      takeIt();
      return Token.RCURLY;

    case SourceFile.EOT:
      return Token.EOT;

    default:
      takeIt();
      return Token.ERROR;
    }
  }

  public Token scan () {
    Token tok;
    SourcePosition pos;
    int kind;

    currentlyScanningToken = false;
    while (currentChar == '!'
           || currentChar == ' '
           || currentChar == '\n'
           || currentChar == '\r'
           || currentChar == '\t')
      scanSeparator();

    currentlyScanningToken = true;
    currentSpelling = new StringBuffer("");
    pos = new SourcePosition();
    pos.start = sourceFile.getCurrentLine();

    kind = scanToken();

    pos.finish = sourceFile.getCurrentLine();
    boolean wasIdentifier = (kind == Token.IDENTIFIER);
    tok = new Token(kind, currentSpelling.toString(), pos);
    if(writingHTML && wasIdentifier && tok.kind != Token.IDENTIFIER)
      this.htmlWriter.writeKeyWord(tok.spelling);
    else if (writingHTML && wasIdentifier){
      this.htmlWriter.writeElse(tok.spelling);
    }
    if (debug)
      System.out.println(tok);
    return tok;
  }

}
