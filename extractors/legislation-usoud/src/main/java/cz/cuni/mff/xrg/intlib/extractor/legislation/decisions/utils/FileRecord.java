/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils;

/**
 *
 * @author tomasknap
 */
public class FileRecord {
    
    private String filePath;
    private String expression; 

    public FileRecord(String fileName, String expression) {
        this.filePath = fileName;
        this.expression = expression;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getExpression() {
        return expression;
    }
    
    
    
}
