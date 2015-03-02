package cz.cuni.mff.xrg.uv.eextractor.listdownloader;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration class for ListDownloader.
 *
 * @author Petr Škoda
 */
public class ListDownloaderConfig_V1 {

    public static class NextPageCondition {

        /**
         * CSS selector used to match "Next page" button.
         */
        private String nextButtonSelector = "a.listingNext";

        public NextPageCondition() {
        }

        public String getNextButtonSelector() {
            return nextButtonSelector;
        }

        public void setNextButtonSelector(String nextButtonSelector) {
            this.nextButtonSelector = nextButtonSelector;
        }
        
    }

    private String pagePattern = "http://localhost/list?page=%d";

    /**
     * Start index.
     */
    private int startIndex = 0;

    /**
     * List of conditions that must hold in order to go to next page.
     */
    private List<NextPageCondition> nextPageConditions = new LinkedList<>();
    
    public ListDownloaderConfig_V1() {

    }

    public String getPagePattern() {
        return pagePattern;
    }

    public void setPagePattern(String pagePattern) {
        this.pagePattern = pagePattern;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public List<NextPageCondition> getNextPageConditions() {
        return nextPageConditions;
    }

    public void setNextPageConditions(List<NextPageCondition> nextPageConditions) {
        this.nextPageConditions = nextPageConditions;
    }

}
