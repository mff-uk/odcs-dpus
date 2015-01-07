package cz.cuni.mff.xrg.uv.extractor.virtuosoextractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Let virtuoso dump a single graph into file.
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class VirtuosoExtractor extends DpuAdvancedBase<VirtuosoExtractorConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(VirtuosoExtractor.class);

    /**
     * %s - name of graph to extract
     * %s - output file
     */
    private static final String SQL_DUMP = "dump_one_graph ('%s', '%s', 1000000000)";

    public VirtuosoExtractor() {
        super(VirtuosoExtractorConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Test for presence of Virtuso drivers.
        try {
            Class.forName("virtuoso.jdbc4.Driver");
        } catch (ClassNotFoundException ex) {
            throw new DPUException("Can't find virtuoso drivers.", ex);
        }
        final String statement = String.format(SQL_DUMP, config.getGraphUri(), config.getOutputPath());
        try {
            executeSqlStatement(statement);
        } catch (SQLException ex) {
            throw new DPUException("Can't execute statement.", ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new VirtuosoExtractorVaadinDialog();
    }

    /**
     * Execute given statement.
     *
     * @param statementAsString
     * @throws SQLException
     */
    private void executeSqlStatement(String statementAsString) throws SQLException {
        LOG.info("Executing statement: {}", statementAsString);
        try (Connection connection = DriverManager.getConnection(config.getServerUrl(), config.getUsername(),
                config.getPassword())) {
            // Execute statement.
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(statementAsString) ) {
                    // We don't need the result.
                }
            }
        }
    }

}
