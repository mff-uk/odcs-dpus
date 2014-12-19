package cz.cuni.mff.xrg.uv.loader.graphstoreprotocol;

/**
 * DPU's configuration class.
 */
public class GraphStoreProtocolConfig_V1 {

    public static enum RepositoryType {
        Virtuoso,
        Fuseki
    }

    private String endpointSelect = "http://localhost:8890/sparql";
    
    private String endpointUpdate = "http://localhost:8890/sparql";
    
    private String endpointCRUD = "http://localhost:8890/sparql-graph-crud";

    private String targetGraphURI = "http://localhost/resource/upload";

    private RepositoryType repositoryType = RepositoryType.Fuseki;

    private boolean useAuthentification = true;

    private String userName = "";

    private String password = "";

    public GraphStoreProtocolConfig_V1() {

    }

    public String getEndpointSelect() {
        return endpointSelect;
    }

    public void setEndpointSelect(String endpointSelect) {
        this.endpointSelect = endpointSelect;
    }

    public String getEndpointUpdate() {
        return endpointUpdate;
    }

    public void setEndpointUpdate(String endpointUpdate) {
        this.endpointUpdate = endpointUpdate;
    }

    public String getEndpointCRUD() {
        return endpointCRUD;
    }

    public void setEndpointCRUD(String endpointCRUD) {
        this.endpointCRUD = endpointCRUD;
    }

    public String getTargetGraphURI() {
        return targetGraphURI;
    }

    public void setTargetGraphURI(String targetGraphURI) {
        this.targetGraphURI = targetGraphURI;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    public boolean isUseAuthentification() {
        return useAuthentification;
    }

    public void setUseAuthentification(boolean useAuthentification) {
        this.useAuthentification = useAuthentification;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
