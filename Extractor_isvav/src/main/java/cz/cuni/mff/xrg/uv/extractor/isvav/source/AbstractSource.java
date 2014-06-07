package cz.cuni.mff.xrg.uv.extractor.isvav.source;

/**
 *
 * @author Škoda Petr
 */
public abstract class AbstractSource {

	/**
	 * Uri used to get cookie with session or to set filters.
	 */
	private final String filterUri;

	/**
	 * Uri used to download data.
	 */
	private final String downloadUri;

	/**
	 * Base name for output file.
	 */
	private final String baseFileName;
	
	protected AbstractSource(String filterUri, String downloadUri, String baseFileName) {
		this.filterUri = filterUri;
		this.downloadUri = downloadUri;
		this.baseFileName = baseFileName;
	}

	public String getFilterUri() {
		return filterUri;
	}

	/**
	 * Substitute sessionId into {@link #downloadUri} and return result.
	 *
	 * @param sessionId
	 * @return
	 */
	public String getDownloadUri(String sessionId) {
		return String.format(downloadUri, sessionId);
	}

	public String getBaseFileName() {
		return baseFileName;
	}
	
}
