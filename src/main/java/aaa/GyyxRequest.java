package aaa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>Gyyx http request</h3> If the urlParams need combine on url,The tools
 * will urlencoded {@linkplain StandardCharsets#UTF_8} for the urlParams value
 */
public class GyyxRequest {
    
    private final static HttpClient CLIENT;
    private final static PoolingHttpClientConnectionManager CONNMGR;

    static {
        LayeredConnectionSocketFactory ssl = null;
        try {
            ssl = SSLConnectionSocketFactory.getSystemSocketFactory();
        } catch (final SSLInitializationException ex) {
            final SSLContext sslcontext;
            try {
                sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
                sslcontext.init(null, null, null);
                ssl = new SSLConnectionSocketFactory(sslcontext);
            } catch (final SecurityException ignore) {
            } catch (final KeyManagementException ignore) {
            } catch (final NoSuchAlgorithmException ignore) {
            }
        }

        final Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", ssl != null ? ssl : SSLConnectionSocketFactory.getSocketFactory())
            .build();

        CONNMGR = new PoolingHttpClientConnectionManager(sfr);
        CONNMGR.setDefaultMaxPerRoute(500);
        CONNMGR.setMaxTotal(1000);
        CONNMGR.setValidateAfterInactivity(1000);
        CLIENT = HttpClientBuilder.create()
                .setConnectionManager(CONNMGR)
                .build();
    }
    
    
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GyyxRequest.class);
    /**
     * default connect timeout unit millisecond
     */
    private static final int CONNECT_TIMEOUT = 15000;
    /**
     * default socket timeout unit millisecond
     */
    private static final int SOCKET_TIMEOUT = 10000;

    /**
     * simple default get
     * 
     * @param url
     *            The request url
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse get(String url) throws IOException {
        return get(url, null, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * get with url params
     * 
     * @param url
     *            The request url
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse get(String url, Map<String, String> params)
            throws IOException {
        return get(url, params, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * get with url params and headers
     * 
     * @param url
     *            request url
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse get(String url, Map<String, String> params,
            Map<String, String> headers) throws IOException {
        return get(url, params, headers, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * get with url params and headers
     * 
     * @param url
     *            request url
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse get(String url, Map<String, String> params,
            Map<String, String> headers, int connectTimeout, int socketTimeout)
            throws IOException {
        if (headers == null || headers.isEmpty()) {
            return get(url, params, connectTimeout, socketTimeout);
        }
        Header[] httpHeaders = SetHttpHeaders(headers);
        return get(url, params, connectTimeout, socketTimeout, httpHeaders);
    }

    public static Header[] SetHttpHeaders(Map<String, String> headers) {
        return headers.entrySet().stream().map(m -> {
            try {
                return new BasicHeader(m.getKey(), new String(
                        m.getValue().getBytes("utf-8"), "ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList()).toArray(new BasicHeader[0]);
    }

    /**
     * base http get method with url
     * 
     * @param url
     * @param params
     * @param connectTimeout
     * @param socketTimeout
     * @param headers
     * @return
     * @throws IOException
     *             if error
     */
    private static GyyxResponse get(String url, Map<String, String> params,
            int connectTimeout, int socketTimeout, Header... headers)
            throws IOException {
        String reqUrl = combineUrlParams(url, params);
//        return test(reqUrl, connectTimeout, headers);
        Request get = Request.Get(reqUrl).connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout);
        if (headers.length > 0) {
            get = get.setHeaders(headers);
        }
//        HttpResponse response = get.execute().returnResponse();
        HttpResponse response = Executor.newInstance(CLIENT).execute(get).returnResponse();
        return getResponse(response);
    }

    public static GyyxResponse gett(int caseindex, String url) throws IOException {
        String reqUrl = combineUrlParams(url, null);
        //
        if (caseindex == 1) {
            return test(reqUrl, CONNECT_TIMEOUT);
        }
        Request get = Request.Get(reqUrl).connectTimeout(CONNECT_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT);
        HttpResponse response;
        if (caseindex == 2) {
            response = get.execute().returnResponse();
        } else {
            response = Executor.newInstance(CLIENT).execute(get)
                    .returnResponse();
        }
        return getResponse(response);
        //

    }
    /**
     * <h3>Simple post</h3> post url no params using
     * 
     * @param url
     *            the request's url
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse post(String url) throws IOException {
        return post(url, null, null, ContentType.APPLICATION_FORM_URLENCODED,
            null, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * <h3>post url with contentMap</h3> It use
     * {@link ContentType#APPLICATION_FORM_URLENCODED} for contentMap's
     * contentType
     * 
     * @param url
     *            the request's url
     * @param contentParams
     *            The content will add to post body.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse post(String url,
            Map<String, String> contentParams) throws IOException {
        if (contentParams == null || contentParams.isEmpty()) {
            return post(url, null, null,
                ContentType.APPLICATION_FORM_URLENCODED, null, CONNECT_TIMEOUT,
                SOCKET_TIMEOUT);
        }
        String content = contentParams.entrySet().stream()
                .map(m -> m.getKey() + "=" + m.getValue())
                .collect(Collectors.joining("&"));
        return post(url, null, content.getBytes(),
            ContentType.APPLICATION_FORM_URLENCODED, null, CONNECT_TIMEOUT,
            SOCKET_TIMEOUT);
    }

    /**
     * <h3>post url with urlParams , contentMap and headers</h3> It use
     * {@link ContentType#APPLICATION_FORM_URLENCODED} for contentMap's
     * contentType
     * 
     * @param url
     *            the request's url
     * @param urlParams
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param contentParams
     *            The content will add to post body.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse post(String url, Map<String, String> urlParams,
            Map<String, String> contentParams, Map<String, String> headers)
            throws IOException {
        if (contentParams == null || contentParams.isEmpty()) {
            return post(url, urlParams, null,
                ContentType.APPLICATION_FORM_URLENCODED, headers,
                CONNECT_TIMEOUT, SOCKET_TIMEOUT);
        }
        String content = contentParams.entrySet().stream()
                .map(m -> m.getKey() + "=" + m.getValue())
                .collect(Collectors.joining("&"));
        return post(url, urlParams, content.getBytes(),
            ContentType.APPLICATION_FORM_URLENCODED, headers, CONNECT_TIMEOUT,
            SOCKET_TIMEOUT);
    }

    /**
     * <h3>post url with urlParams , content and headers</h3>
     * 
     * @param url
     *            the request's url
     * @param urlParams
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param content
     *            The content will add to post body.If you don't have this,you
     *            could use null to fill it.
     * @param contentType
     *            The contentType will used when content doesn't null.It means
     *            the content Type.If content is null, anything for this is
     *            doesn't work.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse post(String url, Map<String, String> urlParams,
            byte[] content, ContentType contentType,
            Map<String, String> headers) throws IOException {
        return post(url, urlParams, content, contentType, headers,
            CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * <h3>post url with urlParams , content , headers and timeout setting</h3>
     * 
     * @param url
     *            the request's url
     * @param urlParams
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param content
     *            The content will add to post body.If you don't have this,you
     *            could use null to fill it.
     * @param contentType
     *            The contentType will used when content doesn't null.It means
     *            the content Type.If content is null, anything for this is
     *            doesn't work.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse post(String url, Map<String, String> urlParams,
            byte[] content, ContentType contentType,
            Map<String, String> headers, int connectTimeout, int socketTimeout)
            throws IOException {
        if (headers == null || headers.isEmpty()) {
            return post(url, urlParams, content, contentType, connectTimeout,
                socketTimeout);
        }
        Header[] httpHeaders = SetHttpHeaders(headers);
        return post(url, urlParams, content, contentType, connectTimeout,
            socketTimeout, httpHeaders);
    }

    /**
     * post url with content and headers,you can chooose response charset.</br>
     * If not need headers, you could use<code> post(URI uri, String content,
     * ContentType contentType, Charset returnCharset)</code>
     * 
     * @param url
     *            the request's url
     * @param urlParams
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param content
     *            The content will add to post body.If you don't have this,you
     *            could use null to fill it.
     * @param contentType
     *            The contentType will used when content doesn't null.It means
     *            the content Type.If content is null, anything for this is
     *            doesn't work.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @param headers
     *            The headers will add to post headers.
     * @return
     * @throws IOException
     *             if error
     */
    private static GyyxResponse post(String url, Map<String, String> urlParams,
            byte[] content, ContentType contentType, int connectTimeout,
            int socketTimeout, Header... headers) throws IOException {
        String requestUrl = combineUrlParams(url, urlParams);
        Request post = Request.Post(requestUrl).connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout);
        if (content != null) {
            post = post.bodyByteArray(content, contentType);
        }
        if (headers.length > 0) {
            post = post.setHeaders(headers);
        }
        HttpResponse response = post.execute().returnResponse();
        return getResponse(response);
    }

    /**
     * simple default put
     * 
     * @param url
     *            the request's url
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse put(String url) throws IOException {
        return put(url, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static GyyxResponse put(String url, Map<String, String> params)
            throws IOException {
        return put(url, params, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * 
     * @param url
     *            the request's url
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @return
     * @throws IOException
     *             if error
     */
    public static GyyxResponse put(String url, Map<String, String> params,
            Map<String, String> headers, Integer connectTimeout,
            Integer socketTimeout) throws IOException {
        Header[] httpHeaders = SetHttpHeaders(headers);
        return put(url, params, connectTimeout, socketTimeout, httpHeaders);
    }

    /**
     * 
     * @param url
     *            the request's url.
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @param headers
     *            The headers will add to post headers.
     * @return
     * @throws IOException
     *             if error
     */
    private static GyyxResponse put(String url, Map<String, String> params,
            Integer connectTimeout, Integer socketTimeout, Header... headers)
            throws IOException {

        connectTimeout = connectTimeout == null ? CONNECT_TIMEOUT
                : connectTimeout;
        socketTimeout = socketTimeout == null ? SOCKET_TIMEOUT : socketTimeout;

        String reqUrl = combineUrlParams(url, params);
        Request put = Request.Put(reqUrl).connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout);

        if (headers != null && headers.length > 0) {
            put = put.setHeaders(headers);
        }
        HttpResponse response = put.execute().returnResponse();
        return getResponse(response);
    }

    /**
     * simple default delete
     * 
     * @param url
     *            the request's url
     * @return
     * @throws IOException
     *             if error
     */
    public static GyyxResponse delete(String url) throws IOException {
        return delete(url, null, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * 
     * @param url
     *            the request's url.
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @return {@linkplain GyyxResponse}
     * @throws IOException
     *             if error
     */
    public static GyyxResponse delete(String url, Map<String, String> params)
            throws IOException {
        return delete(url, params, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * delete
     * 
     * @param url
     *            the request's url.
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param headers
     *            The headers will add to post headers.If you don't have
     *            this,you could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @return
     * @throws IOException
     *             if error
     */
    public static GyyxResponse delete(String url, Map<String, String> params,
            Map<String, String> headers, Integer connectTimeout,
            Integer socketTimeout) throws IOException {
        Header[] httpHeaders = SetHttpHeaders(headers);
        return delete(url, params, connectTimeout, socketTimeout, httpHeaders);
    }

    /**
     * delete
     * 
     * @param url
     *            the request's url
     * @param params
     *            The params will combine on url.If you don't have this,you
     *            could use null or empty Map to fill it.
     * @param connectTimeout
     *            The request connect timeout. unit millisecond
     * @param socketTimeout
     *            The request socket timeout. unit millisecond
     * @param headers
     *            The headers will add to post headers.
     * @return
     * @throws IOException
     *             if error
     */
    private static GyyxResponse delete(String url, Map<String, String> params,
            Integer connectTimeout, Integer socketTimeout, Header... headers)
            throws IOException {

        connectTimeout = connectTimeout == null ? CONNECT_TIMEOUT
                : connectTimeout;
        socketTimeout = socketTimeout == null ? SOCKET_TIMEOUT : socketTimeout;

        String reqUrl = combineUrlParams(url, params);
        Request delete = Request.Delete(reqUrl).connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout);

        if (headers != null && headers.length > 0) {
            delete = delete.setHeaders(headers);
        }
        HttpResponse response = delete.execute().returnResponse();
        return getResponse(response);
    }

    /**
     * translate {@link HttpResponse} to {@link GyyxResponse}
     * 
     * @param response
     *            httpResponse
     * @param returnCharset
     *            charset
     * @return
     * @throws IOException
     *             if error
     */
    private static GyyxResponse getResponse(HttpResponse response)
            throws IOException {
        Header[] allHeaders = response.getAllHeaders();
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        byte[] content = EntityUtils.toByteArray(entity);
        EntityUtils.consumeQuietly(entity);
        return new GyyxResponse(allHeaders, content, statusCode);
    }

    /**
     * if url need combine params,use it
     * 
     * @param url
     *            the request's url
     * @param params
     *            the request's url params
     * @return
     */
    private static String combineUrlParams(String url,
            Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        String collect = params.entrySet().stream().map(m -> {
            String encode = m.getValue();
            try {
                encode = URLEncoder.encode(m.getValue(),
                    StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                LOGGER.warn("URLEncoder error : ", e);
            }
            return m.getKey() + "=" + encode;
        }).collect(Collectors.joining("&"));
        if (!url.contains("?")) {
            return url + "?" + collect;
        }
        if (url.endsWith("?")) {
            return url + collect;
        }
        return url + "&" + collect;
    }

    private static GyyxResponse test(String link, int connectTimeout,
            Header... headers) {
        HttpURLConnection conn = null;
        URL url = null;
        String result = "";
        int statusCode = 0;
        try {
            url = new java.net.URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            for (int i = 0; i < headers.length; i++) {
                conn.setRequestProperty(headers[i].getName(),
                    headers[i].getValue());
            }
            conn.connect();
            statusCode = conn.getResponseCode();
            InputStream urlStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream));
            String s = "";
            while ((s = reader.readLine()) != null) {
                result += s;
            }
            reader.close();
            urlStream.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GyyxResponse(null, result.getBytes(), statusCode) ;

    }
}