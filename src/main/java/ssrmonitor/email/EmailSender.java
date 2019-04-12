package ssrmonitor.email;

import lombok.Data;

@Data
public class EmailSender {
	private String hostName;
	private boolean sslOnConnect;
	private String from;
	private String userName;
	private String password;
}
