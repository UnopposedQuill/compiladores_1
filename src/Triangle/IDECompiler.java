/*
 * IDE-Triangle v1.0
 * Compiler.java 
 */

package Triangle;

import Triangle.ProgramWriter.HTMLWriter;
import Triangle.ProgramWriter.XMLWriter;
import Triangle.SyntacticAnalyzer.SourceFile;
import Triangle.SyntacticAnalyzer.Scanner;
import Triangle.AbstractSyntaxTrees.Program;
import Triangle.SyntacticAnalyzer.Parser;
import Triangle.ContextualAnalyzer.Checker;
import Triangle.CodeGenerator.Encoder;
import java.io.File;


/** 
 * This is merely a re-implementation of the Triangle.Compiler class. We need
 * to get to the ASTs in order to draw them in the IDE without modifying the
 * original Triangle code.
 *
 * @author Luis Leopoldo Pérez <luiperpe@ns.isi.ulatina.ac.cr>
 */
public class IDECompiler {

    // <editor-fold defaultstate="collapsed" desc=" Attributes ">
    private Program rootAST;        // The Root Abstract Syntax Tree.    
    private IDEReporter report;     // Our ErrorReporter class.
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Methods ">
    /**
     * Creates a new instance of IDECompiler.
     *
     */
    public IDECompiler() {
    }
    
    /**
     * Particularly the same compileProgram method from the Triangle.Compiler
     * class.
     * @param sourceName Path to the source file.
     * @return True if compilation was successful.
     */
    public boolean compileProgram(String sourceName) {
        System.out.println("********** " +
                           "Triangle Compiler (IDE-Triangle 1.0)" +
                           " **********");
        System.out.println("Lexical Analysis ...");
        SourceFile source = new SourceFile(sourceName);
        Scanner HTMLscanner = new Scanner(source);
        HTMLscanner.htmlRun(new HTMLWriter(sourceName.substring(sourceName.lastIndexOf(File.separatorChar)).replace(".tri", "")));
        System.out.println("Syntactic Analysis ...");
        source = new SourceFile(sourceName);
        Scanner scanner = new Scanner(source);
        report = new IDEReporter();
        Parser parser = new Parser(scanner, report);
        boolean success = false;
        
        rootAST = parser.parseProgram();
        if (report.numErrors == 0) {

            writeXMLProgram(rootAST, sourceName.substring(sourceName.lastIndexOf(File.separatorChar)).replace(".tri", ""));

            System.out.println("Contextual Analysis ...");
            Checker checker = new Checker(report);
            checker.check(rootAST);
            
            if (report.numErrors == 0) {
                System.out.println("Code Generation ...");
                Encoder encoder = new Encoder(report);
                encoder.encodeRun(rootAST, true);
                
                if (report.numErrors == 0) {
                    encoder.saveObjectProgram(sourceName.replace(".tri", ".tam"));
                    success = true;
                }
            }
        }

        if (success)
            System.out.println("Compilation was successful.");
        else
            System.out.println("Compilation was unsuccessful.");
        
        return(success);
    }
      
    /**
     * Returns the line number where the first error is.
     * @return Line number.
     */
    public int getErrorPosition() {
        return(report.getFirstErrorPosition());
    }
        
    /**
     * Returns the root Abstract Syntax Tree.
     * @return Program AST (root).
     */
    public Program getAST() {
        return(rootAST);
    }
    // </editor-fold>

    private void writeXMLProgram(Program programAST, String sourceName){
        XMLWriter xmlWriter = new XMLWriter(programAST);

        //Write the output files
        xmlWriter.writeProgramAST(sourceName);
    }
}
