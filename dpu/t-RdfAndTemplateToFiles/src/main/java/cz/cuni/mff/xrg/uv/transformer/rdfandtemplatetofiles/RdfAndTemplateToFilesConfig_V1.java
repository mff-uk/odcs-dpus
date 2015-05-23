package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles;

/**
 * Configuration class for RdfAndTemplateToFiles.
 *
 * @author Petr Škoda
 */
public class RdfAndTemplateToFilesConfig_V1 {

    private String template = "Value: ${http://localhost/value}";

    private boolean softFail = true;

    public RdfAndTemplateToFilesConfig_V1() {

    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isSoftFail() {
        return softFail;
    }

    public void setSoftFail(boolean softFail) {
        this.softFail = softFail;
    }

}
