package cz.cuni.mff.xrg.uv.extractor.isvav;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class Configuration extends DPUConfigObjectBase {

	private SourceType sourceType = SourceType.Funder;

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

}