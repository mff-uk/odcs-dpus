package cz.cuni.mff.xrg.uv.extractor.scp;

public class FilesFromScpConfig_V1 {

    private String hostname = "";

    private Integer port = 22;

    private String username = "";

    private String password = "";

    private String source = "~/";

    /**
     * If true and upload failed, then only warning is published.
     */
    private boolean softFail = true;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String upDestination) {
        this.source = upDestination;
    }

    public boolean isSoftFail() {
        return softFail;
    }

    public void setSoftFail(boolean softFail) {
        this.softFail = softFail;
    }

}
