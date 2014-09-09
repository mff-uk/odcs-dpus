package cz.cuni.mff.xrg.uv.transformer;

import java.util.LinkedList;
import java.util.List;

/**
 * DPU's configuration class.
 */
public class HtmlCssConfig_V1 {

    public enum ElementType {
        TEXT,
        TABLE
    }

    public static class NamedQuery {

        /**
         * Query in jsoup.
         */
        private String query = "";

        /**
         * Predicate used for data.
         */
        private String predicate = "";

        /**
         * Name of attribute to select, if null then text representation is
         * selected.
         */
        private String attrName = null;

        
        private ElementType type = ElementType.TEXT;

        public NamedQuery() {
        }

        public NamedQuery(String query, String predicate, String attrName, ElementType type) {
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

    private List<NamedQuery> queries = new LinkedList<>();

    public HtmlCssConfig_V1() {

    }

    public List<NamedQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<NamedQuery> queries) {
        this.queries = queries;
    }

}
