package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 * Support multiple targets for download.
 * 
 * @author Škoda Petr
 */
@EntityDescription.Entity(type = "http://unifiedviews.eu/ontology/dpu/httpPost/Configuration")
public class HttpPostConfig_V2 {

    @EntityDescription.Entity(type = "http://unifiedviews.eu/ontology/dpu/httpPost/Argument")
    public static class Argument {

        @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/key")
        private String name;

        @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/value")
        private String value;

        public Argument() {
        }

        public Argument(String name, String value) {
            this.name = name;
            this.value = value;
        }

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

    @EntityDescription.Entity(type = "http://unifiedviews.eu/ontology/dpu/httpPost/Request")
    public static class Request {

        @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/argument")
        public List<Argument> arguments = new LinkedList<>();

        @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/uri")
        private String uri;

        @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/fileName")
        private String fileName;

        public Request() {
        }

        public List<Argument> getArguments() {
            return arguments;
        }

        public void setArguments(List<Argument> arguments) {
            this.arguments = arguments;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

    }

    @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/httpPost/requests")
    private List<Request> request = new LinkedList<>();

    public HttpPostConfig_V2() {
    }

    public List<Request> getRequest() {
        return request;
    }

    public void setRequest(List<Request> request) {
        this.request = request;
    }

}
