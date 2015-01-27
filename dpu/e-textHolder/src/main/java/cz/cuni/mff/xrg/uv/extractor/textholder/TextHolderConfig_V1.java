package cz.cuni.mff.xrg.uv.extractor.textholder;

import cz.cuni.mff.xrg.uv.boost.ontology.Ontology;

/**
 *
 * @author Škoda Petr
 */
@Ontology.Entity(type = "http://xrg.ksi.ms.mff.cuni.cz/unifiedviews/ontology/textHolder/Config")
public class TextHolderConfig_V1 {

    private String fileName = "triples.ttl";
    
    private String text = "<http://localhost/1> <<http://localhost/value> \"some text\"";

    public TextHolderConfig_V1() {

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
