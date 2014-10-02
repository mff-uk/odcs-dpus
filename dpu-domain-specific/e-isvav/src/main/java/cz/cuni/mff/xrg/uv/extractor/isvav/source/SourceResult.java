package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 * Source for "Vysledky".
 * 
 * @author Škoda Petr
 */
public class SourceResult extends AbstractSource {
	
	/**
	 * Parameters are: resultYearFrom=%s&resultYearTo=%s
	 */
	private static final String URL_FILTER = "http://www.isvav.cz/findResultByFilter.do?typVyhledavani=easy&resultLanguage=&resultDataSuplier=&resultCode=&updateForm=&submitterName=&vyzOrg=0&vyzOrgRok=2014&authorSurname=&resultName=&resultExerciseCode=U&resultYearFrom=%s&resultYearTo=%s&resultBranch=&resultAnnotation=&resultIsbn=&formType=0";
	
	private static final String URL_DOWNLOAD = "http://www.isvav.cz/export.zip;jsessionid=%s?entityType=result&exportType=";
	
	public SourceResult(String exportType, String year) {
		super(String.format(URL_FILTER, year, year), 
                URL_DOWNLOAD + exportType, "Result-" + year);
	}
	
}
