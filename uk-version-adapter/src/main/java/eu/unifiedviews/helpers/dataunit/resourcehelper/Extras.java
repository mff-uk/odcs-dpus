package eu.unifiedviews.helpers.dataunit.resourcehelper;

import java.util.LinkedHashMap;
import java.util.Map;

public class Extras {
    private String source;

    private Map<String, String> map = new LinkedHashMap<>();

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}