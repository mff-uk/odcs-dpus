package cz.cuni.mff.xrg.uv.boost.migration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;

/**
 * Provide DPU with possibility of opening configuration from "core" DPUs.
 *
 * @author Škoda Petr
 */
public class ConfigurationUpdate implements ConfigTransformer, AutoInitializer.Initializable {

    private static final String CLASS_GROUP = "class";

    private static final String CONFIGURATION_GROUP = "config";

    /**
     * Contains pattern used to match old configurations.
     */
    private final Pattern pattern = Pattern.compile("^<object-stream>\\s*<ConfigurationVersion>.*"
            + "<className>(?<" + CLASS_GROUP + ">[^<]+)</className>\\s*</ConfigurationVersion>\\s*"
            + "<Configuration>(?<" + CONFIGURATION_GROUP + ">.+)</Configuration>\\s*</object-stream>$"
            , Pattern.DOTALL);

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {
        // No-op here.
    }

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUpdate.class);

    @Override
    public String transformString(String configName, String config) throws ConfigException {
        if (!configName.equals(MasterConfigObject.CONFIG_NAME)) {
            return config;
        }
        // Fast initial check.
        if (!config.contains("<ConfigurationVersion>")) {
            return config;
        }

        final Matcher matcher = pattern.matcher(config);
        if (!matcher.matches()) {
            return config;
        }
        final String className = matcher.group(CLASS_GROUP).replaceAll("_", "__");
        String originalConfiguration = matcher.group(CONFIGURATION_GROUP);
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
                + "&lt;");
        newConfiguration.append(className);
        newConfiguration.append("&gt;");

        newConfiguration.append(originalConfiguration);
        
        newConfiguration.append("&lt;/");
        newConfiguration.append(className);
        newConfiguration.append("&gt;"
                + "&lt;/object-stream&gt;</string>\n"
                + "      </entry>\n"
                + "    </configurations>\n"
                + "  </MasterConfigObject>\n"
                + "</object-stream>");

        LOG.info("new configuration:\n{}", newConfiguration.toString());

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
