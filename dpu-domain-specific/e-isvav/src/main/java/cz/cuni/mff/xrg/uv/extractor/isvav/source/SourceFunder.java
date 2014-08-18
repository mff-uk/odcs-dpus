package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Poskytovatele podpory";
 * 
 * @author Škoda Petr
 */
public class SourceFunder extends AbstractSource {

	private static final String URL_FILTER = "http://www.isvav.cz/findFunderByFilter.do?sortField=reskod&sortType=0";
	
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=funder&exportType=";
	
	public SourceFunder(String exportType) {
		super(URL_FILTER, URL_DOWNLOAD + exportType, "Funder");
	}
	
}
