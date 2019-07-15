package ssrmonitor.git;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class SSConfig {
    @Getter
    @Setter
    private String ip;
    private int port;
    @Setter
    private String pwd;
    @Setter
    private String method;

    public String getPort() {
        return String.valueOf(port);
    }

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

}
