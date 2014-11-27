package cz.cuni.mff.xrg.uv.transformer;

import java.util.LinkedList;
import java.util.List;

/**
 * DPU's configuration class.
 */
public class HtmlCssConfig_V1 {

    public enum ElementType {
        /**
         * Extract content as text.
         */
        TEXT,
        /**
         * Extract content as html.
         */
        HTML,
        /**
         * Extract table as text.
         */
        TABLE_TEXT,
        /**
         * Extract table as text, if there is direct link in cell, then its href attribute is extracted.
         */
        TABLE_LINKS,
        /**
         * Extract table as html.
         */
        TABLE_HTML
    }

    public static class Query {

        /**
         * Query in jsoup.
         */
        private String query = "";

        /**
         * Predicate used for data.
         */
        private String predicate = "";

        /**
         * Name of attribute to select, if set then value of given attribute is used for further processing.
         */
        private String attrName = null;

        /**
         * Type of extraction.
         */
        private ElementType type = ElementType.TEXT;

        public Query() {
        }

        public Query(String query, String predicate, String attrName, ElementType type) {
            this.query = query;
            this.predicate = predicate;
            this.attrName = attrName;
            this.type = type;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getPredicate() {
            return predicate;
        }

        public void setPredicate(String predicate) {
            this.predicate = predicate;
        }

        public String getAttrName() {
            return attrName;
        }

        public void setAttrName(String attrName) {
            this.attrName = attrName;
        }

        public ElementType getType() {
            return type;
        }

        public void setType(ElementType type) {
            this.type = type;
        }

    }

    private List<Query> queries = new LinkedList<>();

    public HtmlCssConfig_V1() {

    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

}
