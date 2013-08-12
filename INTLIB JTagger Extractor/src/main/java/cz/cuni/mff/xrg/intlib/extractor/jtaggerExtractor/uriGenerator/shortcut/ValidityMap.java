package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut;

import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link.*;
import static cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut.SparqlLoader.FILENAME;
import static cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut.SparqlLoader.getCacheDir;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jakub Starka
 */
public class ValidityMap {
 
    //public static final String EXPRESSION_LIST = "expressionList.csv";
     private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ValidityMap.class);
    
    
    private static TreeMap<Work, Validity> validityMap = new TreeMap<>();
    
    public static void load(ExtractContext context) throws FileNotFoundException, IOException {
        
         
//        //set the tempDir        
//        if (context != null) {  
//            SparqlLoader.tempDir = context.getWorkingDir();
//          
//        } else {
//            SparqlLoader.tempDir = new File(System.getProperty("java.io.tmpdir"));
//        }
//        logger.info("Tmp set as: " + context.getWorkingDir());
//        
        logger.debug("Taking file with expressions from: " + SparqlLoader.EXPRESSION_LIST);
        
        //FileReader fr = new FileReader(EXPRESSION_LIST); 
        FileReader fr = new FileReader(SparqlLoader.getFile(SparqlLoader.EXPRESSION_LIST)); 
        BufferedReader br = new BufferedReader(fr); 
        String s; 
        while((s = br.readLine()) != null) { 
            if (!s.contains("/")) {
                continue;
            }
            s = s.replaceAll("\"", "");
            // http://linked.opendata.cz/resource/legislation/cz/act/1918/1/version/cz/1918-11-02
            String[] parts = s.split("/");
            
            Work w = new Work("", parts[5], WorkType.getEnum(parts[6]), Integer.valueOf(parts[7]), parts[8], null, null, null, null, null, null);
            
            if (validityMap.containsKey(w)) {
                try {
                    validityMap.get(w).add(parts[11]);
                } catch (ParseException ex) {
                    Logger.getLogger(ValidityMap.class.getName()).log(Level.SEVERE, "Invalid date: " + s, ex);
                }
            } else {
                try {
                    Validity v = new Validity();
                    v.add(parts[11]);
                    validityMap.put(w, v);
                } catch (ParseException ex) {
                    Logger.getLogger(ValidityMap.class.getName()).log(Level.SEVERE, "Invalid date: " + s);                
                }
            }
            
        }
    }
 
    
    public static Date getValidity(Work w, Date issued) {
        if (w.getYear() == null || w.getNumber() == null) {
            return null;
        }
        
        Validity v = validityMap.get(w);
        if (v != null) {
            return v.getLastDate(issued);
        } 
        return null;
    }
    
    // -------------------------------------------------------------------------
    // Map textual dump
    
    public static void getInfo() {
        for (Entry<Work, Validity> e: validityMap.entrySet()) {
            ValidityMap.getInfo(e.getKey());
        }
    }    
    
    public static void getInfo(Work w) {
        
        if (!validityMap.containsKey(w)) {
            System.out.println("Validity not included.");
            return;
        }
        
        Validity v = validityMap.get(w);
        
        System.out.println(w.getNumber() + "/" + w.getYear());
        
        for (Date d: v.getValidity()) {
            System.out.println(" - " + d.toString());
        }
        
    }

    
}
