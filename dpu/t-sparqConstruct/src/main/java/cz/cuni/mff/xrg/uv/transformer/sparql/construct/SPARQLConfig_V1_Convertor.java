package cz.cuni.mff.xrg.uv.transformer.sparql.construct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class SPARQLConfig_V1_Convertor implements ConfigTransformer, AutoInitializer.Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(SPARQLConfig_V1_Convertor.class);

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {
        // No-op here.
    }

    @Override
    public String transformString(String configName, String config) throws ConfigException {
        if (!configName.equals(MasterConfigObject.CONFIG_NAME)) {
            return config;
        }
        // Master config object, check for SPARQLConfig_V1 configuration.
        if (!config.contains("SPARQLConfig_V1")) {
            return config;
        }
        // Get SPARQL query from old configuration.
        final String query = config.substring(
                config.indexOf("<SPARQLQuery>") + "<SPARQLQuery>".length(),
                config.indexOf("</SPARQLQuery>"));
        LOG.info("Extracted query: {}", query);
        // Create new configuration.
        final StringBuilder newConfiguration = new StringBuilder();
        newConfiguration.append(""
                + "<object-stream>\n"
                + "  <MasterConfigObject>\n"
                + "    <configurations>\n"
                + "      <entry>\n"
                + "        <string>dpu_config</string>\n"
                + "        <string>&lt;object-stream&gt;\n"
                + "  &lt;cz.cuni.mff.xrg.uv.transformer.sparql.construct.SparqlConstructConfig__V1&gt;\n"
                + "    &lt;query&gt;");
        // Insert query, just escape any &
        final String newQuery = query.replaceAll("&", "&amp;");
        newConfiguration.append(newQuery);
        
        newConfiguration.append("&lt;/query&gt;\n"
                + "    &lt;perGraph&gt;false&lt;/perGraph&gt;\n"
                + "  &lt;/cz.cuni.mff.xrg.uv.transformer.sparql.construct.SparqlConstructConfig__V1&gt;\n"
                + "&lt;/object-stream&gt;</string>\n"
                + "      </entry>\n"
                + "    </configurations>\n"
                + "  </MasterConfigObject>\n"
                + "</object-stream>");
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
