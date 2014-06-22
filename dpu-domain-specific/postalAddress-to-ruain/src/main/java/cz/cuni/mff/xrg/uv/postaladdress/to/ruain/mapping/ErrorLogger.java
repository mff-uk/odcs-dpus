package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import java.util.LinkedList;
import java.util.List;

/**
 * Gather informations about problems that arise during mapping.
 *
 * @author Škoda Petr
 */
public class ErrorLogger {

    private String uri = "";
    
    private final List<String> unused = new LinkedList<>();
    
    private final List<String> noOutput = new LinkedList<>();
    
    private final List<String> parseFaield = new LinkedList<>();
    
    private int failCounter = 0;
    
    public void start(String uri) { 
        this.uri = uri;
        this.unused.clear();
        this.noOutput.clear();
        this.parseFaield.clear();
        this.failCounter = 0;
    }

    public void failedToMap(String predicate, String object, String message) {
        failCounter++;
        parseFaield.add(predicate + " " + object + "\n\t\t" + message);
    }

    public void failedToMap(String predicate, String object, String message,
            Exception ex) {
        failCounter++;
        parseFaield.add(predicate + " " + object + "\n\t\t" + message + "\n\t\t" + 
                ex.getMessage());
    }    
    
    public void mapped(String predicate, String object,
            List<Requirement> generated) {
        if (generated.isEmpty()) {
            noOutput.add(predicate + " " + object);
        }
    }

    public void unused(String predicate, String object) {
        unused.add(predicate + " " + object);
    }
    
    public void end() {

    }

    public String getStringInfo() {
        final StringBuilder info = new StringBuilder();
    
        info.append("Ununsed:");
        for (String item : unused) {
            info.append("\n\t");
            info.append(item);
        }

        info.append("\nParseFailed:");
        for (String item : parseFaield) {
            info.append("\n\t");
            info.append(item);
        }       
        
        return info.toString();
    }
    
    public boolean hasFailed() {
        return failCounter > 0;
    }
    
}
