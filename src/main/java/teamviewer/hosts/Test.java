package teamviewer.hosts;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class Test {
    private static final Logger logger = Logger.getLogger("Test");
    private static final int timeOut = 3000;
    private static final String GIT_PATH = "C:\\Users\\Administrator\\Desktop\\tips";

    public static void main(String[] args) throws TextParseException {
        while (true) {
            Calendar calendar = Calendar.getInstance();
            @SuppressWarnings("deprecation")
            int minutes = calendar.getTime().getMinutes();
            if (minutes == 0) {
                timeTask();
            } else {
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private static void timeTask() {
        String fastHost = "# teamviewer\n";
        fastHost += getFastHosts("www.teamviewer.com");
        fastHost += getFastHosts("static.teamviewer.com");
        for (int i = 1; i <= 16; i++) {
            String domian = String.format("router%d.teamviewer.com", i);
            fastHost += getFastHosts(domian);
        }
        logger.log(Level.INFO, "get fastest hosts finish");
        logger.log(Level.INFO, fastHost);
        try (Git git = Git.open(new File(GIT_PATH))) {
            FileUtils.writeStringToFile(
                new File(GIT_PATH + File.separatorChar + "hosts"), fastHost,
                StandardCharsets.UTF_8);
            git.add().addFilepattern(".").call();
            git.commit().setMessage("auto change teamwiewer hosts").call();
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    "caishuai_cs@163.com", "caishuai123");
            git.push().setForce(true)
                    .setCredentialsProvider(credentialsProvider).call();
        } catch (Exception e) {
            logger.log(Level.WARNING, "git operate error", e);
        }
        logger.log(Level.INFO, "git operate finished");
    }

    private static String getFastHosts(String domian) {
        logger.log(Level.INFO, "get fastest hosts:" + domian);
        Lookup lookup;
        try {
            lookup = new Lookup(domian, Type.A);
        } catch (TextParseException e) {
            return "";
        }
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            List<String> ips = Arrays.stream(lookup.getAnswers())
                    .map(Record::rdataToString)
                    .filter(ip -> ip.replaceAll("\\d", "").length() == 3)
                    .collect(Collectors.toList());
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
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return System.currentTimeMillis() - before;
    }
}
