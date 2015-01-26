package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.ConfigurableAddon;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Provide possibility to wrap user code. Wrapped code may be re-executed in case of failure and so this
 * add-on can be used a form of fault tolerant layer.
 *
 * @author Škoda Petr
 */
public class FaultToleranceWrap implements ConfigurableAddon<FaultToleranceWrap.Configuration_V1> {

    public static final String USED_CONFIG_NAME = "addon/faultToleranceWrap";

    public static final String ADDON_NAME = "Fault tolerance";

    private static final Logger LOG = LoggerFactory.getLogger(FaultToleranceWrap.class);

    /**
     * Interface to wrap general user code.
     */
    public interface Action {

        /**
         * Put your code inside this method. Remember that this method can be executed more then once!
         *
         * @param connection
         */
        void action() throws Exception;

    }

    /**
     * Interface to wrap general user code that should return some value.
     */
    public interface ActionReturn<TYPE> {

        /**
         * Put your code inside this method. Remember that this method can be executed more then once!
         *
         * @param connection
         * @return
         */
        TYPE action() throws Exception;

    }

    /**
     * Interface to wrap user code that uses {@link RepositoryConnection}.
     */
    public interface ConnectionAction {

        /**
         * Put your code inside this method. Remember that this method can be executed more then once!
         *
         * @param connection
         */
        void action(RepositoryConnection connection) throws Exception;

    }

    public static class Configuration_V1 {

        /**
         * If false no fault tolerance is provided by this add-on.
         */
        private boolean enabled = true;

        /**
         * Store names of exceptions that should be catch.
         */
        private List<String> exceptionNames = new LinkedList<>();

        /**
         * Number of retry before failure, use -1 as no limit.
         */
        private int maxRetryCount = -1;

        public Configuration_V1() {
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getExceptionNames() {
            return exceptionNames;
        }

        public void setExceptionNames(List<String> exceptionNames) {
            this.exceptionNames = exceptionNames;
        }

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

    }

    public class VaadinDialog extends AddonVaadinDialogBase<Configuration_V1> {

        private CheckBox checkEnabled;
        
        private TextField txtRetryCount;

        private TextArea txtExceptionNames;

        public VaadinDialog() {
            super(Configuration_V1.class);
        }

        @Override
        public void buildLayout() {
            final VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(true);
            layout.setMargin(true);

            checkEnabled = new CheckBox("Enable fault tolerace wrap");
            layout.addComponent(checkEnabled);
            checkEnabled.addValueChangeListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    txtRetryCount.setEnabled((Boolean)event.getProperty().getValue());
                    txtExceptionNames.setEnabled((Boolean)event.getProperty().getValue());
                }
            });

            txtRetryCount = new TextField("Number of retry attemps");
            txtRetryCount.setDescription("Use -1 for infinite amount of attemps.");
            txtRetryCount.setWidth("100%");
            layout.addComponent(txtRetryCount);

            txtExceptionNames = new TextArea("Names of eceptions to catch (separated by ; )");
            txtExceptionNames.setSizeFull();
            layout.addComponent(txtExceptionNames);
            layout.setExpandRatio(txtExceptionNames, 1.0f);
            
            final Panel panel = new Panel();
            panel.setSizeFull();
            panel.setContent(layout);

            setCompositionRoot(panel);
        }

        @Override
        protected String getConfigClassName() {
            return USED_CONFIG_NAME;
        }

        @Override
        protected void setConfiguration(Configuration_V1 conf) throws DPUConfigException {
            checkEnabled.setValue(conf.isEnabled());
            txtRetryCount.setValue(Integer.toString(conf.maxRetryCount));
            // Construct exception string.
            final StringBuilder exceptionList = new StringBuilder();
            for (String item : conf.getExceptionNames()) {
                exceptionList.append(item);
                exceptionList.append(";");
            }
            txtExceptionNames.setValue(exceptionList.toString());
        }

        @Override
        protected Configuration_V1 getConfiguration() throws DPUConfigException {
            final Configuration_V1 c = new Configuration_V1();

            c.setEnabled(checkEnabled.getValue());
            try {
                if (c.isEnabled()) {
                    // Read only if enabled ..
                    c.setMaxRetryCount(Integer.parseInt(txtRetryCount.getValue()));
                }
            } catch (NumberFormatException ex) {
                throw new DPUConfigException(txtRetryCount.getCaption() + " must be a number!");
            }
            c.getExceptionNames().addAll(Arrays.asList(txtExceptionNames.getValue().split(";")));

            return c;
        }

    }

    private Configuration_V1 config;

    private DPUContext dpuContext;

    @Override
    public void init(AdvancedVaadinDialogBase.Context context) {
        // No work on dpu initialization.
    }

    @Override
    public Class<Configuration_V1> getConfigClass() {
        return Configuration_V1.class;
    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AddonVaadinDialogBase<Configuration_V1> getDialog() {
        return new VaadinDialog();
    }

    @Override
    public void init(DpuAdvancedBase.Context context) throws AddonException {
        this.dpuContext = context.getDpuContext();
        // Load configuration.
        try {
            this.config = context.getConfigManager().get(USED_CONFIG_NAME, Configuration_V1.class);
        } catch (ConfigException ex) {
            ContextUtils.sendWarn(dpuContext, "Addon failed to load configuration", ex,
                    "Failed to load configuration for: %s default configuration is used.", ADDON_NAME);
            this.config = new Configuration_V1();
        }
        if (this.config == null) {
            ContextUtils.sendWarn(dpuContext, "Addon failed to load configuration",
                    "Failed to load configuration for: %s default configuration is used.", ADDON_NAME);
            this.config = new Configuration_V1();
        }
    }

    /**
     *
     * @param failCounter
     * @return new value for failCounter.
     * @throws DPUException If we run out of attempts.
     */
    private int checkFailState(Exception ex, int failCounter) throws DPUException {
        if (!shouldBeCatched(ex) || failCounter > config.maxRetryCount) {
            throw new DPUException("Operation failed.", ex);
        }
        LOG.warn("Use operation failed {}/{}.", failCounter, config.maxRetryCount, ex);
        return failCounter + (config.maxRetryCount == -1 ? 0 : 1);        
    }

    public void execute(Action codeToExecute) throws DPUException {
        int failCounter = -1;
        while (!dpuContext.canceled()) {
            // Try to execute user code.
            try {
                codeToExecute.action();
                return;
            } catch (Exception ex) {
                failCounter = checkFailState(ex, failCounter);
            }
        }
        // If we get here we were interupter before we finished the operation.
        throw new DPUException("Interrupted before user operation could be completed.");
    }

    public <TYPE> TYPE execute(ActionReturn<TYPE> codeToExecute) throws DPUException {
        int failCounter = -1;
        while (!dpuContext.canceled()) {
            // Try to execute user code.
            try {
                return codeToExecute.action();
            } catch (Exception ex) {
                failCounter = checkFailState(ex, failCounter);
            }
        }
        // If we get here we were interupter before we finished the operation.
        throw new DPUException("Interrupted before user operation could be completed.");
    }

    public <T extends MetadataDataUnit> void execute(T dataUnit, ConnectionAction codeToExecute)
            throws DPUException {
        int failCounter = -1;
        while (!dpuContext.canceled()) {
            // Get connection.
            RepositoryConnection connection;
            try {
                connection = dataUnit.getConnection();
            } catch (DataUnitException ex) {
                // Test for max retry count.
                if (!shouldBeCatched(ex) || failCounter > config.maxRetryCount) {
                    throw new DPUException("Operation failed.", ex);
                } else {
                    LOG.warn("Can't get connection {}/{}.", failCounter, config.maxRetryCount, ex);
                    failCounter += (config.maxRetryCount == -1 ? 0 : 1);
                    continue;
                }
            }
            // Try to execute user code.
            try {
                codeToExecute.action(connection);
                return;
            } catch (Exception ex) {
                failCounter = checkFailState(ex, failCounter);
            } finally {
                // In every case close the connection.
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }            
        }
        // If we get here we were interupter before we finished the operation.
        throw new DPUException("Interrupted before user operation could be completed.");
    }

    /**
     *
     * @param ex
     * @return True if this add-on is configured to catch this type of exception.
     */
    private boolean shouldBeCatched(Exception ex) {
        // Convert Exception class name into string.
        final String exceptionClassName = ex.getClass().getSimpleName();
        // Scan if we have same on the white-list.
        for (String item : config.exceptionNames) {
            LOG.trace("shouldBeCatched '{}' ? '{}'", exceptionClassName, item);
            if (item.compareToIgnoreCase(exceptionClassName) == 0) {
                return true;
            }
        }
        return false;
    }

}
