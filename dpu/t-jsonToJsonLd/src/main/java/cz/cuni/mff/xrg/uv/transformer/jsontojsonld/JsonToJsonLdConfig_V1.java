package cz.cuni.mff.xrg.uv.transformer.jsontojsonld;

/**
 * Configuration class for JsonToJsonLd.
 *
 * @author Škoda Petr
 */
public class JsonToJsonLdConfig_V1 {

    private String context = "http://schema.org";

    private String encoding = "UTF-8";

    public JsonToJsonLdConfig_V1() {

    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
