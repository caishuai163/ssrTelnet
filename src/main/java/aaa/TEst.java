package aaa;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TEst {
	private static int index = 0;
	private static final Logger logger = LoggerFactory.getLogger(TEst.class);

	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < 10000; i++) {
			new Thread(() -> {
				while (true) {
					if (index > Integer.valueOf(100000)) {
						logger.error("spend:{}",
								Calendar.getInstance().getTime().getTime() - calendar.getTime().getTime());
						break;
					}
					try {
						long currentTimeMillis = System.currentTimeMillis();
						GyyxResponse gyyxResponse = GyyxRequest.gett(2, "http://lxmjd.cn/trade/6.html");
//						GyyxResponse gyyxResponse = GyyxRequest.gett(2, "http://lxmjd.cn/public/static/AmazeUI/css/amazeui.css");
						// GyyxResponse gyyxResponse = GyyxRequest.gett(2, "http://h5sdk.gyyx.cn/");
						index++;
						long next = System.currentTimeMillis() - currentTimeMillis;
						int statusCode = gyyxResponse.getStatusCode();
						logger.info("index:{},spend:{},resCode:{}", index, next, statusCode);
					} catch (IOException e) {
						logger.error("Exc:", e);
					}
				}

			}).start();
		}

	}
}
