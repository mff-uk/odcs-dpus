/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomasknap
 */
public class Utils {
    
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    
     /**
         * Reads content of a file to string
         * @param path Path to a file
         * @param encoding Encoding 
         * @return Content of a file
         */
	public static String readFile(String path, Charset encoding) {
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException ex) {
			log.warn("Cannot read the file {}", Paths.get(path));
			log.debug(ex.getLocalizedMessage());
			return null;
		}
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
    
    
}
