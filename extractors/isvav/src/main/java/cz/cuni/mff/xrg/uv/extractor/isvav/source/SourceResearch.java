package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Vyzkumne zamery".
 * 
 * @author Škoda Petr
 */
public class SourceResearch extends AbstractSource {
	
	/**
	 * currentYear=2014&stavFazeKod= -> rok je k tomu abychom vedeli k jakemu roku se ma vztahova stav (pokud specifikovan)
	 */
	private static final String URL_FILTER = "http://www.isvav.cz/findResearchPlanByFilter.do?typVyhledavani=easy&prjIntCode=&prjIntName=&providerCode=&updateForm=&branchCode=&typOboru=1&keyword=&currentYear=2014&stavFazeKod=&stavovyFiltrRok=2014&rolePrijemce=1&nazevPrijemce=&vyzOrg=0&vyzOrgRok=2014&roleResitele=3&personSurname=&sortType=0&formType=0";
	
	/**
	 * Views:
	 *	0 - výpis údajů o účastnících výzkumných záměrů po jednotlivých letech
	 *	1 - výpis finančních údajů výzkumných záměrů po jednotlivých letech
	 */
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=researchPlan&exportType=xls&views=1&views=2&x=15&y=4";
	
	public SourceResearch() {
		super(URL_FILTER, URL_DOWNLOAD, "Research");
	}	

}
