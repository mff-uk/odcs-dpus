package cz.cuni.mff.xrg.uv.transformer.xslt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;

/**
 *
 * @author Škoda Petr
 */
public class XSLTConfig_V1_Convertor implements ConfigTransformer, AutoInitializer.Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(XSLTConfig_V1_Convertor.class);

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {
        // No-op here.
    }

    @Override
    public String transformString(String configName, String config) throws ConfigException {
        LOG.info("source:\n{}", config);
        if (!configName.equals(MasterConfigObject.CONFIG_NAME)) {
            return config;
        }
        // Master config object, check for SPARQLConfig_V1 configuration.
        if (!config.contains("XSLTConfig_V1")) {
            return config;
        }
        // Get SPARQL query from old configuration.
        String originalConfiguration = config.substring(
                config.indexOf("<Configuration>") + "<Configuration>".length(),
                config.indexOf("</Configuration>"));
        originalConfiguration = originalConfiguration.
                replaceAll("&", "&amp;").
                replaceAll("<", "&lt;").
                replaceAll(">", "&gt;");

        // Create new configuration.
        final StringBuilder newConfiguration = new StringBuilder();
        newConfiguration.append(""
                + "<object-stream>\n"
                + "  <MasterConfigObject>\n"
                + "    <configurations>\n"
                + "      <entry>\n"
                + "        <string>dpu_config</string>\n"
                + "        <string>&lt;object-stream&gt;"
                + "&lt;eu.unifiedviews.plugins.transformer.xslt.XSLTConfig__V1&gt;");
        newConfiguration.append(originalConfiguration);       
        newConfiguration.append(""
                + "&lt;/eu.unifiedviews.plugins.transformer.xslt.XSLTConfig__V1&gt;"
                + "&lt;/object-stream&gt;</string>\n"
                + "      </entry>\n"
                + "    </configurations>\n"
                + "  </MasterConfigObject>\n"
                + "</object-stream>");

        LOG.info("\n{}\n", newConfiguration.toString());

        return newConfiguration.toString();
    }

    @Override
    public <TYPE> void transformObject(String configName, TYPE config) throws ConfigException {
        // No-op here.
    }

    @Override
    public void preInit(String param) {
        // No-op here.
    }

    @Override
    public void afterInit(Context context) {
        // No-op here.
    }

}
