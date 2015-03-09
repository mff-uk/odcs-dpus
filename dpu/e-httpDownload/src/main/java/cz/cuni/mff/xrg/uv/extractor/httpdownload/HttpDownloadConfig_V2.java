package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 * DPU's configuration class.
 */
@EntityDescription.Entity(type = FilesDownloadVocabulary.STR_CONFIG_CLASS)
public class HttpDownloadConfig_V2 {

    @EntityDescription.Property(uri = FilesDownloadVocabulary.STR_CONFIG_HAS_FILE)
    private List<DownloadInfo_V1> toDownload = new LinkedList<>();

    public HttpDownloadConfig_V2() {

    }

    public List<DownloadInfo_V1> getToDownload() {
        return toDownload;
    }

    public void setToDownload(List<DownloadInfo_V1> toDownload) {
        this.toDownload = toDownload;
    }
    
}
