package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonWithVaadinDialog;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.*;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialise annotated {@link SimpleRdfWrite} classes. Also at the end call
 * {@link SimpleRdfWrite#flushBuffer()}.
 *
 *
 * @author Škoda Petr
 */
public class SimpleRdfConfigurator<T extends DpuAdvancedBase>
        implements AddonWithVaadinDialog<SimpleRdfConfigurator.Configuration> {

    public static final String USED_CONFIG_NAME
            = "addon/simpleRdfConfigurator";

    public static final String ADDON_NAME
            = "Simple RDF";

    private static final Logger LOG = LoggerFactory.getLogger(
            SimpleRdfConfigurator.class);

    /**
     * Use to annotate {@link SimpleRdfRead} and {@link SimpleRdfWrite} classes
     * to connect them to {@link RDFDataUnit} and {@link WritableRDFDataUnit}.
     *
     * Both the simple RDF class and target dataUnit must be public!!
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Configure {

        String dataUnitFieldName();

    }

    /**
     * Configuration for single {@link SimpleRdfWrite}.
     */
    public static class RdfWriteConfig {

        /**
         * If true then user specify graph URI directly.
         */
        private boolean advancedMode = false;

        /**
         * User provided value - symbolic name or URI based on
         * {@link #advancedMode}. If null then automatic values is generated.
         */
        private String value = null;

        public RdfWriteConfig() {
        }

        public boolean isAdvancedMode() {
            return advancedMode;
        }

        public void setAdvancedMode(boolean advancedMode) {
            this.advancedMode = advancedMode;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /**
     * Add-on's configuration.
     */
    public static class Configuration {

        private Boolean buffered = true;

        private Integer bufferSize = 50000;

        /**
         * Configuration stored under user visible (annotation.name) names of
         * dataUnits.
         */
        private Map<String, RdfWriteConfig> writeSettings = new LinkedHashMap<>();

        public Configuration() {
        }

        public Boolean isBuffered() {
            return buffered;
        }

        public Integer getBufferSize() {
            return bufferSize;
        }

        public Map<String, RdfWriteConfig> getWriteMapping() {
            return writeSettings;
        }

        public void setBuffered(Boolean buffered) {
            this.buffered = buffered;
        }

        public void setBufferSize(Integer bufferSize) {
            this.bufferSize = bufferSize;
        }

        public void setWriteSettings(
                Map<String, RdfWriteConfig> writeSettings) {
            this.writeSettings = writeSettings;
        }

    }

    /**
     * Component for configuration of a single output.
     */
    public static class RdfWriteConfigComponent extends CustomComponent {

        private final CheckBox checkAdvancedMode;

        private final TextField txtValue;

        /**
         *
         * @param layout Layout to insert components into.
         * @param name   Name of DataUnit visible to user.
         */
        public RdfWriteConfigComponent(GridLayout layout, String name) {
            layout.addComponent(new Label(name));

            checkAdvancedMode = new CheckBox();
            layout.addComponent(checkAdvancedMode);
            layout.setComponentAlignment(checkAdvancedMode,
                    Alignment.MIDDLE_CENTER);

            txtValue = new TextField();
            txtValue.setWidth("100%");
            txtValue.setInputPrompt("generate");
            layout.addComponent(txtValue);
        }

        public void setConfig(RdfWriteConfig c) {
            checkAdvancedMode.setValue(c.isAdvancedMode());
        }

        public RdfWriteConfig getConfig() throws DPUConfigException {
            RdfWriteConfig c = new RdfWriteConfig();

            c.setAdvancedMode(checkAdvancedMode.getValue());
            c.setValue(txtValue.getValue());

            if (c.isAdvancedMode()) {
                // perform checks on value
                if (c.getValue() == null || c.getValue().trim().isEmpty()) {
                    throw new DPUConfigException(
                            "URI must be set for advanced mode.");
                }
            }
            return c;
        }

    }

    public class VaadinDialog extends AddonVaadinDialogBase<Configuration> {

        private CheckBox checkBuffered;

        private TextField txtBufferSize;

        private final Map<String, RdfWriteConfigComponent> mapping = new LinkedHashMap<>();

        public VaadinDialog() {
            super(ConfigHistory.createNoHistory(Configuration.class));
        }

        @Override
        public void buildLayout() {
            final VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);
            mainLayout.setSizeFull();

            checkBuffered = new CheckBox("Is buffered:");
            mainLayout.addComponent(checkBuffered);

            txtBufferSize = new TextField("Buffer size:");
            txtBufferSize.setWidth("10em");
            mainLayout.addComponent(txtBufferSize);

            checkBuffered.addValueChangeListener(
                    new Property.ValueChangeListener() {

                        @Override
                        public void valueChange(Property.ValueChangeEvent event) {
                            Boolean value = (Boolean) event.getProperty()
                            .getValue();
                            if (value != null) {
                                txtBufferSize.setEnabled(value);
                            }
                        }
                    });

            // components for mapping
            final GridLayout gridLayout = new GridLayout(3, 1);
            gridLayout.setSpacing(true);
            gridLayout.setSizeFull();

            final Label lblName = new Label(" ");
            lblName.setWidth("13em");
            gridLayout.addComponent(lblName);
            gridLayout.setColumnExpandRatio(0, 0.0f);

            final Label lblAdvMode = new Label(" Advanced mode ");
            lblAdvMode.setWidth("-1px");
            gridLayout.addComponent(lblAdvMode);
            gridLayout.setColumnExpandRatio(1, 0.0f);

            gridLayout.addComponent(new Label(
                    "Symbolic name / URI (in advanced mode)"));
            gridLayout.setColumnExpandRatio(2, 1.0f);

            for (RdfSimpleField item : rdfDataUnits) {
                if (!item.writable) {
                    // skip nonwritable
                    continue;
                }
                final String name = item.getDataUnitFieldName();
                mapping.put(name,
                        new RdfWriteConfigComponent(gridLayout, name));
            }
            mainLayout.addComponent(gridLayout);
            setCompositionRoot(mainLayout);
        }

        @Override
        protected String getConfigClassName() {
            return USED_CONFIG_NAME;
        }

        @Override
        protected void setConfiguration(Configuration c) throws DPUConfigException {
            checkBuffered.setValue(c.isBuffered());
            if (c.isBuffered()) {
                txtBufferSize.setEnabled(true);
                txtBufferSize.setValue(c.getBufferSize().toString());
            } else {
                txtBufferSize.setEnabled(false);
            }

            for (String name : mapping.keySet()) {
                RdfWriteConfig config = c.getWriteMapping().get(name);
                if (config != null) {
                    mapping.get(name).setConfig(config);
                }
            }
        }

        @Override
        protected Configuration getConfiguration() throws DPUConfigException {
            Configuration c = new Configuration();
            c.setBuffered(checkBuffered.getValue());
            if (checkBuffered.getValue()) {
                int bufferSize;
                try {
                    bufferSize = Integer.parseInt(txtBufferSize.getValue());
                } catch (NumberFormatException ex) {
                    throw new DPUConfigException("Buffer size must be number.");
                }
                if (bufferSize < 1) {
                    throw new DPUConfigException(
                            "Buffer size must be greater then 0.");
                }
                c.setBufferSize(bufferSize);
            }
            // store
            for (String name : mapping.keySet()) {
                c.getWriteMapping().put(name,
                        mapping.get(name).getConfig());
            }
            return c;
        }

    }

    /**
     * Contains information about a single filed of type {@link SimpleRdf} or
     * {@link SimpleRdfWrite}.
     */
    public static class RdfSimpleField {

        private final Field field;

        private final Field dataUnitField;

        /**
         * Name of data unit used in mappings.
         */
        private final String dataUnitName;

        /**
         * If true then {@link SimpleRdfWritable} is represented;otherwise,
         * {@link SimpleRdfRead} is represented.
         */
        private final boolean writable;

        public RdfSimpleField(Field field, Field dataUnitField,
                String dataUnitName, boolean writable) {
            this.field = field;
            this.dataUnitField = dataUnitField;
            this.dataUnitName = dataUnitName;
            this.writable = writable;
        }

        public String getDataUnitFieldName() {
            return dataUnitField.getName();
        }

    }

    private final List<RdfSimpleField> rdfDataUnits = new LinkedList<>();

    /**
     * List of created {@link SimpleRdfWrite}.
     */
    private final List<SimpleRdfWrite> rdfToFlush = new LinkedList<>();

    public SimpleRdfConfigurator(Class<T> clazz) {
        // gather data about fields
        for (Field field : clazz.getFields()) {
            if (!field.isAnnotationPresent(Configure.class)) {
                // skip not annotated
                continue;
            }
            final Configure annotation = field.getAnnotation(Configure.class);
            final Field dataunitField;
            try {
                dataunitField = clazz.getField(annotation.dataUnitFieldName());
            } catch (NoSuchFieldException ex) {
                LOG.error("No field for {}.", annotation.dataUnitFieldName());
                continue;
            }
            Class<?> c = dataunitField.getType();
            if (field.getType() == SimpleRdfWrite.class) {
                if (dataunitField.getType() != WritableRDFDataUnit.class) {
                   LOG.error("Filed: {} does not bind to RDFDataUnit",
                           annotation.dataUnitFieldName());
                   continue;
                }
                rdfDataUnits.add(new RdfSimpleField(field, dataunitField,
                        annotation.dataUnitFieldName(), true));
            } else if (field.getType() == SimpleRdfRead.class) {
                if (dataunitField.getType() != RDFDataUnit.class) {
                   LOG.error("Filed: {} does not bind to RDFDataUnit",
                           annotation.dataUnitFieldName());
                   continue;
                }
                rdfDataUnits.add(new RdfSimpleField(field, dataunitField,
                        annotation.dataUnitFieldName(), false));
            } else {
                LOG.error("Anotation on non SimpleRdf field ingnored!");
            }
        }

    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AddonVaadinDialogBase<Configuration> getDialog() {
        return new VaadinDialog();
    }

    @Override
    public boolean preAction(DpuAdvancedBase.Context context) {
        Configuration config;
        try {
            // load configuration
            config = context.getConfigManager().get(USED_CONFIG_NAME,
                    Configuration.class);
        } catch (ConfigException ex) {
            LOG.debug("Addon failed to load configuration, default used.");
//            context.getDpuContext().sendMessage(DPUContext.MessageType.WARNING,
//                    "Addon failed to load configuration",
//                    "Failed to load configuration for: " + ADDON_NAME
//                    + " default configuration is used.", ex);
            config = new Configuration();
        }

        if (config == null) {
            LOG.debug("Addon configuration is null, default used.");
//            context.getDpuContext().sendMessage(DPUContext.MessageType.WARNING,
//                    "Addon configuration is null.",
//                    "Failed to load configuration for: " + ADDON_NAME
//                    + " default configuration is used.");
            config = new Configuration();
        }

        final T dpuInstance = (T) context.getDpu();
        // bind and set
        for (RdfSimpleField dataUnitInfo : rdfDataUnits) {

            if (dataUnitInfo.writable) {
                RdfWriteConfig c = config.writeSettings.get(
                        dataUnitInfo.dataUnitName);
                if (c == null) {
                    LOG.debug("Configuration for '{}' not found, default used.",
                            dataUnitInfo.dataUnitName);
//                    context.getDpuContext().sendMessage(
//                            DPUContext.MessageType.WARNING,
//                            "Configuration for '" + dataUnitInfo.dataUnitName + "' was not found, default is used.");
                    c = new RdfWriteConfig();
                }
                if (!setRdfWrite(context, dataUnitInfo, dpuInstance, c, config)) {
                    return false;
                }
            } else {
                if (!setRdfRead(context, dataUnitInfo, dpuInstance)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void postAction(DpuAdvancedBase.Context context) {
        for (SimpleRdfWrite item : rdfToFlush) {
            try {
                item.flushBuffer();
            } catch (OperationFailedException ex) {
                context.getDpuContext().sendMessage(
                        DPUContext.MessageType.ERROR, "Can't flush buffer.", "",
                        ex);
            }
        }
    }

    private boolean setRdfWrite(DpuAdvancedBase.Context context,
            RdfSimpleField dataUnitInfo, T dpuInstance, RdfWriteConfig c,
            Configuration config) {
        // prepare prefix for generated names
        final String generatedNamePrefix = "generated/"
                + context.getDpu().getClass().getSimpleName() + "/";
        // get data unit
        final WritableRDFDataUnit dataUnit;
        try {
            dataUnit = (WritableRDFDataUnit) dataUnitInfo.dataUnitField.get(
                    dpuInstance);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Illegal operation during binding.", "", ex);
            return false;
        }

        if (dataUnit == null) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "DataUnit '" + dataUnitInfo.dataUnitName + "' is null!");
            return false;
        }

        // create isntance of SimpleRdfWrite
        SimpleRdfWrite simpleRdfWrite;
        try {
            simpleRdfWrite = SimpleRdfFactory.create(dataUnit,
                    context.getDpuContext());
        } catch (OperationFailedException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Can not create SimpleRdfWrite", "", ex);
            return false;
        }
        // set
        if (config.isBuffered()) {
            simpleRdfWrite.setPolicy(AddPolicy.BUFFERED);
            simpleRdfWrite.setBufferSize(config.bufferSize);
        }
        // get symbolic name / graph URI
        String usedName = c.value;
        if (usedName == null || usedName.isEmpty()) {
            // generate
            usedName = generatedNamePrefix
                    + Long.toString((new Date()).getTime());
        }

        // set symbolic name / graph URI
        try {
            simpleRdfWrite.setOutputGraph(usedName);
            LOG.info("'{}' used as a symbolic name for '{}'", usedName,
                    dataUnitInfo.dataUnitName);
        } catch (OperationFailedException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Failed to set graph name.", "", ex);
            return false;
        }

        try {
            // set SimpleRdfWrite back to instance
            dataUnitInfo.field.set(dpuInstance, simpleRdfWrite);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Illegal operation during binding.", "", ex);
            return false;
        }

        // add to storage
        rdfToFlush.add(simpleRdfWrite);

        return true;
    }

    private boolean setRdfRead(DpuAdvancedBase.Context context,
            RdfSimpleField dataUnitInfo, T dpuInstance) {
        // get data unit
        final RDFDataUnit dataUnit;
        try {
            dataUnit = (RDFDataUnit) dataUnitInfo.dataUnitField.get(dpuInstance);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Illegal operation during binding.", "", ex);
            return false;
        }

        SimpleRdfRead simpleRdfRead;
        try {
            simpleRdfRead = SimpleRdfFactory.create(dataUnit,
                    context.getDpuContext());
        } catch (OperationFailedException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Can not create SimpleRdfWrite", "", ex);
            return false;
        }

        try {
            // set SimpleRdfWrite back to instance
            dataUnitInfo.field.set(dpuInstance, simpleRdfRead);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            context.getDpuContext().sendMessage(DPUContext.MessageType.ERROR,
                    "Illegal operation during binding.", "", ex);
            return false;
        }

        return true;
    }

}
