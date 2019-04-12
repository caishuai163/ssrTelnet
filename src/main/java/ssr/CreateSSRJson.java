package ssr;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

public class CreateSSRJson {
	private static final String template = "{\r\n" + "    \"server\":\"0.0.0.0\",\r\n" + "    \"server_port\":%d,\r\n"
			+ "    \"local_address\":\"127.0.0.1\",\r\n" + "    \"local_port\":1080,\r\n"
			+ "    \"password\":\"xiaoma666\",\r\n" + "    \"timeout\":300,\r\n" + "    \"method\":\"aes-256-cfb\",\r\n"
			+ "    \"fast_open\":false\r\n" + "}\r\n" + "";

	public static void main(String[] args) throws IOException {
		for (int i = 10000; i < 60001; i++) {
			FileUtils.writeStringToFile(new File("C:\\Users\\cs\\Desktop\\ShadowsocksR\\serverConf\\" + i + ".json"),
					String.format(template, i), StandardCharsets.UTF_8);
		}

	}
}
