/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mff.cuni.scraper.lib.triple;

import java.util.LinkedList;

/**
 * Triple to group multiple triples, ie. dynamic number of triples in final result.
 *
 * @author Jakub Starka
 */
public class Multitriple extends Triple {

    Triple[] triples;
    
    public Multitriple(String name, LinkedList<Triple> triples) {
        this.property = name;
        this.triples = new Triple[triples.size()];
        triples.toArray(this.triples);
    }
    
    public Multitriple(String name, Triple ... triples) {
        this.property = name;
        this.triples = triples;
    }

    @Override
    public String print(String indent, String suffix) {
        String out = "";
        String outUI = "";
        for (Triple t: triples) {
            String cur = t.print(indent, "");
            if (!cur.isEmpty()) {
                if (t.isImportant()) {
                    out += cur + " ;\n";
                } else {
                    outUI += cur + " ;\n";
                }
            }
        }
        if (!out.isEmpty()) {
            out = outUI + out.substring(0, out.length() - 3) + suffix;
        } else {
            out = "";
        }
        return out;
    }
}
