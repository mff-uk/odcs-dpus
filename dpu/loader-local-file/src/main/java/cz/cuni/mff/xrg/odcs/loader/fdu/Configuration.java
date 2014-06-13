package cz.cuni.mff.xrg.odcs.loader.fdu;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {
		
	private String destination = null;

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
}
