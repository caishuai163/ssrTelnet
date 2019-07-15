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
import ssrmonitor.git.GitTools;
import ssrmonitor.vultr.ChangePortService;

@Service
public class TestSsrPort {
    private static final Logger logger = LoggerFactory
            .getLogger(QuartzApplication.class);
    @Autowired
    private ChangePortService changePortService;
    @Autowired
    private EmailTools emailTools;
    @Autowired
    private GitTools gitTools;
    @Value("${ssr.ip}")
    private String ip;
    @Value("${ssr.port.start}")
    private Integer port;
    @Value("${ssr.ssh.port}")
    private int sshPort;

    public void aaa() {
        port++;
        logger.info("ip:{},port:{}", ip, port);
        emailTools.sendSysError("机器无法ping通",
            new RuntimeException(ip + "已经无法ping通，请及时更换服务器以及更改程序配置"));

    }

    public void run() {

        if (isPingSuccess()) {
            boolean isFirst = true;
            while (port <= 60000) {
                if (!PTUtil.isTelnetSuccess(ip, sshPort)) {
                    emailTools.sendSysError("SSH端口无法使用",
                        new RuntimeException("SSH端口无法使用"));
                    return;
                }
                if (PTUtil.isTelnetSuccess(ip, port)) {
                    if (!isFirst) {
                        // if telnet success and not first telnet。 it means port
                        // changed。
                        gitTools.syncConfigInGit(ip, port);
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
            emailTools.sendSysError("端口号被用尽",
                new RuntimeException("端口号已经循环过60000"));
            return;
        }
        emailTools.sendSysError("机器无法ping通",
            new RuntimeException(ip + "已经无法ping通，请及时更换服务器以及更改程序配置"));

    }

    public boolean isPingSuccess() {
        for (int i = 0; i < 10; i++) {
            logger.info("第{}次开始ping {}", i + 1, ip);
            JsonResult pingResult = PTUtil.pingResult(ip, 1000);
            logger.info("第{}次ping {} 结束，结果：{}", i + 1, ip, pingResult);
            if (pingResult.getCode()
                    .equals(Constants.ResultCode.SUCCESS.val())) {
                return true;
            }
        }
        return false;
    }

   
}
