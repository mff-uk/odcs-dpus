package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Programy VaVaI".
 * 
 * @author Škoda Petr
 */
public class SourceProgramme extends AbstractSource {
	
	/**
	 * Parameters are: programRokZahajeniOd=%s&programRokZahajeniDo=%s
	 */
	private static final String URL_FILTER = "http://www.isvav.cz/findProgrammeByFilter.do?kodProgramu=&typProgramu=&providerCode=&nazevProgramu=&cileProgramu=&casovyFiltrRok=&programRokZahajeniOd=%s&programRokZahajeniDo=%s&programRokUkonceniOd=&programRokUkonceniDo=&sortField=aktkod&sortType=0";
	
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=programme&exportType=";
	
	/**
	 * Download for every year 1991-2019
     * @param exportType
	 */
	public SourceProgramme(String exportType) {
		super(String.format(URL_FILTER, "1991", "2019"), 
                URL_DOWNLOAD + exportType, "Programme-1991-2019");
	}
	
	public SourceProgramme(String exportType, String from, String to) {
		super(String.format(URL_FILTER, from, to), 
                URL_DOWNLOAD + exportType, "Programme-" + from + "-" + to);
	}	

}
