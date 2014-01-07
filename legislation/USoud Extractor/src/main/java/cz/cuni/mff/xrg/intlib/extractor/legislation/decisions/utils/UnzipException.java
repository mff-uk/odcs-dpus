/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils;

/**
 *
 * @author tomasknap
 */
public class UnzipException extends Exception {
	
    public UnzipException() {
    	super("Invalid configuration.");
    }

    public UnzipException(Throwable cause) {
        super(cause);
    }

    public UnzipException(String message) {
        super(message);
    }

    public UnzipException(String message, Throwable cause) {
        super(message, cause);
    }    


}
