package ssrmonitor.ssh;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SSHUtils {
	@Value("${ssr.ip}")
	private String ip;
	@Value("${ssr.ssh.port}")
	private int port;
	@Value("${ssr.ssh.user}")
	private String userName;
	@Value("${ssr.ssh.pwd}")
	private String password;

	public void changeSSRPort(int newPort) throws IOException {
		try (SshClient client = SshClient.setUpDefaultClient()) {
			client.start();
			try (ClientSession session = client.connect(userName, ip, port).verify(1L, TimeUnit.MINUTES).getSession()) {
				session.addPasswordIdentity(password);
				session.auth().verify(30L, TimeUnit.SECONDS);
				String executeRemoteCommand = session.executeRemoteCommand("pkill -f ssserver");
				System.out.println(executeRemoteCommand);
				String ss = session.executeRemoteCommand(String.format(
						"/bin/python /usr/bin/ssserver -c /data/ssrconfig/%d.json -d start >/dev/null 2>&1", newPort));
				System.out.println(ss);
				session.close(false);
			} finally {
				client.stop();
			}
		}
	}

}
