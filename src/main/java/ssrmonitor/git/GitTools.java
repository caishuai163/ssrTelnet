package ssrmonitor.git;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.tools.PTUtil;

@Component
public class GitTools {
	private static final Logger logger = LoggerFactory.getLogger(GitTools.class);

	@Value("${git.local:C:\\Users\\Administrator\\Desktop\\tips}")
	private String gitPath;

	public String getGitPath() {
		return gitPath;
	}

	public void syncConfigInGit(String ip, int port) {
		Callable<Boolean> call = () -> {
			boolean isContinue = false;
			File config = new File(gitPath + File.separatorChar + "ss" + File.separatorChar + "conf.json");
			List<SSConfig> configs = new ArrayList<>();
			try {
				if (config.exists() && config.isFile()) {
					byte[] byteArray = FileUtils.readFileToByteArray(config);
					configs = new ObjectMapper().readValue(byteArray, new TypeReference<List<SSConfig>>() {
					});
					logger.info("原始文件信息{}", configs);
					for (int i = configs.size() - 1; i >= 0; i--) {
						boolean telnetSuccess = PTUtil.isTelnetSuccess(configs.get(i).getIp(),
								configs.get(i).getPortInt());
						if (!telnetSuccess) {
							configs.remove(i);
							logger.info("删除了配置{}", i);
							isContinue = true;
						}
					}
				}
			} catch (Exception e) {
				logger.warn("git test old file error", e);
			}
			boolean anyMatch = configs.stream().anyMatch(p -> ip.equals(p.getIp()) && port == p.getPortInt());
			if (!anyMatch) {
				isContinue = true;
			}
			if (isContinue) {
				SSConfig build = SSConfig.builder().ip(ip).port(port).build();
				configs.add(build);
				String asString = new ObjectMapper().writeValueAsString(configs);
				logger.info("文件信息{}", asString);
				FileUtils.writeStringToFile(config, asString, StandardCharsets.UTF_8);
			}
			return isContinue;
		};
		gitOperate(call);
	}

	public void gitOperate(Callable<Boolean> call) {
		logger.info("git operate start");
		try (Git git = Git.open(new File(gitPath))) {
			logger.info("git pull");
			git.pull().call();
			Boolean res = call.call();
			if (res == null || !res) {
				return;
			}
			List<DiffEntry> diff = git.diff().call();
			if (diff.isEmpty()) {
				logger.info("git no diff");
				return;
			}
			logger.info("git add,  commit and push");
			git.add().addFilepattern(".").call();
			git.commit().setMessage("service auto change").call();
			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("caishuai_cs@163.com",
					"caishuai123");
			git.push().setForce(true).setCredentialsProvider(credentialsProvider).call();
		} catch (Exception e) {
			logger.warn("git operate error", e);
		}
		logger.info("git operate finished");
	}
}
