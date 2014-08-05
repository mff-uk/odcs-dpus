package cz.cuni.mff.xrg.uv.boost.dpu.simple;

import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXml;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
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
public abstract class DpuSimpleBase<CONFIG> implements DPU, DPUConfigurable,
        ConfigDialogProvider<CONFIG> {

    /**
     * DPU's configuration.
     */
    protected CONFIG config = null;

    /**
     * Execution context.
     */
    protected DPUContext context;

    /**
     * Service for xml serialization.
     */
    private final SerializationXml<CONFIG> serializationService;

    public DpuSimpleBase(Class<CONFIG> configClazz) {
        this.serializationService = SerializationXmlFactory.serializationXml(
                configClazz);
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
        boolean executeMain = false;
        try {
            init();
            executeMain = true;
        } catch (DPUException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU init. failed",
                    "init() method failed for DPUException", ex);
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU init. failed",
                    "init() method failed for DataUnitException", ex);
        }
        // execute
        try {
            if (executeMain) {
                innerExecute();
            } else {
                context.sendMessage(DPUContext.MessageType.INFO,
                        "Main execution method skipped.");
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU Failed for DataUnit Exception", "", ex);
        }
        close();
    }

    /**
     * Called before {@link #innerExecute()}. If throws then
     * {@link #innerExecute()} is not called.
     *
     * @throws DPUException
     * @throws DataUnitException
     */
    protected void init() throws DPUException, DataUnitException {
        
    }

    /**
     * DPU user core.
     * 
     * @throws DPUException
     * @throws DataUnitException
     */
    protected abstract void innerExecute() throws DPUException, DataUnitException;

    /**
     * Is executed in any case.
     */
    protected void close() {

    }
    
}
