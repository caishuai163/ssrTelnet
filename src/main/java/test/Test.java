package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

public class Test {
	public static void main(String[] args) throws ClientProtocolException, IOException {

		String pre = "C:\\Users\\cs\\Desktop";
		String flieName = args[0];
		List<String> readLines = FileUtils.readLines(new File(pre + File.separatorChar + flieName + ".m3u8"),
				StandardCharsets.UTF_8);
		File file = new File(pre + File.separatorChar + flieName + ".mp4");
		for (String string : readLines) {
			if (!string.startsWith("#")) {
				byte[] bs = FileUtils.readFileToByteArray(new File(pre + string));
				FileUtils.writeByteArrayToFile(file, bs, true);
			}
		}
	}
}
