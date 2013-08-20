/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mff.cuni.scraper.lib.triple;

import cz.mff.cuni.scraper.lib.generator.TemplateURIGenerator;
import cz.mff.cuni.scraper.lib.selector.CssSelector;
import java.net.URL;
import java.util.LinkedList;

/**
 * One triple with URI defined by URIGenerator. The stored triples has to be printed directly.
 * 
 * @author Jakub Starka
 */
public class StandaloneTriple extends Triple {

    TemplateURIGenerator generator = null;
    String uri = null;
    
    // -------------------------------------------------------------------------
    // Inherited constructors
    
    public StandaloneTriple(TemplateURIGenerator generator, Triple... params) {
        super(params);
        this.generator = generator;
    }

    public StandaloneTriple(String uri, String property, CssSelector literal) {
        super(property, literal);
        this.uri = uri;
    }
    
    public StandaloneTriple(TemplateURIGenerator generator, String property, CssSelector literal) {
        super(property, literal);
        this.generator = generator;
    }

    public StandaloneTriple(TemplateURIGenerator generator, String property, String literal) {
        super(property, literal);
        this.generator = generator;
    }

    public StandaloneTriple(String uri, String property, Triple literal) {
        super(property, literal);
        this.uri = uri;
    }
    
    public StandaloneTriple(TemplateURIGenerator generator, String property, Triple triple) {
        super(property, triple);
        this.generator = generator;
    }

    public StandaloneTriple(TemplateURIGenerator generator, String string, URL uRL) {
        super(string, uRL);
        this.generator = generator;
    }

    public StandaloneTriple(TemplateURIGenerator generator, String property, String literal, Boolean qname) {
        super(property, literal, qname);
        this.generator = generator;
    }

    public StandaloneTriple(TemplateURIGenerator generator, String property, CssSelector literal, String language, String datatype) {
        super(property, literal, language, datatype);
        this.generator = generator;
    }

    public StandaloneTriple(TemplateURIGenerator generator, String property, String literal, String language, String datatype, Boolean qname) {
        super(property, literal, language, datatype, qname);
        this.generator = generator;
    }

    // -------------------------------------------------------------------------
    // Print methods
    
    /**
     * Prints only the property and the generated URI. The triple is stored and has to be printed separatedly.
     * 
     * @see StandaloneTriple.popInstances
     * 
     */
    @Override
    public String print(String indent, String suffix) {
        String out = "";
	
        if (property != null && subject != null) {
	    String sub = subject.print("\t");
	    if (!sub.trim().equals("")) {
		if (!sub.contains("\n")) {
		    sub += "\n";
		}
		out = sub + indent;
                count ++;
	    }
	} else if (child != null && child.length != 0) {
            for (int i = 0; i < child.length; i ++) {
                String sub = child[i].print("\t");
                if (!sub.trim().equals("")) {
                    out += sub + " ;\n";
                }
            }
        }
	
        if (!out.trim().equals("")) {
            String url = null;
            if (uri == null) {
                if (!generator.isQName()) {
                    url = generator.getUrl().toString();
                    url = "<" + url + ">";
                } else {
                    url = generator.getQName();
                }
            } else {
                url = "<" + uri + ">";
            }
            out = out.substring(0, out.lastIndexOf(';')) + '.';
            StandaloneTriple.storedInstances.add(url + "\n" + out);
            count ++;
            return indent + property + " " + url + suffix;
	} else {
            return "";
        }
        
        
    }
    
    /* Triple storage */
    protected static LinkedList<String> storedInstances = new LinkedList<>();
    
    public static LinkedList<String> popInstances() {
        LinkedList<String> out = (LinkedList<String>)storedInstances.clone();
        storedInstances.clear();
        return out;
    }
    
    
    
}
