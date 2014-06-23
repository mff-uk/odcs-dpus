#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.As${dpu_type};
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@As${dpu_type}
public class Main extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
		
	public Main() {
		super(Configuration.class);
	}
	
	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}
	
	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException {
		
		// Put execution code here
		
	}
	
}
