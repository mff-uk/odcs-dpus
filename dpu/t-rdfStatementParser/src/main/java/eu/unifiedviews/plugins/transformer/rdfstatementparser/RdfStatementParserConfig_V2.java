package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public class RdfStatementParserConfig_V2 {

    public static class ActionInfo {

        /**
         * Group name.
         */
        private String name;
        
        /**
         * Type of action with given group;
         */
        private ActionType_V1 actionType;

        /**
         * Based on {@link #actionType} contains regular expression  to use or
         * predicate to for triple.
         * If regular expression is used then named groups should be used.
         */
        private String actionData;

        public ActionInfo() {
        }

        /**
         * For conversion.
         * 
         * @param name
         * @param info
         */
        public ActionInfo(String name, RdfStatementParserConfig_V1.ActionInfo info) {
            this.name = name;
            switch(info.getActionType()) {
                case CreateTriple:
                    this.actionType = ActionType_V1.CreateTriple;
                    break;
                case RegExp:
                    this.actionType = ActionType_V1.RegExp;
                    break;
            }
            this.actionData = info.getActionData();            
        }
        
        public ActionInfo(String name, ActionType_V1 actionType, String actionData) {
            this.name = name;
            this.actionType = actionType;
            this.actionData = actionData;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public ActionType_V1 getActionType() {
            return actionType;
        }

        public void setActionType(ActionType_V1 actionType) {
            this.actionType = actionType;
        }

        public String getActionData() {
            return actionData;
        }

        public void setActionData(String actionData) {
            this.actionData = actionData;
        }

    }

    /**
     * Map of actions. Group name and respective action.
     */
    private List<ActionInfo> actions = new LinkedList<>();

    /**
     * Query used to get values.
     */
    private String selectQuery = "SELECT * WHERE {?subject ?p ?o}";

    /**
     * If true then also labels/tags are transfered with values.
     */
    private boolean transferLabels = false;

    public RdfStatementParserConfig_V2() {
    }

    public List<ActionInfo> getActions() {
        return actions;
    }

    public void setActions(List<ActionInfo> actions) {
        this.actions = actions;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public boolean isTransferLabels() {
        return transferLabels;
    }

    public void setTransferLabels(boolean transferLabels) {
        this.transferLabels = transferLabels;
    }

}
