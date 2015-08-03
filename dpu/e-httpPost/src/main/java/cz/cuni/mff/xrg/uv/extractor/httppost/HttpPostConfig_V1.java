package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.VersionedConfig;

/**
 * Configuration class for HttpPost.
 *
 * @author Petr Škoda
 */
public class HttpPostConfig_V1 implements VersionedConfig<HttpPostConfig_V2>{

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

    @Override
    public HttpPostConfig_V2 toNextVersion() throws DPUConfigException {
        final HttpPostConfig_V2 c = new HttpPostConfig_V2();
        final HttpPostConfig_V2.Request request = new HttpPostConfig_V2.Request();
        
        for (Argument argument : arguments) {
            request.getArguments().add(new HttpPostConfig_V2.Argument(argument.name, argument.value));
        }
        
        request.setFileName("content-file");
        request.setUri(this.endpoint);
        
        c.getRequest().add(request);
        return c;
    }

}
