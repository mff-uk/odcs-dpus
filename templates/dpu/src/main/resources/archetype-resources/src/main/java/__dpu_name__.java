#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.As${dpu_type}
public class ${dpu_name} extends DpuAdvancedBase<${dpu_name}Config_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(${dpu_name}.class);
		
	public ${dpu_name}() {
		super(${dpu_name}Config_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        LOG.info("DPU is running ...");
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new ${dpu_name}VaadinDialog();
    }
	
}
