package cz.cuni.mff.xrg.odcs.loader.ftp;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {
	
	private String host = "ftp://";
	
	private int port = 20;
	
	private String user = null;
	
	private String password = null;
	
	private boolean SFTP = false;

	private String protocol = "TLS";
	
	private boolean implicit = false;
	
	private String targetPath = "/tmp";
	
	public String getHost() {
		return host;
	}

	public void setHost(String url) {
		this.host = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSFTP() {
		return SFTP;
	}

	public void setSFTP(boolean sftp) {
		this.SFTP = sftp;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}	

	public boolean isImplicit() {
		return implicit;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}	
	
	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
}
