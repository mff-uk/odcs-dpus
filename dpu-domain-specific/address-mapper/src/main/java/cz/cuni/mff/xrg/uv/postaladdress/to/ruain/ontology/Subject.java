package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology;

/**
 * Used ruian ontology definition.
 * 
 * @author Škoda Petr
 */
public enum Subject {
    VUSC("?vusc", "r:Vusc", "vusc", 5),
    ORP("?orp", "r:Orp", "orp", 4),
    POU("?pou", "r:Pou", "pou", 3),
    OBEC("?obec", "r:Obec", "obec", 2),
    ULICE("?ulice", "r:Ulice", "ulice", 1),
    ADRESNI_MISTO("?adrMisto", "r:AdresniMisto", "adresni-misto", 0);    
    
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
