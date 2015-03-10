package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.unifiedviews.helpers.dpu.config.VersionedConfig;

/**
 * DPU's configuration class.
 */
public class RdfStatementParserConfig_V1 implements VersionedConfig<RdfStatementParserConfig_V2> {

    /**
     * Action type for group.
     */
    public static enum ActionType {
        CreateTriple,
        RegExp;
    }

    public static class ActionInfo {

        /**
         * Type of action with given group;
         */
        private ActionType actionType;

        /**
         * Based on {@link #actionType} contains regular expression  to use or
         * predicate to for triple.
         * If regular expression is used then named groups should be used.
         */
        private String actionData;

        public ActionInfo() {
        }

        public ActionInfo(ActionType actionType, String actionData) {
            this.actionType = actionType;
            this.actionData = actionData;
        }

        public ActionType getActionType() {
            return actionType;
        }

        public void setActionType(ActionType actionType) {
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
    private Map<String, ActionInfo> actions = new LinkedHashMap<>();

    /**
     * Query used to get values.
     */
    private String selectQuery = "SELECT * WHERE {?subject ?p ?o}";

    private boolean transferLabels = false;

    public RdfStatementParserConfig_V1() {
    }

    public Map<String, ActionInfo> getActions() {
        return actions;
    }

    public void setActions(Map<String, ActionInfo> actions) {
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

    @Override
    public RdfStatementParserConfig_V2 toNextVersion() {
        RdfStatementParserConfig_V2 c = new RdfStatementParserConfig_V2();

        c.setSelectQuery(selectQuery);
        c.setTransferLabels(transferLabels);

        for (String key : actions.keySet()) {
            c.getActions().add(new RdfStatementParserConfig_V2.ActionInfo(key, actions.get(key)));
        }

        return c;
    }

}
