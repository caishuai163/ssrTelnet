package ssrmonitor.git;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class SSConfig {
	private String ip;
	private int port;
	private String pwd;
	private String method;

	public String getPort() {
		return String.valueOf(port);
	}

	@JsonIgnore
	public int getPortInt() {
		return port;
	}

	public String getPwd() {
		return pwd == null ? "xiaoma666" : pwd;
	}

	public String getMethod() {
		return method == null ? "aes-256-cfb" : method;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPort(String port) {
		if (StringUtils.isNumeric(port)) {
			this.port = Integer.valueOf(port);
		}
		this.port = 0;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public SSConfig() {
		super();
	}

	public SSConfig(String ip, int port, String pwd, String method) {
		super();
		this.ip = ip;
		this.port = port;
		this.pwd = pwd;
		this.method = method;
	}

}
