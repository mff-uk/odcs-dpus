package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Projekty".
 * 
 * @author Škoda Petr
 */
public class SourceProject extends AbstractSource {

	/**
	 * currentYear=2014&stavFazeKod= -> rok je k tomu abychom vedeli k jakemu roku se ma vztahova stav (pokud specifikovan)
	 */
	private static final String URL_FILTER = "http://www.isvav.cz/findProjectByFilter.do?typVyhledavani=easy&prjIntCode=&prjIntName=&providerCode=&updateForm=&activityType=&activityCode=&branchCode=&typOboru=1&keyword=&currentYear=2014&stavFazeKod=&stavovyFiltrRok=2000&rolePrijemce=2&nazevPrijemce=&vyzOrg=0&vyzOrgRok=2014&roleResitele=3&personSurname=&sortType=0&formType=0";

	/**
	 * Views:
	 *	1 - výpis údajů o účastnících projektů po jednotlivých letech
	 *	2 - výpis finančních údajů projektů po jednotlivých letech
	 *	3 - výpis finančních údajů projektů po jednotlivých letech a účastnících
	 */
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=project&views=1&views=2&views=3&exportType=";
	
	public SourceProject(String exportType) {
		super(URL_FILTER, URL_DOWNLOAD + exportType, "Project");
	}	

}
