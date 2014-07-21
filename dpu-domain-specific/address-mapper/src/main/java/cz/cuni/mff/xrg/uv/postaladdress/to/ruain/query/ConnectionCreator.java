package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Ruian;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import java.util.LinkedList;

/**
 *
 * @author Škoda Petr
 */
public class ConnectionCreator {

    public void addConnections(Query q) {
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
        // add mapping, we are the only one who can ..
        for (int level = minLevel; level < maxLevel; ++level) {
            // goes level = min level up to maxLevel - 1            
            switch (level) {
                case 0: // ADRESNI_MISTO --> ULICE, STAVEBNI_OBJEKT
                    if (q.getContent().containsKey(Subject.ULICE)) {
                        // connecto to ulice
                        addToQuery(q, Subject.ADRESNI_MISTO,
                                "<" + Ruian.P_ULICE + ">",
                                Subject.ULICE);
                    } else {
                        // use stavebniObjekt
                        addToQuery(q, Subject.ADRESNI_MISTO,
                                "<" + Ruian.P_STAVEBNI_OBJEKT + ">",
                                Subject.STAVEBNI_OBJEKT);
                    }
                    break;
                case 1: // ULICE --> OBEC, STAVEBNI_OBJEKT --> CASTI_OBCE --> OBEC
                    if (q.getContent().containsKey(Subject.ULICE)) {
                        // use ulice to bind to obec
                        addToQuery(q, Subject.ULICE, "<" + Ruian.P_OBEC + ">",
                                Subject.OBEC);
                    } else {
                        // use stavebni objekt
                        addToQuery(q, Subject.STAVEBNI_OBJEKT,
                                "<" + Ruian.P_CAST_OBCE + ">", Subject.CASTIOBCI);
                        addToQuery(q, Subject.CASTIOBCI,
                                "<" + Ruian.P_OBEC + ">", Subject.OBEC);
                    }
                    break;
                case 2: // OBEC --> POU
                    addToQuery(q, Subject.OBEC, "<" + Ruian.P_POU + ">",
                            Subject.POU);
                    break;
                case 3: // POU --> ORP
                    addToQuery(q, Subject.POU, "<" + Ruian.P_ORP + ">",
                            Subject.ORP);
                    break;
                case 4:
                    addToQuery(q, Subject.ORP, "<" + Ruian.P_VUSC + ">",
                            Subject.VUSC);
                    break;
            }
        }
    }

    private void addToQuery(Query q, Subject s, String p, Subject o) {
        addToQuery(q, s, p, o.getValueName());
    }    
    
    private void addToQuery(Query q, Subject s, String p, String o) {
        if (!q.getContent().containsKey(s)) {
            q.getContent().put(s, new LinkedList<PredicatObject>());
        }
        q.getContent().get(s).add(new PredicatObject(p, o));
    }

}
