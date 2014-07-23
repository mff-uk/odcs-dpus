package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Ruian;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import java.util.*;

/**
 * Convert list of {@link Requirement} into string query. Also add limit clause
 * to 12 - to restrict possible result size.
 *
 * @author Škoda Petr
 */
public class RequirementsToQuery {

    public List<Query> convert(List<Requirement> requirementsList) {
        // first aggreagate requirements
        Map<Subject, Map<String, Set<String>>> requirementsMap = new HashMap<>();
        for (Requirement item : requirementsList) {
            if (!requirementsMap.containsKey(item.getSubject())) {
                requirementsMap.put(item.getSubject(),
                        new HashMap<String, Set<String>>());
            }
            final Map<String, Set<String>> predObject
                    = requirementsMap.get(item.getSubject());
            if (!predObject.containsKey(item.getPredicate())) {
                predObject.put(item.getPredicate(), new HashSet<String>());
            }
            final Set<String> objects = predObject.get(item.getPredicate());
            if (!objects.add(item.getObject())) {
                // there already was object of same name
            }
        }
        // convert requirementsMap to queries
        final List<Query> queries = new LinkedList<>();
        // put one initial query inside
        queries.add(new Query());
        // 
        for (Subject s : requirementsMap.keySet()) {
            final Map<String, Set<String>> sData = requirementsMap.get(s);
            for (String p : sData.keySet()) {
                final Set<String> pData = sData.get(p);
                if (pData.size() == 1) {
                    // add just one option
                    addToQueries(queries, s, p, pData.iterator().next());
                } else {
                    // add multiple variants                    
                    final Iterator<String> iter = pData.iterator();
                    final List<Query> toAdd = new LinkedList<>();
                    String value = iter.next();
                    do {
                        // copy and add
                        final List<Query> newQ = deepCopy(queries);
                        addToQueries(newQ, s, p, value);
                        toAdd.addAll(newQ);
                        // get next - as there are more then two items
                        // this is ok
                        value = iter.next();
                    } while (iter.hasNext());
                    // add for value
                    addToQueries(queries, s, p, value);
                    // merge
                    queries.addAll(toAdd);
                }
            }
        }
        // generate alternative
        queries.addAll(alternativeLandAndHouseNumber(queries));
        queries.addAll(alternativeHouseNumber(queries));
        queries.addAll(alternativePsc(queries));
        // add connection between triples in query
        final List<Query> result = new LinkedList<>();
        for (Query q : queries) {
            if (q.getContent().isEmpty()) {
                continue;
            }
            result.addAll(addConnections(q));
        }
        return result;
    }

    /**
     * Return list of new queries as alternatives to given. The alternative is
     * created by removing the PSC.
     *
     * @param queries
     * @return
     */
    private List<Query> alternativePsc(List<Query> queries) {
        return alternativeRemove(queries, Subject.ADRESNI_MISTO, "<" + Ruian.P_PSC + ">");
    }

    private List<Query> alternativeLandAndHouseNumber(List<Query> queries) {
        final List<Query> toAdd = new LinkedList<>();
        for (Query q : queries) {
            if (q.getContent().containsKey(Subject.ADRESNI_MISTO)) {
                final List<PredicatObject> predObjList
                        = q.getContent().get(Subject.ADRESNI_MISTO);
                PredicatObject landNumber = null;
                PredicatObject houseNumber = null;
                for (PredicatObject item : predObjList) {
                    // TODO move remove somehow the direct string
                    if (item.predicate.compareTo("<" + Ruian.P_CISLO_DOMOVNI + ">") == 0) {
                        landNumber = item;
                    }
                    if (item.predicate.compareTo("<" + Ruian.P_CISLO_ORIENTACNI + ">") == 0) {
                        houseNumber = item;
                    }
                    
                }
                if (landNumber != null && houseNumber != null) {
                    // modify - switch land and house 
                    Query newQuery = new Query(q, false, true);
                    // remove old
                    newQuery.getContent().get(Subject.ADRESNI_MISTO).remove(
                            landNumber);
                    newQuery.getContent().get(Subject.ADRESNI_MISTO).remove(
                            houseNumber);
                    // add new
                    addToQuery(newQuery, Subject.ADRESNI_MISTO, 
                            landNumber.predicate, houseNumber.object);
                    addToQuery(newQuery, Subject.ADRESNI_MISTO, 
                            houseNumber.predicate, landNumber.object);
                    // add to the list
                    toAdd.add(newQuery);
                } else if (landNumber != null) {
                    // but houseNumber is null
                    Query newQuery = new Query(q, false, true);
                    // remove old
                    newQuery.getContent().get(Subject.ADRESNI_MISTO).remove(
                            landNumber);
                    addToQuery(newQuery, Subject.ADRESNI_MISTO, 
                            "<" + Ruian.P_CISLO_ORIENTACNI + ">", landNumber.object);
                    // add to the list
                    toAdd.add(newQuery);                   
                }
            }
        }
        return toAdd;
    }

    private List<Query> alternativeHouseNumber(List<Query> queries) {
        return alternativeRemove(queries, Subject.ADRESNI_MISTO, 
                "<" + Ruian.P_CISLO_ORIENTACNI + ">");
    }
    
    /**
     * Create alternatives by removing triple with given subject and object.
     * 
     * @param queries
     * @param subject
     * @param predicate
     * @return 
     */
    private List<Query> alternativeRemove(List<Query> queries, Subject subject, String predicate) {
        final List<Query> toAdd = new LinkedList<>();
        for (Query q : queries) {
            if (q.getContent().containsKey(subject)) {
                List<PredicatObject> predObjList = q.getContent().get(subject);
                PredicatObject houseNumberObject = null;
                for (PredicatObject item : predObjList) {
                    if (item.predicate.compareTo(predicate) == 0) {
                        houseNumberObject = item;
                        break;
                    }
                }
                if (houseNumberObject != null) {
                    // modify 
                    Query newQuery = new Query(q, true, false);
                    newQuery.getContent().get(subject).remove(houseNumberObject);
                    if (newQuery.getContent().get(subject).isEmpty()) {
                        newQuery.getContent().remove(subject);
                    }
                    
                    if (!newQuery.getContent().isEmpty()) {
                        // add only if not empty
                        toAdd.add(newQuery);
                    }
                }
            }
        }
        return toAdd;
    }    
    
    /**
     * Create deep copy. Copy as non alternatives and non variants.
     * 
     * @param qList
     * @return 
     */
    private List<Query> deepCopy(List<Query> qList) {
        final List<Query> newQList = new LinkedList<>();
        for (Query q : qList) {
            newQList.add(new Query(q, false, false));
        }
        return newQList;
    }

    private void addToQueries(List<Query> queries, Subject s, String p, Subject o) {
        addToQueries(queries, s, p, o.getValueName());
    }    
    
    private void addToQueries(List<Query> queries, Subject s, String p, String o) {
        for (Query q : queries) {
            addToQuery(q, s, p, o);
        }
    }

    private void addToQuery(Query q, Subject s, String p, String o) {
        if (!q.getContent().containsKey(s)) {
            q.getContent().put(s, new LinkedList<PredicatObject>());
        }
        q.getContent().get(s).add(new PredicatObject(p, o));
    }

    private List<Query> addConnections(Query q) {
        final List<Query> result = new LinkedList<>();
        result.add(q);
        // get min and max level
        int minLevel = Integer.MAX_VALUE;
        int maxLevel = Integer.MIN_VALUE;
        for (Subject s : q.getContent().keySet()) {
            final Integer level = s.getLevel();
            
            if (level == null) {
                continue;
            }
            
            if (minLevel > level) {
                minLevel = level;
            }
            if (maxLevel < level) {
                maxLevel = level;
            }
        }
        
        if (q.getContent().containsKey(Subject.CASTIOBCI)) {
            // add mapping 
            // ADRESNI_MISTO --> STAVEBNI_OBJEKT --> CASTI_OBCE
            addToQueries(result, Subject.ADRESNI_MISTO, 
                                "<" + Ruian.P_STAVEBNI_OBJEKT + ">",
                                Subject.STAVEBNI_OBJEKT);
            addToQueries(result, Subject.STAVEBNI_OBJEKT, 
                                "<" + Ruian.P_CAST_OBCE + ">",
                                Subject.CASTIOBCI);
        }
        
        // add mapping, we are the only one who can ..
        for (int level = minLevel; level < maxLevel; ++level) {
            // goes level = min level up to maxLevel - 1            
            switch (level) {
                case 0: // ADRESNI_MISTO --> ULICE, STAVEBNI_OBJEKT
                    if (q.getContent().containsKey(Subject.ULICE)) { 
                        // connecto to ulice
                        addToQueries(result, Subject.ADRESNI_MISTO, 
                                "<" + Ruian.P_ULICE + ">",
                            Subject.ULICE);
                    } else {
                        // else stavebniObjekt is used
                        addToQueries(result, Subject.ADRESNI_MISTO, 
                                "<" + Ruian.P_STAVEBNI_OBJEKT + ">",
                                Subject.STAVEBNI_OBJEKT);
                    }                    
                    break;
                case 1: // ULICE --> OBEC, STAVEBNI_OBJEKT --> CASTI_OBCE --> OBEC
                    if (q.getContent().containsKey(Subject.ULICE)) {
                        // use ulice to bind to obec
                        addToQueries(result, Subject.ULICE, 
                                "<" + Ruian.P_OBEC + ">", Subject.OBEC);
                    }
                    if (q.getContent().containsKey(Subject.CASTIOBCI)) {
                        addToQueries(result, Subject.CASTIOBCI, 
                                "<" + Ruian.P_OBEC + ">", Subject.OBEC);
                    }
                    
                    if (!q.getContent().containsKey(Subject.ULICE) && 
                            !q.getContent().containsKey(Subject.CASTIOBCI)) {
                        // CASTIOBCE is not presented
                        // but we do not know it they are not used to map 
                        // for obec or not -> so ve create two alternatives
                        
                        final List<Query> alternatives = deepCopy(result);
                        // STAVEBNI_OBJEKT --> CASTI_OBCE --> OBEC
                        addToQueries(alternatives, Subject.STAVEBNI_OBJEKT, 
                                "<" + Ruian.P_CAST_OBCE + ">", Subject.CASTIOBCI);
                        addToQueries(alternatives, Subject.CASTIOBCI, 
                            "<" + Ruian.P_OBEC + ">", Subject.OBEC);
                        // STAVEBNI_OBJEKT --> OBEC
                        addToQueries(result, Subject.STAVEBNI_OBJEKT, 
                            "<" + Ruian.P_OBEC + ">", Subject.OBEC);                            
                        // merge
                        result.addAll(alternatives); 
                    }
                    break;
                case 2: // OBEC --> POU
                    addToQueries(result, Subject.OBEC, 
                            "<" + Ruian.P_POU + ">", Subject.POU);
                    break;
                case 3: // POU --> ORP
                    addToQueries(result, Subject.POU, 
                            "<" + Ruian.P_ORP + ">", Subject.ORP);
                    break;
                case 4:
                    addToQueries(result, Subject.ORP, 
                            "<" + Ruian.P_VUSC + ">", Subject.VUSC);
                    break;
            }
        }
        return result;
    }
}
