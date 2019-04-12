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
		emailTools.sendSysError("机器无法ping通", new RuntimeException(ip + "已经无法ping通，请及时更换服务器以及更改程序配置"));

	}

	public void run() {

		if (isPingSuccess()) {
			boolean isFirst = true;
			while (port <= 60000) {
				if (isTelnetSuccess()) {
					if (!isFirst) {
						// if telnet success and not first telnet。 it means port changed。
						emailTools.sendChangePortMsg(ip, port);
					}
					return;
				}
				isFirst = false;
				port++;
				changePortService.changePort(port);
				logger.info("变更端口结束，准备测试端口:{}", port);
			}
			port = 10000;
			emailTools.sendSysError("端口号被用尽", new RuntimeException("端口号已经循环过60000"));
			return;
		}
		emailTools.sendSysError("机器无法ping通", new RuntimeException(ip + "已经无法ping通，请及时更换服务器以及更改程序配置"));

	}

	public boolean isPingSuccess() {
		for (int i = 0; i < 10; i++) {
			logger.info("第{}次开始ping {}", i + 1, ip);
			JsonResult pingResult = PTUtil.pingResult(ip, 1000);
			logger.info("第{}次ping {} 结束，结果：{}", i + 1, ip, pingResult);
			if (pingResult.getCode().equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}

	public boolean isTelnetSuccess() {
		for (int i = 0; i < 10; i++) {
			logger.info("开始telnet {}:{}", ip, port);
			JsonResult telnetResult = PTUtil.telnetResult(ip, port, 5000);
			logger.info("telnet {}:{} 结束。结果：{}", ip, port, telnetResult);
			if (telnetResult.getCode().equals(Constants.ResultCode.SUCCESS.val())) {
				return true;
			}
		}
		return false;
	}
}
