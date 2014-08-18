package cz.cuni.mff.xrg.uv.extractor.isvav;

public class IsvavConfig_V1 {

    public static final String EXPORT_TYPE_DBF = "dbf";

    public static final String EXPORT_TYPE_XLS = "xls";

	private SourceType sourceType = SourceType.Funder;

    private String exportType = EXPORT_TYPE_DBF;

    public IsvavConfig_V1() {
    }

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

}