package eu.unifiedviews.plugins.transformer.xslt;

import cz.cuni.mff.xrg.uv.boost.dpu.config.VersionedConfig;
import cz.cuni.mff.xrg.uv.transformer.xslt.XsltConfig_V2;

/**
 * Original configuration from XSLT.
 *
 * @author Škoda Petr
 */
public class XSLTConfig_V1 implements VersionedConfig<XsltConfig_V2> {

    private String xslTemplate = "";

    private String xslTemplateFileNameShownInDialog = "";

    private boolean skipOnError = false;

    private String xsltParametersMapName = "xsltParameters";

    private String outputFileExtension = "";

    public XSLTConfig_V1() {
    }

    public String getXslTemplate() {
        return xslTemplate;
    }

    public void setXslTemplate(String xslTemplate) {
        this.xslTemplate = xslTemplate;
    }

    public String getXslTemplateFileNameShownInDialog() {
        return xslTemplateFileNameShownInDialog;
    }

    public void setXslTemplateFileNameShownInDialog(
            String xslTemplateFileNameShownInDialog) {
        this.xslTemplateFileNameShownInDialog = xslTemplateFileNameShownInDialog;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public String getXsltParametersMapName() {
        return xsltParametersMapName;
    }

    public void setXsltParametersMapName(String xsltParametersMapName) {
        this.xsltParametersMapName = xsltParametersMapName;
    }

    public String getOutputFileExtension() {
        return outputFileExtension;
    }

    public void setOutputFileExtension(String outputFileExtension) {
        this.outputFileExtension = outputFileExtension;
    }

    @Override
    public XsltConfig_V2 toNextVersion() {
        final XsltConfig_V2 config = new XsltConfig_V2();

        config.setFailOnError(!this.skipOnError);
        config.setXsltTemplate(this.xslTemplate);
        config.setXsltTemplateName(this.xslTemplateFileNameShownInDialog);

        return config;
    }

}
