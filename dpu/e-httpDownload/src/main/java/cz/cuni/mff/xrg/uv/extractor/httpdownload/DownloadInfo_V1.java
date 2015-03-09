package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 *
 * @author Škoda Petr
 */
@EntityDescription.Entity(type = FilesDownloadVocabulary.STR_FILE_CLASS)
public class DownloadInfo_V1 {

    /**
     * Path from which download the file.
     */
    @EntityDescription.Property(uri = FilesDownloadVocabulary.STR_FILE_URI)
    private String uri;

    /**
     * Virtual path.
     */
    @EntityDescription.Property(uri = FilesDownloadVocabulary.STR_FILE_NAME)
    private String virtualPath;

    public DownloadInfo_V1() {
    }

    public DownloadInfo_V1(String uri) {
        this.uri = uri;
        this.virtualPath = null;
    }

    public DownloadInfo_V1(String uri, String virtualPath) {
        this.uri = uri;
        this.virtualPath = virtualPath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVirtualPath() {
        return virtualPath;
    }

    public void setVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
    }

}
