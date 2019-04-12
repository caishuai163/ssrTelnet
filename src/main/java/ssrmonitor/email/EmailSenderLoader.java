package ssrmonitor.email;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Repository
public class EmailSenderLoader {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Value("${ssr.email.emailSenderJsonConfig}")
	private String emailSenderJsonConfig;
	@Value("${ssr.email.sysyemErrorTemplate}")
	@Getter
	private String sysyemErrorTemplate;
	@Value("${ssr.email.changePortMsgTemplate}")
	@Getter
	private String changePortMsgTemplate;
	@Value("${ssr.email.emailto}")
	private String emailto;

	private List<EmailSender> emailSenderList;

	public List<EmailSender> getEmailSenderList() {
		if (emailSenderList == null) {
			try {
				emailSenderList = mapper.readValue(emailSenderJsonConfig, new TypeReference<List<EmailSender>>() {
				});
			} catch (IOException e) {
				return Collections.emptyList();
			}
		}
		return emailSenderList;
	}

	public EmailSender getOneEmailSender() {
		List<EmailSender> senderList = getEmailSenderList();
		int nextInt = RandomUtils.nextInt(0, senderList.size());
		return senderList.get(nextInt);
	}

	public String[] getEmailto() {
		return emailto.split(",");
	}

}
