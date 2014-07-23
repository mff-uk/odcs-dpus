package cz.cuni.mff.xrg.uv.boost.dpu.simple;

import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXml;
import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXmlFailure;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class ConfigurableBase<CONFIG> implements DPU, DPUConfigurable,
        ConfigDialogProvider<CONFIG> {

    /**
     * DPU's configuration.
     */
    protected CONFIG config = null;

    protected DPUContext context;
    
    private final SerializationXml<CONFIG> serializationService;

    public ConfigurableBase(Class<CONFIG> configClazz) {
        this.serializationService = SerializationXmlFactory.serializationXml(
                configClazz, "dpuConfig");
    }

    @Override
    public void configure(String configStr) throws DPUConfigException {
        try {
            config = serializationService.convert(configStr);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Deserialization failed.", ex);
        }
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            CONFIG instance = serializationService.createInstance();
            return serializationService.convert(instance);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException(ex);
        }
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        this.context = context;
        try {
            execute();
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        }
    }

    public abstract void execute() throws DPUException, DataUnitException;
        
    
}
