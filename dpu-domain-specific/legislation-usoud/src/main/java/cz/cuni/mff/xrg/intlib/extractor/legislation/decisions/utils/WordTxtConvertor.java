/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Extractor;
import static cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Extractor.fromStream;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.slf4j.LoggerFactory;
import eu.unifiedviews.dpu.DPUContext;

/**
 *
 * @author tomasknap
 */
public class WordTxtConvertor {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            Extractor.class);

    private String workingDir; 
    private DPUContext context;
    private String jarContentPath;
    


    WordTxtConvertor(DPUContext context) {
        this.context = context;
    }

    WordTxtConvertor(DPUContext context, String jarContentPath) {
         this.context = context;
         this.jarContentPath = jarContentPath;
    }
    
    
    
    /**
     * Converts the given word file content to txt file 
     * @param wordFile File to be processed 
     * @param processedFileId Id of the processed file used when creating temporary file input for catdoc
     * @return Output of catdoc
     */
    public String convertToTxt(String wordFile, String processedFileId) {
    
        
           //STEP 1: Convert WORD to TXT
           String pathToCatDoc =  jarContentPath + File.separator + "src/main/resources/";
           String filePath = context.getWorkingDir() + File.separator + processedFileId;
           Utils.storeStringToTempFile(wordFile, filePath, Charset.forName("Cp1250"));
            log.info("Catdoc is about to be executed");
            
            //String txtFilePath = context.getWorkingDir() + File.separator + processedFileName + ;
            
            //adjust the permissions, if needed, to execute catdoc
            try {
                    Runtime.getRuntime().exec("chmod +x " + pathToCatDoc + "catdoc");
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }
            
            try {
                
                //TODO launching catdoc which must be installed on the target system! 
                //To launch catdoc shipped with the extractor DPU, use: pathToCatDoc + "catdoc ... "
                String catdocCommand = "catdoc -scp1250 -dcp1250 " + filePath;
                
                log.info("AAbout to execute: {}", catdocCommand);
//                Process p = Runtime.getRuntime().exec(pathToCatDoc + "catdoc -sutf-8 -dcp1250 " + filePath); 
                  Process p = Runtime.getRuntime().exec(catdocCommand); 
               
                Extractor.printProcessErrorOutput(p);
   
                String f = fromStream(p.getInputStream(), Charset.forName("Cp1250"));
                
                log.info("Catdoc was executed successfully");
                return f;
            } catch (IOException ex) {
                log.warn(ex.getLocalizedMessage());
                log.warn("Problem executing catdoc: "
                        + ex.getMessage());
               
            }
            
            return null;
    
    }

  
}
