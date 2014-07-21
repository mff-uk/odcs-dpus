package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology;

/**
 * Definition of supported geographical subjects.
 * 
 * @author Škoda Petr
 */
public enum Subject {
    VUSC("?vusc", Ruian.O_VUSC, Ruian.P_LINK_VUSC, 5),
    ORP("?orp", Ruian.O_ORP, Ruian.P_LINK_ORP, 4),
    POU("?pou", Ruian.O_POU, Ruian.P_LINK_POU, 3),
    OBEC("?obec", Ruian.O_OBEC, Ruian.P_LINK_OBEC, 2),
    CASTIOBCI("?castObce", Ruian.O_CAST_OBCE, Ruian.P_LINK_CAST_OBCE, null),
    ULICE("?ulice", Ruian.O_ULICE, Ruian.P_LINK_ULICE, 1),
    ADRESNI_MISTO("?adrMisto", Ruian.O_ADRESNI_MISTO, Ruian.P_LINK_ADRESNI_MISTO, 0),
    STAVEBNI_OBJEKT("?stavObjekt", Ruian.O_STAVEBNI_OBJEKT, "", null);
        
    private final String valueName;

    private final String className;
    
    private final String relation;
    
    private final Integer level;
    
    private Subject(String text, String className, String relation, Integer level) {
        this.valueName = text;
        this.className = className;
        this.level = level;
        this.relation = relation;
    }

    public String getValueName() {
        return valueName;
    }

    public String geClassName() {
        return className;
    }

    public String getRelation() {
        return relation;
    }

    public Integer getLevel() {
        return level;
    }
    
}
