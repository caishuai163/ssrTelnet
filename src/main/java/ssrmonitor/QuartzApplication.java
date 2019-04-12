package ssrmonitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@ComponentScan
@AutoConfigurationPackage
public class QuartzApplication {
	@Autowired
	private TestSsrPort testSsrPort;

	public static void main(String[] args) {
		SpringApplication.run(QuartzApplication.class, args);
	}

	@Scheduled(cron = "${ssr.quartz}")
	public void work() {
		testSsrPort.run();
	}

}
