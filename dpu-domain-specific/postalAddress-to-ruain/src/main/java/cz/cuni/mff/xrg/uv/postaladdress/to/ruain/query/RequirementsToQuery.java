package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import java.util.*;

/**
 * Convert list of {@link Requirement} into string query. Also add
 * limit clause to 12 - to restrict possible result size.
 *
 * @author Škoda Petr
 */
public class RequirementsToQuery {

    private final static String SELECT_PREAMBLE = "PREFIX r: <http://ruian.linked.opendata.cz/ontology/>\n"
            + "PREFIX s: <http://schema.org/>\n"
            + "SELECT ";
    
    private final static String SELECT_WHERE = " WHERE {\n";

    private final static String SELECT_END = "} LIMIT 12";
    
    private class PredicatObject {

        public String predicate;

        public String object;

        public PredicatObject(Requirement req) {
            this.predicate = req.getPredicate();
            this.object = req.getObject();
        }

        public PredicatObject(String predicate, String object) {
            this.predicate = predicate;
            this.object = object;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PredicatObject) {
                PredicatObject other = (PredicatObject) obj;
                return predicate.equals(other.predicate) && object.equals(
                        other.object);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.predicate);
            hash = 29 * hash + Objects.hashCode(this.object);
            return hash;
        }
    }

    /**
     * Store aggregated requirements.
     */
    private final Map<Subject, List<PredicatObject>> requirements = new HashMap<>();

    public String convert(List<Requirement> requirementsList) {
        int minLevel = Integer.MAX_VALUE;
        int maxLevel = Integer.MIN_VALUE;
        String minLevelName = "*";
        // convert into agregated requirements
        for (Requirement req : requirementsList) {
            // secure aggregation for given triples
            List<PredicatObject> agregList = secureList(req.getSubject());
            // ..
            addIfNew(agregList, new PredicatObject(req));
            // update levels
            final int level = req.getSubject().getLevel();
            if (minLevel > level) {
                minLevel = level;
                minLevelName = req.getSubject().getText();
            }
            if (maxLevel < level) {
                maxLevel = level;
            }
        }
        // add dependency mapping, go down-up
        for (int level = minLevel; level < maxLevel; ++level) {
            // goes level = min level up to maxLevel - 1            
            switch (level) {
                case 0:
                    addIfNew(secureList(Subject.ADRESNI_MISTO), 
                            new PredicatObject("r:ulice", Subject.ULICE.getText()));
                    break;
                case 1:
                    addIfNew(secureList(Subject.ULICE),
                            new PredicatObject("r:obec", Subject.OBEC.getText()));
                    break;
                case 2:
                    addIfNew(secureList(Subject.OBEC),
                            new PredicatObject("r:pou", Subject.POU.getText()));
                    break;
                case 3:
                    addIfNew(secureList(Subject.POU),
                            new PredicatObject("r:orp", Subject.ORP.getText()));
                    break;
                case 4:
                    addIfNew(secureList(Subject.ORP),
                            new PredicatObject("r:vusc", Subject.VUSC.getText()));
                    break;
            }
        }
        // convert to string
        StringBuilder result = new StringBuilder(SELECT_PREAMBLE);
        // put name of the result in the query
        result.append(minLevelName);
        result.append(SELECT_WHERE);
        
        for (Subject key : requirements.keySet()) {
            result.append(key.getText());
            // add type information
            result.append(" rdf:type ");
            result.append(key.getType());
            
            for (PredicatObject predObj : requirements.get(key)) {
                result.append(";\n");
                result.append("    ");
                result.append(predObj.predicate);
                result.append(" ");
                result.append(predObj.object);                
            }
            result.append(".\n");
        }
        // clear - prepare for next use
        requirements.clear();        
        // return string
        result.append(SELECT_END);
        return result.toString();
    }

    private List<PredicatObject> secureList(Subject subject) {
        if (!requirements.containsKey(subject)) {
            requirements.put(subject,
                    new LinkedList<PredicatObject>());
        }
        return requirements.get(subject);
    }

    private void addIfNew(List<PredicatObject> list,
            PredicatObject toAdd) {
        if (!list.contains(toAdd)) {
            list.add(toAdd);
        } else {
            // ignore duplicit triples
        }
    }

}
