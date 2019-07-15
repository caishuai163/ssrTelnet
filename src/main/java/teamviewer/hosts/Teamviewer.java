package teamviewer.hosts;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import ssrmonitor.git.GitTools;

@Component
public class Teamviewer {
	private static final Logger logger = LoggerFactory.getLogger(Teamviewer.class);
	private static final int timeOut = 3000;

	@Autowired
	private GitTools gitTools;

	private String getFastestHostsList() {
		String fastHost = "# teamviewer\n";
		fastHost += getFastHosts("www.teamviewer.com");
		fastHost += getFastHosts("static.teamviewer.com");
		for (int i = 1; i <= 16; i++) {
			String domian = String.format("router%d.teamviewer.com", i);
			fastHost += getFastHosts(domian);
		}
		logger.info("get fastest hosts finish");
		logger.info(fastHost);
		return fastHost;
	}

	public void timeTask() {
		String fastHost = getFastestHostsList();
		gitTools.gitOperate(() -> {
			FileUtils.writeStringToFile(new File(gitTools.getGitPath() + File.separatorChar + "hosts"), fastHost,
					StandardCharsets.UTF_8);
			return true;
		});
	}

	private static String getFastHosts(String domian) {
		logger.info("get fastest hosts:" + domian);
		Lookup lookup;
		try {
			lookup = new Lookup(domian, Type.A);
		} catch (TextParseException e) {
			return "";
		}
		lookup.run();
		if (lookup.getResult() == Lookup.SUCCESSFUL) {
			List<String> ips = Arrays.stream(lookup.getAnswers()).map(Record::rdataToString)
					.filter(ip -> ip.replaceAll("\\d", "").length() == 3).collect(Collectors.toList());
			String fastestIp = "";
			long minTTL = 99999L;
			for (String ip : ips) {
				long ttl = ttl(ip);
				if (ttl >= 0 && ttl < minTTL) {
					minTTL = ttl;
					fastestIp = ip;
				}
			}
			if (StringUtils.isNotBlank(fastestIp)) {
				return fastestIp + " " + domian + "\n";
			}
		}
		return "";
	}

	private static long ttl(String ip) {
		long before = System.currentTimeMillis();
		for (int i = 0; i < 4; i++) {
			try {
				InetAddress.getByName(ip).isReachable(timeOut);
			} catch (IOException e) {
				try {
					Thread.sleep(timeOut);
				} catch (InterruptedException e1) {
					logger.error("get fastest hosts error:", e);
				}
			}
		}
		return System.currentTimeMillis() - before;
	}
}
