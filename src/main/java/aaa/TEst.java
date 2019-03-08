package aaa;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TEst {
    private static int index = 0;
    private static final Logger logger = LoggerFactory.getLogger(TEst.class);

    public static void main(String[] args)
            throws ClientProtocolException, IOException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        String signUrl = GyyxUnitySignUtil.signUrl("http://10.12.54.9:8001/log/test?content=aascas", "123456");
        for (int i = 0; i < 150; i++) {
            new Thread(() -> {
                while (true) {
                    if (index > Integer.valueOf(args[0])) {
                        logger.error("spend:{}",
                            Calendar.getInstance().getTime().getTime()
                                    - calendar.getTime().getTime());
                        break;
                    }
                    try {
                        long currentTimeMillis = System.currentTimeMillis();
                        GyyxResponse gyyxResponse = GyyxRequest.gett(Integer.valueOf(args[1]),signUrl);
                        index++;
                        long next = System.currentTimeMillis()
                                - currentTimeMillis;
                        int statusCode = gyyxResponse.getStatusCode();
                        String content = gyyxResponse.getStringContent();
                        if (statusCode == 200 && "success".equals(content)) {
                            logger.info("index:{},spend:{},resCode:{},res:{}",
                                index, next, statusCode, content);
                        } else {
                            logger.error("index:{},spend:{},resCode:{},res:{}",
                                index, next, statusCode, content);
                        }
                    } catch (IOException e) {
                        logger.error("Exc:", e);
                    }
                }

            }).start();
        }

    }
}
