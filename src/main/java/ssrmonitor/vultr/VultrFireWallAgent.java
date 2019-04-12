package ssrmonitor.vultr;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * operate Vultr FireWall
 * 
 * @author cs
 */
public class VultrFireWallAgent {
	private static final String API_KEY = "AJRGMOP5DJY4SVFMP3LPDW6B2XQNQ6M4FSQA";
	private static final String FIREWALL_GROUPID = "62c999b3";
	private static final Logger logger = LoggerFactory.getLogger(VultrFireWallAgent.class);

	/**
	 * get FireWall rule list
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static List<FireWallRule> getFireWallRule() throws IOException {
		try {
			String asString = Request
					.Get("https://api.vultr.com/v1/firewall/rule_list?FIREWALLGROUPID=" + FIREWALL_GROUPID
							+ "&direction=in&ip_type=v4")
					.addHeader("API-Key", API_KEY).execute().returnContent().asString();
			logger.info("get FireWall rule list return :{}", asString);
			Map<String, FireWallRule> readValue = new ObjectMapper().readValue(asString,
					new TypeReference<Map<String, FireWallRule>>() {
					});
			return readValue.entrySet().stream().map(Entry::getValue).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("get FireWall rule list error:", e);
			throw e;
		}
	}

	/**
	 * delete rule by rule number
	 * 
	 * @param ruleNumber
	 */
	public static void deleteFireWallRule(int ruleNumber) {
		logger.info("delete FireWall Rule rulenumber :{}", ruleNumber);
		Form delete = Form.form().add("FIREWALLGROUPID", FIREWALL_GROUPID).add("rulenumber",
				String.valueOf(ruleNumber));
		try {
			int deleteStatus = Request.Post("https://api.vultr.com/v1/firewall/rule_delete")
					.addHeader("API-Key", API_KEY).bodyForm(delete.build()).execute().returnResponse().getStatusLine()
					.getStatusCode();
			logger.info("delete FireWall Rule return :{}", deleteStatus);
		} catch (IOException e) {
			logger.error("delete FireWall Rule error:", e);
		}
	}

	/**
	 * delete rule by rule number
	 * 
	 * @param ruleNumber
	 * @throws IOException
	 */
	public static void createTcpAndUdpFireWallRule(int port) throws IOException {
		logger.info("create Tcp and Udp firewall rule. port :{}", port);
		Form form = Form.form().add("FIREWALLGROUPID", FIREWALL_GROUPID).add("direction", "in").add("ip_type", "v4")
				.add("subnet", "0.0.0.0").add("subnet_size", "0").add("port", String.valueOf(port)).add("notes", "ssr");
		try {
			HttpResponse tcpResponse = Request.Post("https://api.vultr.com/v1/firewall/rule_create")
					.addHeader("API-Key", API_KEY).bodyForm(form.add("protocol", "tcp").build()).execute()
					.returnResponse();
			int tcpCode = tcpResponse.getStatusLine().getStatusCode();
			String tcpData = EntityUtils.toString(tcpResponse.getEntity());
			logger.info("create Tcp  firewall rule. return code :{},data:{}", tcpCode, tcpData);
			if (tcpCode != 200) {
				throw new ClientProtocolException(tcpData);
			}
			HttpResponse udpResponse = Request.Post("https://api.vultr.com/v1/firewall/rule_create")
					.addHeader("API-Key", API_KEY).bodyForm(form.add("protocol", "udp").build()).execute()
					.returnResponse();
			int udpCode = udpResponse.getStatusLine().getStatusCode();
			String udpData = EntityUtils.toString(udpResponse.getEntity());
			logger.info("create UDP firewall rule. return code :{},data:{}", udpCode, udpData);
			if (udpCode != 200) {
				throw new ClientProtocolException(udpData);
			}
		} catch (IOException e) {
			logger.error("create Tcp and Udp firewall rule error:", e);
			throw e;
		}
	}
}
