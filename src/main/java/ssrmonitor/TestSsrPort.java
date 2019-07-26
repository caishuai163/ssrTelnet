package ssrmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.tools.PTUtil;
import ssrmonitor.email.EmailTools;
import ssrmonitor.git.GitTools;
import ssrmonitor.vultr.ChangePortService;

@Service
public class TestSsrPort {
    private static final Logger logger = LoggerFactory
            .getLogger(TestSsrPort.class);
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
    @Value("${ssr.email.isopen:true}")
    private boolean emailIsOpen;

    public void aaa() {
        port++;
        logger.info("ip:{},port:{}", ip, port);
        emailTools.sendSysError("机器无法ping通",
            new RuntimeException(ip + "已经无法ping通，请及时更换服务器以及更改程序配置"));

    }

    public void run() {

        if (PTUtil.isPingSuccess(ip)) {
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
                        if (emailIsOpen) {
                        	emailTools.sendChangePortMsg(ip, port);
						}
                        
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
}
