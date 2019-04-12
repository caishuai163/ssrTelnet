package ssrmonitor.vultr;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ssrmonitor.email.EmailTools;
import ssrmonitor.ssh.SSHUtils;

@Service
public class ChangePortService {
	private static final Logger logger = LoggerFactory.getLogger(ChangePortService.class);
	@Value("${ssr.changePort.waitMinutes}")
	private int waitMinutes;
	@Autowired
	private SSHUtils sshUtils;
	@Autowired
	private EmailTools emailTools;

	public void changePort(int port) {
		try {
			List<FireWallRule> fireWallRuleList = VultrFireWallAgent.getFireWallRule();
			fireWallRuleList.forEach(e -> {
				if (StringUtils.isNumeric(e.getPort()) && Integer.valueOf(e.getPort()) >= 10000
						&& Integer.valueOf(e.getPort()) <= 60000) {
					VultrFireWallAgent.deleteFireWallRule(e.getRulenumber());
				}
			});
			VultrFireWallAgent.createTcpAndUdpFireWallRule(port);
		} catch (IOException e) {
			String msg = "��������ǽʱ����,�˴β�����ֹ.";
			logger.error(msg + "=>", e);
			emailTools.sendSysError(msg, e);
			return;
		}
		try {
			sshUtils.changeSSRPort(port);
		} catch (IOException e) {
			String msg = "���Զ�̷�����SSR�˿�ʱ����,�˴β�����ֹ.";
			logger.error(msg + "=>", e);
			emailTools.sendSysError(msg, e);
			return;
		}
		logger.info("��ʼ�ȴ��˿ڱ����Ч��ʱ��{}min", waitMinutes);
		try {
			Thread.sleep(waitMinutes * 60000);
		} catch (InterruptedException e) {
			logger.error("�ȴ��˿ڱ����Ч�쳣->", e);
		}
		logger.info("�ȴ��˿ڱ����Ч����");
	}
}
