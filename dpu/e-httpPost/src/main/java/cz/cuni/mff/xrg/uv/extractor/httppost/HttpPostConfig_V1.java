package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration class for HttpPost.
 *
 * @author Petr Škoda
 */
public class HttpPostConfig_V1 {

    public static class Argument {
        
        private String name;
                
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
    }

    private String endpoint = "http://127.0.0.1";

    private List<Argument> arguments = new LinkedList<>();

    public HttpPostConfig_V1() {

    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

}
