package cz.cuni.mff.xrg.uv.transformer.tabular.column;

/**
 * This class is versioned in name because it's used in configuration.
 *
 * @author Škoda Petr
 */
public class ColumnInfo_V1 {

    /**
     * Used column URI.
     */
    private String URI = null;

    /**
     * Final column type.
     */
    private ColumnType type = ColumnType.Auto;

    /**
     * If true then we use information from Dfb to determine data type.
     */
    private Boolean useTypeFromDfb = null;

    /**
     * If {@link #type} is {@link ColumnType#String} then this value is used to
     * specify language.
     */
    private String language = null;

    public ColumnInfo_V1() {
    }

    public ColumnInfo_V1(String URI, ColumnType type) {
        this.URI = URI;
        this.type = type;
    }

    public ColumnInfo_V1(String URI) {
        this.URI = URI;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public Boolean isUseTypeFromDfb() {
        return useTypeFromDfb;
    }

    public void setUseTypeFromDfb(Boolean useTypeFromDfb) {
        this.useTypeFromDfb = useTypeFromDfb;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
