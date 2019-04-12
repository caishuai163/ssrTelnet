package ssrmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.tools.Constants;
import net.tools.JsonResult;
import net.tools.PTUtil;
import ssrmonitor.email.EmailTools;
import ssrmonitor.vultr.ChangePortService;

@Service
public class TestSsrPort {
	private static final Logger logger = LoggerFactory.getLogger(QuartzApplication.class);
	@Autowired
	private ChangePortService changePortService;
	@Autowired
	private EmailTools emailTools;
	@Value("${ssr.ip}")
	private String ip;
	@Value("${ssr.port.start}")
	private Integer port;

	public void aaa() {
		port++;
		logger.info("ip:{},port:{}", ip, port);
		emailTools.sendSysError("�����޷�pingͨ", new RuntimeException(ip + "�Ѿ��޷�pingͨ���뼰ʱ�����������Լ����ĳ�������"));

	}

	public void run() {

		if (isPingSuccess()) {
			boolean isFirst = true;
			while (port <= 60000) {
				if (isTelnetSuccess()) {
					if (!isFirst) {
						// if telnet success and not first telnet�� it means port changed��
						emailTools.sendChangePortMsg(ip, port);
					}
					return;
				}
				isFirst = false;
				port++;
				changePortService.changePort(port);
				logger.info("����˿ڽ�����׼�����Զ˿�:{}", port);
			}
			port = 10000;
			emailTools.sendSysError("�˿ںű��þ�", new RuntimeException("�˿ں��Ѿ�ѭ����60000"));
			return;
		}
		emailTools.sendSysError("�����޷�pingͨ", new RuntimeException(ip + "�Ѿ��޷�pingͨ���뼰ʱ�����������Լ����ĳ�������"));

	}

	public boolean isPingSuccess() {
		for (int i = 0; i < 10; i++) {
			logger.info("��{}�ο�ʼping {}", i + 1, ip);
			JsonResult pingResult = PTUtil.pingResult(ip, 1000);
			logger.info("��{}��ping {} �����������{}", i + 1, ip, pingResult);
			if (pingResult.getCode().equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}

	public boolean isTelnetSuccess() {
		for (int i = 0; i < 10; i++) {
			logger.info("��ʼtelnet {}:{}", ip, port);
			JsonResult telnetResult = PTUtil.telnetResult(ip, port, 5000);
			logger.info("telnet {}:{} �����������{}", ip, port, telnetResult);
			if (telnetResult.getCode().equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}
}
