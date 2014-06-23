package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

/**
 *
 * @author Škoda Petr
 */
public enum Subject {
    VUSC("?vusc", "r:Vusc", 5),
    ORP("?orp", "r:Orp", 4),
    POU("?pou", "r:Pou", 3),
    OBEC("?obec", "r:Obec", 2),
    ULICE("?ulice", "r:Ulice", 1),
    ADRESNI_MISTO("?adrMisto", "r:AdresniMisto",  0);    
    
    private final String text;

    private final String type;
    
    private final int level;
    
    private Subject(String text, String type, int level) {
        this.text = text;
        this.type = type;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }
    
}
