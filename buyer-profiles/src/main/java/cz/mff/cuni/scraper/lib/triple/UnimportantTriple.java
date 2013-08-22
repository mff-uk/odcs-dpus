/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mff.cuni.scraper.lib.triple;

import cz.mff.cuni.scraper.lib.selector.Selector;
import java.net.URL;

/**
 *
 * @author Jakub
 */
public class UnimportantTriple extends Triple{

    // -------------------------------------------------------------------------
    // Inherited constructors
    
    public UnimportantTriple(String property, String literal, Boolean qname) {
        super(property, literal, qname);
    }

    public UnimportantTriple(String string, URL uRL) {
        super(string, uRL);
    }

    public UnimportantTriple(String property, Triple triple) {
        super(property, triple);
    }

    public UnimportantTriple(String property, Selector literal) {
        super(property, literal);
    }

    public UnimportantTriple(String property, Selector literal, String language, String datatype) {
        super(property, literal, language, datatype);
    }

    public UnimportantTriple(String property, String literal, String language, String datatype, Boolean qname) {
        super(property, literal, language, datatype, qname);
    }

    public UnimportantTriple(String property, String literal) {
        super(property, literal);
    }

    public UnimportantTriple(Triple... params) {
        super(params);
    }
    
    
    /**
     * Returns false. If triple contains only UnimportantTriple instances it is not printed.
     * 
     * @return False.
     */
    @Override
    public boolean isImportant() {
        return false;
    }
    
}
