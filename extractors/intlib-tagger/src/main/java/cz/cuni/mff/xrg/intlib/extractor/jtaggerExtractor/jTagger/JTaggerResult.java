package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger;

public class JTaggerResult {
    private String xml;
    private String html;

    public JTaggerResult() {
        this.xml = "";
        this.html = "";
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
