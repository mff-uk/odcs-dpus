package cz.cuni.mff.xrg.odcs.loader.scp;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 * DPU's configuration class.
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {

	private String hostname = "";

	private Integer port = 22;
	
	private String username = "";

	private String password = "";

	private String destination = "/";

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

	public String getDestination() {
		return destination;
	}

	public void setDestination(String upDestination) {
		this.destination = upDestination;
	}
	
}
