package cz.cuni.mff.xrg.uv.transformer;

import java.util.LinkedList;
import java.util.List;

/**
 * DPU's configuration class.
 *
 * @author Škoda Petr
 */
public class HtmlCssConfig_V1 {

    public enum ActionType {
        /**
         * Execute jsoup query from action data.
         */
        QUERY,
        /**
         * Extract content as value string.
         */
        TEXT,
        /**
         * Extract content as html string.
         */
        HTML,
        /**
         * Select attribute of given name. Attribute's name is stored in actin data.
         */
        ATTRIBUTE,
        /**
         * Create predicate with given value as an object. Predicate is stored in under action data.
         */
        OUTPUT,
        /**
         * Create subject used by {@link #OUTPUT} in subtree. Action data contains subject class, that
         * is created to identify subject.
         * If action data == null, then can be used to create a common subject for a sub tree.
         */
        SUBJECT,
        /**
         * Given list of elements put each element into separated group.
         */
        UNLIST
    }

    public static class Action {

        /**
         * Name of action. This value is used to match named output on which this query is executed.
         */
        private String name = HtmlCss.SUBJECT_URI_TEMPLATE;

        /**
         * Determine type of an action.
         */
        private ActionType type = ActionType.TEXT;

        /**
         * Data for action, based on {@link #type}.
         */
        private String actionData = "";

        /**
         * Name out output, if any.
         */
        private String outputName = "";

        public Action() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ActionType getType() {
            return type;
        }

        public void setType(ActionType type) {
            this.type = type;
        }

        public String getActionData() {
            return actionData;
        }

        public void setActionData(String actionData) {
            this.actionData = actionData;
        }

        public String getOutputName() {
            return outputName;
        }

        public void setOutputName(String outputName) {
            this.outputName = outputName;
        }
        
    }

    private List<Action> actions = new LinkedList<>();

    private String classAsStr = "http://unifiedviews.eu/ontology/e-htmlCss/Page";
    
    private String hasPredicateAsStr = "http://unifiedviews.eu/ontology/e-htmlCss/hasObject";

    public HtmlCssConfig_V1() {

    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getClassAsStr() {
        return classAsStr;
    }

    public void setClassAsStr(String classAsStr) {
        this.classAsStr = classAsStr;
    }

    public String getHasPredicateAsStr() {
        return hasPredicateAsStr;
    }

    public void setHasPredicateAsStr(String hasPredicateAsStr) {
        this.hasPredicateAsStr = hasPredicateAsStr;
    }
    
}
