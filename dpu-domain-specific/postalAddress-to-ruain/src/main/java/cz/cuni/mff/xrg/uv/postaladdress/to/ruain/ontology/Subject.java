package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology;

/**
 * Used ruian ontology definition.
 * 
 * @author Škoda Petr
 */
public enum Subject {
    VUSC("?vusc", "r:Vusc", "http://ruian.linked.opendata.cz/ontology/links/vusc", 5),
    ORP("?orp", "r:Orp", "http://ruian.linked.opendata.cz/ontology/links/orp", 4),
    POU("?pou", "r:Pou", "http://ruian.linked.opendata.cz/ontology/links/pou", 3),
    OBEC("?obec", "r:Obec", "http://ruian.linked.opendata.cz/ontology/links/obec", 2),
    ULICE("?ulice", "r:Ulice", "http://ruian.linked.opendata.cz/ontology/links/ulice", 1),
    ADRESNI_MISTO("?adrMisto", "r:AdresniMisto", "http://ruian.linked.opendata.cz/ontology/links/okres", 0);    
    
    private final String text;

    private final String type;
    
    private final String relation;
    
    private final int level;
    
    private Subject(String text, String type, String relation, int level) {
        this.text = text;
        this.type = type;
        this.relation = relation;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getRelation() {
        return relation;
    }
    
    public int getLevel() {
        return level;
    }
    
}
