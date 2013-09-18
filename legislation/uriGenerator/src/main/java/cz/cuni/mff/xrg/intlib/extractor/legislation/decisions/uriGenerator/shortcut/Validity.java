/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub
 */
public class Validity {
    
    private TreeSet<Date> validity = new TreeSet<>();
    
    public Date getLastDate(Date current) {
        return validity.floor(current);
    }
    
    public void add(String date) throws ParseException {
        
        Date newDate;
        newDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date);
        add(newDate);
    }
    
    public void add(Date date) {
        if (!validity.contains(date)) {
            validity.add(date);
        }
    }
    
    public TreeSet<Date> getValidity() {
        return validity;
    }
    
    
}
