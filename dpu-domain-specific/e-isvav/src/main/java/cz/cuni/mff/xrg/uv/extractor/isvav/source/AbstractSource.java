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
	 * Unique file name for given source.
	 */
	private final String fileName;
	
	protected AbstractSource(String filterUri, String downloadUri, String fileName) {
		this.filterUri = filterUri;
		this.downloadUri = downloadUri;
		this.fileName = fileName;
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

	public String getFileName() {
		return fileName;
	}
	
}
