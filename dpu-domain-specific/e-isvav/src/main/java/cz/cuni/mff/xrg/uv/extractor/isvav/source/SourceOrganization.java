package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Subjekty ve VaVaI".
 * 
 * @author Škoda Petr
 */
public class SourceOrganization extends AbstractSource {

	/**
	 * vyzOrgPoskyt=any -> všechny subjekty evidované v IS VaVaI
	 */
	private static final String URL_FILTER = "http://www.isvav.cz/findOrganizationByFilter.do?orgName=&kodSubjektu=&kategorieSubjektu=&orgICO=&nazevOrgJednotky=&kodOrgJednotky=&kodZeme=&vyzOrgRok=2008&vyzOrgPoskyt=any&vyzOrg=0&bezPodrizenych=0&sortField=parnidk&sortType=0";
	
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=organization&exportType=";
	
	public SourceOrganization(String exportType) {
		super(URL_FILTER, URL_DOWNLOAD + exportType, "Organization");
	}

}
