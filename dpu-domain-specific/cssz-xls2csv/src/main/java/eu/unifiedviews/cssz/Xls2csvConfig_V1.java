package eu.unifiedviews.cssz;

/**
 * DPU's configuration class.
 * 
 * 
TEMPLATE_PREFIX=SABLONA_
PREFIX=\\%\\%
SUFFIX=\\%\\%
 * 
 */
public class Xls2csvConfig_V1 {

//    private String filepath = "";
    private String template_prefix = "SABLONA_";
    private String prefix = "\\\\%\\\\%";
    private String suffix = "\\\\%\\\\%";

//    public String getFilepath() {
//        return filepath;
//    }
//
//    public void setFilepath(String filepath) {
//        this.filepath = filepath;
//    }

    public String getTemplate_prefix() {
        return template_prefix;
    }

    public void setTemplate_prefix(String template_prefix) {
        this.template_prefix = template_prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public Xls2csvConfig_V1() {

    }

}
