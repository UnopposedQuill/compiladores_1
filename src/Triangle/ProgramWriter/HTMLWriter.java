
package Triangle.ProgramWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HTMLWriter {

    private FileWriter fileWriter;

    public HTMLWriter(String sourceName) {
        //Create the output dir in case of needed
        File dir = new File("output" + File.separator);
        dir.mkdirs();

        //The HTML file to write into
        //File htmlFile = new File(dir, "source_program.html");
        File htmlFile = new File(dir, sourceName.concat(".html"));

        //Helper file writer class
        try{

            //The HTML visitor writes to file with the fileWriter
            fileWriter = new FileWriter(htmlFile);

            writeToHTMLFile("<!DOCTYPE html>");
            writeToHTMLFile("\n");
            writeToHTMLFile("<html>");
            writeToHTMLFile("\n");

            writeToHTMLFile("<p style=\"font-family: monospace; font-size:160%;\">");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeKeyWord(String keyword){
        writeToHTMLFile("<b>"+keyword+"</b>");
    }

    public void writeElse(String word){
        writeToHTMLFile(word);
    }

    public void writeLiteral(String word){
        writeToHTMLFile("<span style=\"color:blue\">" + word + "</span>");
    }

    public void writeComment(String comment){
        writeToHTMLFile("<span style=\"color:green\">" + comment + "</span><br>\n");
    }

    public void finishHTML(){
        writeToHTMLFile("</p>" + "\n" +
                "</html>");
        try {
            fileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void writeToHTMLFile(String content){
        try {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
