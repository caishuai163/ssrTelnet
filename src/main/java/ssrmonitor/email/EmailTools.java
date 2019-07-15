package ssrmonitor.email;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

@Component
public class EmailTools {
	private static final Logger logger = LoggerFactory.getLogger(EmailTools.class);

	@Autowired
	private EmailSenderLoader emailSenderLoader;

	public void emailBySelf(String subject, String msg, String... emailTo) {
		try {
			Email email = new HtmlEmail();
			email.setHostName("mail.wangeqiu.top");
			// email.setSSLOnConnect(true);
			email.setFrom("admin@mail.wangeqiu.top");
			email.setAuthentication("admin", "Caishuai123");
			email.setSubject(subject);
			email.setMsg(msg);
			email.addTo(emailTo);
			email.send();
		} catch (Exception e) {
			logger.error("send email failed. msg:", e);
		}
	}

	public void email(String subject, String msg, String... emailTo) {
		EmailSender sender = emailSenderLoader.getOneEmailSender();
		try {
			Email email = new HtmlEmail();
			email.setHostName(sender.getHostName());
			email.setSSLOnConnect(sender.isSslOnConnect());
			email.setFrom(sender.getFrom());
			email.setAuthentication(sender.getUserName(), sender.getPassword());
			email.setSubject(subject);
			email.setMsg(msg);
			email.addTo(emailTo);
			email.send();
		} catch (Exception e) {
			logger.error("send email failed. msg:", e);
			sendSysError("发送邮件异常", e);
		}
	}

	public void sendChangePortMsg(String ip, int port) {
		String replace = StringUtils.replace(emailSenderLoader.getChangePortMsgTemplate(), "{serverIp}", ip);
		replace = StringUtils.replace(replace, "{serverPort}", String.valueOf(port));
		replace = StringUtils.replace(replace, "{datetime}",
				DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.S"));
		email("云服务器开放端口发生变化", replace, emailSenderLoader.getEmailto());
		logger.info("发送端口变化通知结束");
	}

	public void sendSysError(String msg, Throwable e) {
		String replace = StringUtils.replace(emailSenderLoader.getSysyemErrorTemplate(), "{errorMsg}", encode(msg));
		replace = StringUtils.replace(replace, "{errorInfo}", encode(Throwables.getStackTraceAsString(e)));
		replace = StringUtils.replace(replace, "{datetime}",
				DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.S"));
		emailBySelf("云服务器监控异常", replace, "461588977@qq.com");
	}

	private String encode(String args) {
		char[] charArray = args.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < charArray.length; i++) {
			builder.append("&#");
			builder.append(Integer.toString(charArray[i], 10));
			builder.append(";");
		}
		return builder.toString();
	}

}
