/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.scraper.lib.triple;

import cz.cuni.mff.scraper.lib.selector.Selector;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Simple Triple class.
 * 
 * @author Jakub Starka
 */
public class Triple {

    public static int count;
    
    protected Triple[] child = null;
    protected String property = null;
    protected String literal = null;
    protected Triple subject = null;
    protected String datatype = null;
    protected String language = null;
    protected Boolean qname = false;
    protected URL url = null;
    
    /**
     * Set if Triples
     * 
     * @param params Set of Triples.
     */    
    public Triple(Triple ... params) {
        child = params;
    }

    /**
     * Triple with defined property and literal object.
     * 
     * @param property Triple property. Always qname.
     * @param literal Literal object.
     */
    public Triple(String property, String literal) {
	this(property, literal, null, null, false);
    }
    
    protected static final HashMap<String, Integer> usageMap = new HashMap<>();

    public static void report() {
        for (Map.Entry<String, Integer> item: usageMap.entrySet()) {
            System.out.println(String.format("%1$-30s", item.getKey()) + ":\t" + item.getValue());
        }
    }
    
    /**
     * Triple with defined property, literal and optional language or datatype.
     * 
     * When language and datatype are specified only language is used.
     * 
     * @param property Triple property. Always qname.
     * @param literal Literal object.
     * @param language Language specification.
     * @param datatype Datatype specification.
     * @param qname Object is not a literal, it is a qname.
     */
    public Triple(String property, String literal, String language, String datatype, Boolean qname) {
	this.property = property;
	this.literal = literal;
	this.language = language;
	this.datatype = datatype;
	this.qname = qname;
        
        if (property != null && !usageMap.containsKey(property)) {
            usageMap.put(property, 0);
        }
    }

    /**
     * Triple with defined property, literal and optional language or datatype.
     * 
     * When language and datatype are specified only language is used.
     * 
     * @param property Triple property. Always qname.
     * @param literal Selector to get literal object.
     * @param language Language specification.
     * @param datatype Datatype specification.
     */
    public Triple(String property, Selector literal, String language, String datatype) {
	this(property, literal.getValue(), language, datatype, false);
    }

    /**
     * Triple with defined property and literal. No datatype or language specified.
     * 
     * @param property Triple property. Always qname.
     * @param literal Selector to get literal object.
     */
    public Triple(String property, Selector literal) {
	this(property, literal, null, null);
    }

    /**
     * Triple with defined property and blank node object.
     * 
     * @param property Triple property. Always qname.
     * @param triple Blank node content.
     */
    public Triple(String property, Triple triple) {
	this.property = property;
	this.subject = triple;
        
        if (property != null && !usageMap.containsKey(property)) {
            usageMap.put(property, 0);
        }
    }

    /**
     * Triple with defined property and URI object.
     * 
     * @param string
     * @param uRL 
     */
    public Triple(String string, URL uRL) {
	this.property = string;
	this.url = uRL;
        
        if (property != null && !usageMap.containsKey(property)) {
            usageMap.put(property, 0);
        }
    }
    
    /**
     * Triple with defined property and literal. No language or datatype specified.
     * 
     * @param property Triple property. Always qname.
     * @param literal Literal object.
     * @param qname Object is not a literal, it is a qname.
     */
    public Triple(String property, String literal, Boolean qname) {
	this(property, literal, null, null, qname);
    }

    /**
     * Property getter.
     * 
     * @return Property qname.
     */
    public String getProperty() {
        return property;
    }
    
    /**
     * Print this triple.
     * 
     * @return Turtle output of this triple.
     */
    public String print() {
	return this.print("", "");
    }

    /**
     * Print this triple with specified indent.
     * 
     * @param indent Indent for each line of the output.
     * @return Turtle output of this triple.
     */
    public String print(String indent) {
	return this.print(indent, "");
    }

    /**
     * Print this triple with specified indent and suffix.
     * 
     * @param indent Indent for each line of the output.
     * @param suffix Suffix after last line of the output.
     * @return Turtle output of this triple.
     */
    public String print(String indent, String suffix) {
	String out = "";
        String outUI = "";
	//System.out.println(property + " " + literal);
	if (property != null && literal != null && !literal.equals("")) {
	    if (this.qname) {
		out = indent + property + " " + literal;
	    } else {
		out = indent + property + " \"" + literal.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"") + "\"";
	    }
	    if (language != null) {
		out += "@" + language;
	    } else if (datatype != null) {
		out += "^^" + datatype;
	    }
            count ++;
	} else if (property != null && url != null) {
            try {
                url.toURI();
                count ++;
                out = indent + property + " <" + url + ">";
            } catch (URISyntaxException ex) {
                System.out.println(url + " not valid.");
                //Logger.getLogger(Triple.class.getName()).log(Level.SEVERE, null, ex);
            }
	} else if (property != null && subject != null) {
	    String sub = subject.print(indent + "\t");
	    if (!sub.trim().equals("")) {
		if (!sub.contains("\n")) {
		    sub += "\n";
		}
                count ++;
		out = indent + property + " [\n" + sub + indent + "]";
	    }
	} else if (child != null && child.length != 0) {
            for (int i = 0; i < child.length; i ++) {
                if (child[i] == null) {
                    System.out.println("damed");
                }
                String sub = child[i].print(indent + "\t");
                if (!sub.trim().equals("")) {
                    if (child[i].isImportant()) {
                        out += sub + " ;\n";
                    } else {
                        outUI += sub + " ;\n";
                    }
                }
            }
	}
	if (out.trim().equals("")) {
	    return "";
	} else {
            if (property != null) {
                usageMap.put(property, usageMap.get(property) + 1);
            }
            
	    return out + outUI + suffix;
	}
    }

    
       
    /**
     * Always returns true. Always should be printed.
     * 
     * @return True.
     */
    public boolean isImportant() {
        return true;
    }
    
}
