package aaa;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GyyxUnitySignUtil {

    private static final String TIMESTAMP = "timestamp=";
    public static final String SIGN_TYPE = "MD5";
    /**
     * 打印日志
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GyyxUnitySignUtil.class);

    private GyyxUnitySignUtil() {
        // donothing
    }

    /**
     * @Title: signUrl @Description: 根据URL进行签名 @param signUrl @return
     *         String @throws
     */
    public static String signUrl(String signUrl, String key) {
        try {
            URL url = new URL(signUrl);
            long currentTime = System.currentTimeMillis() / 1000;
            String query = url.getQuery() + "&" + TIMESTAMP + currentTime;
            String path = url.getPath();
            String sign = getSigns(path, query, key);
            return signUrl + "&" + TIMESTAMP + currentTime + "&sign=" + sign
                    + "&sign_type=MD5";
        } catch (MalformedURLException e) {
            LOGGER.error("签名URL失败：{}", e);
        }
        return null;
    }

    public static String signUrl(String baseUrl, Map<String, String> params,
            String key) {

        String queryWithUrlEncode = params.entrySet().stream().map(m -> {
            String val;
            try {
                val = m.getValue() == null ? ""
                        : URLEncoder.encode(m.getValue(),
                            StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                val = "";
                LOGGER.error("签名URL参数编码异常：", e);
            }
            return m.getKey() + "=" + val;
        }).collect(Collectors.joining("&"));
        String base = baseUrl.endsWith("?") ? baseUrl : (baseUrl + "?");
        long currentTime = System.currentTimeMillis() / 1000;
        boolean hasTimestamp = queryWithUrlEncode.contains(TIMESTAMP);
        String sign = getSignWithBaseUrl(base, params, key);
        return base + queryWithUrlEncode
                + (hasTimestamp ? "" : ("&" + TIMESTAMP + currentTime))
                + "&sign=" + sign + "&sign_type=MD5";
    }

    public static String getSignWithBaseUrl(String baseUrl,
            Map<String, String> params, String key) {
        try {

            String query = params.entrySet().stream().map(m -> {
                String val = m.getValue() == null ? "" : m.getValue();
                return m.getKey() + "=" + val;
            }).collect(Collectors.joining("&"));
            String base = baseUrl.endsWith("?") ? baseUrl : (baseUrl + "?");
            long currentTime = System.currentTimeMillis() / 1000;
            boolean hasTimestamp = query.contains(TIMESTAMP);
            query = query
                    + (hasTimestamp ? "" : ("&" + TIMESTAMP + currentTime));
            URL url = new URL(base);
            String path = url.getPath() + "?";
            return getSigns(path, query, key);
        } catch (MalformedURLException e) {
            LOGGER.error("签名URL获取Sign失败：{}", e);
        }
        return null;
    }

    /**
     * @Title: getSign @Description: 字符串URL进行签名 @param partUrl @param paramsStr
     *         query path @param key @return String @throws
     */
    public static String getSigns(String partUrl, String paramsStr,
            String key) {
        Map<String, String> params = parseQueryParams(paramsStr);
        return getSign(partUrl, params, key);
    }

    /**
     * @Title: parseQueryParams @Description: 根据url获取参数为map @param
     *         paramsStr @return Map<String,String> @throws
     */
    public static Map<String, String> parseQueryParams(String paramsStr) {
        String thisParamsStr = paramsStr;
        if (StringUtils.isBlank(thisParamsStr)) {
            return null;
        }
        Map<String, String> params = new HashMap<>();
        if (thisParamsStr.contains("?")
                && thisParamsStr.split("\\?").length >= 2) {
            thisParamsStr = thisParamsStr.split("\\?")[1];
        }
        String[] splits = thisParamsStr.split("&");
        Arrays.asList(splits).stream().forEach(e -> {
            String[] split = e.split("=");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            } else if (split.length == 1) {
                params.put(split[0], "");
            }
        });
        return params;
    }

    /**
     * @Title:getSign
     * @param partyUrl
     *            url的部分
     *            如：http://api.mobile.gyyx.cn/api/ChargeUser/?key1=value1中/api/
     *            ChargeUser/?
     * @param params
     *            Hashmap键值对封装参数
     * @param key
     *            key
     * @param input_charset
     *            编码
     * @return
     * @return String
     * @throws @Description:
     *             请求的sign生成方法
     */
    public static String getSign(String partUrl, Map<String, String> params,
            String key) {
        String thisPartUrl = partUrl;
        // 必须有参数
        if (params.isEmpty() || stringIsEmpty(thisPartUrl)
                || stringIsEmpty(key)) {
            return "";
        }
        // url改為 以?結尾的
        if (!thisPartUrl.endsWith("?")) {
            thisPartUrl += "?";
        }
        // url+?+params拼接成的string
        String signString = thisPartUrl
                + signStringDelEmptyParam(params, false, "&") + key;
        return getStrSign(signString);
    }

    /**
     * @Title: signString @Description: 抛出指定不需要签名、为空的参数
     *         此方法能够将map组装成&链接的url字符串，第二个参数去掉不需要拼装的参数。 @param params @param
     *         exceptKeys @return String @throws
     */
    public static String combineIntoUrlParams(Map<String, String> params,
            boolean excludeZero, String spliter, String... exceptKeys) {
        Map<String, String> signMap = new HashMap<>();

        params.keySet().stream()
                .filter(
                    e -> (exceptKeys == null || "".equals(exceptKeys[0])) ? true
                            : !Arrays.asList(exceptKeys).contains(e))
                .forEach(e -> signMap.put(e,
                    params.get(e) != null ? String.valueOf(params.get(e))
                            : ""));

        return signStringDelEmptyParam(signMap, excludeZero, spliter);
    }

    /**
     * @Title: signString @Description: 抛出指定不需要签名的参数
     *         此方法能够将map组装成&链接的url字符串，第二个参数去掉指定不需要拼装的参数。 @param params @param
     *         exceptKeys @return String @throws
     */
    public static String combineIntoUrlParams(Map<String, String> params,
            String spliter, String... exceptKeys) {
        Map<String, String> signMap = new HashMap<>();

        params.keySet().stream()
                .filter(
                    e -> (exceptKeys == null || "".equals(exceptKeys[0])) ? true
                            : !Arrays.asList(exceptKeys).contains(e))
                .forEach(e -> signMap.put(e,
                    params.get(e) != null ? String.valueOf(params.get(e))
                            : ""));

        return signStringAllParams(signMap, spliter);
    }

    /**
     * @Title:signString
     * @param params
     *            HashMap
     * @return
     * @return String
     * @throws @Description:
     *             对非空 参数进行拼接
     */
    public static String signStringDelEmptyParam(Map<String, String> params,
            boolean excludeZero, String spliter) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder prestr = new StringBuilder("");

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (stringIsEmpty(value) || (excludeZero && "0".equals(value))) {// value为空时不参与签名
                continue;
            }
            prestr.append(key).append("=").append(value);
            if (!StringUtils.isEmpty(spliter) && i != keys.size() - 1) {
                prestr.append(spliter);
            }
        }

        if (!StringUtils.isEmpty(spliter) && prestr.length() > 0
                && prestr.toString().endsWith(spliter)) {
            return prestr.substring(0, prestr.length() - 1);
        }
        return prestr.toString();
    }

    /**
     * @Title:signString
     * @param params
     *            HashMap
     * @return
     * @return String
     * @throws @Description:
     *             对所有参数进行拼接
     */
    public static String signStringAllParams(Map<String, String> params,
            String spliter) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder prestr = new StringBuilder("");

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            prestr.append(key).append("=").append(value);
            if (!StringUtils.isEmpty(spliter) && i != keys.size() - 1) {
                prestr.append(spliter);
            }
        }

        if (!StringUtils.isEmpty(spliter) && prestr.length() > 0
                && prestr.toString().endsWith(spliter)) {
            return prestr.substring(0, prestr.length() - 1);
        }
        return prestr.toString();
    }

    /**
     * @Title: signIsLegal @Description: 签名是否合法 @param partUrl @param
     *         params @param key @return boolean @throws
     */
    public static boolean signIsLegal(String partUrl,
            Map<String, String> requestParams, Map<String, String> signParams) {
        String key = signParams.get("key");
        String sign = signParams.get("sign");
        // 参数为空时 不合法
        if (requestParams.isEmpty() || stringIsEmpty(partUrl) // key和签名Url为空
                || stringIsEmpty(key)) {
            return false;
        }
        String timeStampStr = requestParams.get("timestamp");
        if (stringIsEmpty(sign)// 不包含 sign 和时间戳时，不合法
                || stringIsEmpty(timeStampStr) || !charsIs0To9(timeStampStr)) {// 时间戳全是数字
                                                                               // 否则不合法
            return false;
        }
        // 获取当前时间戳
        long currentTime = System.currentTimeMillis() / 1000;
        long timeStamp = Long.parseLong(timeStampStr);
        // 相差五分钟以上 不合法
        if (Math.abs(currentTime - timeStamp) > 300)
            return false;
        LOGGER.info("-------------sign:" + sign);
        // 签名相同则合法
        if (sign.equals(getSign(partUrl, requestParams, key))) {
            return true;
        }
        return false;
    }

    /**
     * @Title:stringIsEmpty
     * @param query
     * @return
     * @return boolean
     * @throws @Description:
     *             验证String是否长度为空，或者为null
     */
    private static boolean stringIsEmpty(String query) {
        boolean ret = false;
        if (query == null || "".equals(query.trim())) {
            ret = true;
        }
        return ret;
    }

    /**
     * @Title:charsIs0To9
     * @param chars
     * @return
     * @return boolean
     * @throws @Description:
     *             string的char是否都是0-9之间的
     */
    private static boolean charsIs0To9(String chars) {
        return chars.matches("[0-9]+");
    }

    public static boolean signIsLegalWithoutUrl(
            Map<String, String> requestParams, Map<String, String> signParams) {
        String key = signParams.get("key");
        String sign = signParams.get("sign");
        // 参数为空时 不合法
        if (requestParams.isEmpty() || stringIsEmpty(key)) {
            return false;
        }
        String timeStampStr = requestParams.get("timestamp");
        if (stringIsEmpty(sign)// 不包含 sign 和时间戳时，不合法
                || stringIsEmpty(timeStampStr) || !charsIs0To9(timeStampStr)) {// 时间戳全是数字
                                                                               // 否则不合法
            return false;
        }
        // 获取当前时间戳
        long currentTime = System.currentTimeMillis() / 1000;
        long timeStamp = Long.parseLong(timeStampStr);
        // 相差五分钟以上 不合法
        if (Math.abs(currentTime - timeStamp) > 300)
            return false;
        LOGGER.info("-------------sign:" + sign);
        // 签名相同则合法
        if (sign.equals(getSign(requestParams, key))) {
            return true;
        }
        return false;
    }

    public static String getSign(Map<String, String> params, String key) {
        // 必须有参数
        if (params.isEmpty() || stringIsEmpty(key)) {
            return "";
        }
        String signString = signStringDelEmptyParam(params, false, "&") + key;
        return getStrSign(signString);
    }

    private static String getStrSign(String signString) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance(SIGN_TYPE)
                    .digest(signString.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10)
                    hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            LOGGER.error("MD%算法签名异常：{}", e);
        }
        return "";
    }
}
