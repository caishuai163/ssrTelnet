package ssrmonitor.git;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
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
    private static final Logger logger = LoggerFactory
            .getLogger(GitTools.class);

    @Value("${git.local:C:\\Users\\Administrator\\Desktop\\tips}")
    private String gitPath;

    public void syncConfigInGit(String ip, int port) {
        ObjectMapper mapper = new ObjectMapper();
        try (Git git = Git.open(new File(gitPath))) {
            git.pull().call();

            File config = new File(gitPath + File.separatorChar + "ss"
                    + File.separatorChar + "conf.json");
            List<SSConfig> configs = new ArrayList<>();
            try {
                if (config.exists() && config.isFile()) {
                    byte[] byteArray = FileUtils.readFileToByteArray(config);
                    configs = mapper.readValue(byteArray,
                        new TypeReference<List<SSConfig>>() {});
                    for (int i = configs.size() - 1; i >= 0; i--) {
                        boolean telnetSuccess = PTUtil.isTelnetSuccess(
                            configs.get(i).getIp(),
                            configs.get(i).getPortInt());
                        if (!telnetSuccess) {
                            configs.remove(i);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("git test old file error", e);
            }
            boolean anyMatch = configs.stream().anyMatch(
                p -> ip.equals(p.getIp()) && port == p.getPortInt());
            if (anyMatch) {
                return;
            }
            SSConfig build = SSConfig.builder().ip(ip).port(port).build();
            configs.add(build);
            String asString = mapper.writeValueAsString(configs);
            FileUtils.writeStringToFile(config, asString,
                StandardCharsets.UTF_8);
            git.add().addFilepattern(".").call();
            git.commit().setMessage("auto change ss config list").call();
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    "caishuai_cs@163.com", "caishuai123");
            git.push().setForce(false)
                    .setCredentialsProvider(credentialsProvider).call();
        } catch (Exception e) {
            logger.warn("git operate error", e);
        }
    }
}
